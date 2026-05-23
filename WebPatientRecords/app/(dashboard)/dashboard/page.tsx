import { getSession } from "@/lib/session";
import pool from "@/lib/db";
import { DashboardStats } from "@/types";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, Activity, TrendingUp, Calendar, ArrowRight } from "lucide-react";
import Link from "next/link";

async function getDashboardData() {
  async function periodStats(days: number) {
    const dateFilter =
      days === 0 ? "DATE(dateJoined) = CURDATE()" : `dateJoined >= DATE_SUB(CURDATE(), INTERVAL ${days} DAY)`;
    const followFilter =
      days === 0 ? "DATE(date) = CURDATE()" : `date >= DATE_SUB(CURDATE(), INTERVAL ${days} DAY)`;

    const [[np]] = await pool.execute(`SELECT COUNT(*) AS c FROM patient_data WHERE ${dateFilter}`) as any;
    const [[fu]] = await pool.execute(`SELECT COUNT(*) AS c FROM follow_up_data WHERE ${followFilter}`) as any;
    const [[ep]] = await pool.execute(`SELECT SUM(CAST(paid AS DECIMAL(10,2))) AS s FROM patient_data WHERE ${dateFilter}`) as any;
    const [[ef]] = await pool.execute(`SELECT SUM(CAST(paid AS DECIMAL(10,2))) AS s FROM follow_up_data WHERE ${followFilter}`) as any;

    return {
      newPatients: Number(np.c ?? 0),
      followUps: Number(fu.c ?? 0),
      earnings: Number(ep.s ?? 0) + Number(ef.s ?? 0),
    };
  }

  const [today, week, month, year] = await Promise.all([
    periodStats(0), periodStats(7), periodStats(30), periodStats(365),
  ]);

  // Recent patients (last 7 days)
  const [recentPatients] = await pool.execute(
    `SELECT id, firstName, lastName, dateJoined, address, phone, paid, balance, treatment
     FROM patient_data WHERE dateJoined >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
     ORDER BY dateJoined DESC LIMIT 10`
  ) as any;

  // Recent follow-ups (last 7 days)
  const [recentFollowUps] = await pool.execute(
    `SELECT p.id, p.firstName, p.lastName, p.dateJoined, p.address, p.phone,
            f.paid, f.balance, f.treatment
     FROM follow_up_data f
     INNER JOIN patient_data p ON f.id = p.id
     WHERE f.date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
     LIMIT 10`
  ) as any;

  // Admin info
  return { today, week, month, year, recentPatients, recentFollowUps };
}

function StatCard({
  title, icon: Icon, patients, followUps, earnings, color,
}: {
  title: string;
  icon: React.ElementType;
  patients: number;
  followUps: number;
  earnings: number;
  color: string;
}) {
  return (
    <Card className="relative overflow-hidden">
      <div className={`absolute inset-x-0 top-0 h-1 ${color}`} />
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-sm font-medium text-muted-foreground">{title}</CardTitle>
        <Icon className="h-4 w-4 text-muted-foreground" />
      </CardHeader>
      <CardContent className="space-y-1">
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">New Patients</span>
          <span className="font-semibold">{patients}</span>
        </div>
        <div className="flex justify-between text-sm">
          <span className="text-muted-foreground">Follow-ups</span>
          <span className="font-semibold">{followUps}</span>
        </div>
        <div className="flex justify-between text-sm pt-1 border-t">
          <span className="text-muted-foreground">Earnings</span>
          <span className="font-semibold text-primary">₹{earnings.toLocaleString()}</span>
        </div>
      </CardContent>
    </Card>
  );
}

export default async function DashboardPage() {
  const session = await getSession();
  const { today, week, month, year, recentPatients, recentFollowUps } = await getDashboardData();

  const hour = new Date().getHours();
  const greeting = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold">
          {greeting}, Dr. {session.firstName} {session.lastName}
        </h1>
        <p className="text-muted-foreground mt-1">Here's what's happening at your clinic today.</p>
      </div>

      {/* Stat cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <StatCard title="Today" icon={Activity} patients={today.newPatients} followUps={today.followUps} earnings={today.earnings} color="bg-blue-500" />
        <StatCard title="This Week" icon={Calendar} patients={week.newPatients} followUps={week.followUps} earnings={week.earnings} color="bg-violet-500" />
        <StatCard title="This Month" icon={TrendingUp} patients={month.newPatients} followUps={month.followUps} earnings={month.earnings} color="bg-emerald-500" />
        <StatCard title="This Year" icon={Users} patients={year.newPatients} followUps={year.followUps} earnings={year.earnings} color="bg-amber-500" />
      </div>

      {/* Recent tables */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* New patients */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-base">New Patients — Last 7 Days</CardTitle>
            <Link href="/patients" className="text-xs text-primary flex items-center gap-1 hover:underline">
              View all <ArrowRight className="h-3 w-3" />
            </Link>
          </CardHeader>
          <CardContent>
            {recentPatients.length === 0 ? (
              <p className="text-sm text-muted-foreground py-4 text-center">No new patients this week.</p>
            ) : (
              <div className="space-y-2">
                {recentPatients.map((p: any) => (
                  <Link
                    key={p.id}
                    href={`/patients/${p.id}`}
                    className="flex items-center justify-between p-2 rounded-md hover:bg-muted transition-colors group"
                  >
                    <div>
                      <p className="text-sm font-medium group-hover:text-primary transition-colors">
                        {p.firstName} {p.lastName}
                      </p>
                      <p className="text-xs text-muted-foreground">{p.dateJoined}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-primary">₹{p.paid ?? 0}</p>
                      <p className="text-xs text-muted-foreground">Bal: ₹{p.balance ?? 0}</p>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Follow-ups */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <CardTitle className="text-base">Follow-ups — Last 7 Days</CardTitle>
            <Link href="/patients" className="text-xs text-primary flex items-center gap-1 hover:underline">
              View all <ArrowRight className="h-3 w-3" />
            </Link>
          </CardHeader>
          <CardContent>
            {recentFollowUps.length === 0 ? (
              <p className="text-sm text-muted-foreground py-4 text-center">No follow-ups this week.</p>
            ) : (
              <div className="space-y-2">
                {recentFollowUps.map((p: any, i: number) => (
                  <Link
                    key={`${p.id}-${i}`}
                    href={`/patients/${p.id}`}
                    className="flex items-center justify-between p-2 rounded-md hover:bg-muted transition-colors group"
                  >
                    <div>
                      <p className="text-sm font-medium group-hover:text-primary transition-colors">
                        {p.firstName} {p.lastName}
                      </p>
                      <p className="text-xs text-muted-foreground">{p.dateJoined}</p>
                    </div>
                    <div className="text-right">
                      <p className="text-sm font-medium text-primary">₹{p.paid ?? 0}</p>
                      <p className="text-xs text-muted-foreground">Bal: ₹{p.balance ?? 0}</p>
                    </div>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
