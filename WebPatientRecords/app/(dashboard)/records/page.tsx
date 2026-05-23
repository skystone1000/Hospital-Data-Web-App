import pool from "@/lib/db";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import Link from "next/link";
import { Eye, Download } from "lucide-react";
import { PatientsSearch } from "@/components/patients/patients-search";

async function getAllRecords(search: string) {
  let where = "1=1";
  const params: string[] = [];
  if (search) {
    where = "(firstName LIKE ? OR lastName LIKE ? OR regno LIKE ? OR diagnosis LIKE ?)";
    const t = `%${search}%`;
    params.push(t, t, t, t);
  }

  const [rows] = await pool.execute(
    `SELECT id, firstName, middleName, lastName, age, sex, address, phone, regno,
            dateJoined, diagnosis, treatment, paid, balance,
            cc1, cc2, cc3, occupation, height, weight
     FROM patient_data WHERE ${where}
     ORDER BY dateJoined DESC`,
    params
  ) as any;
  return rows;
}

interface PageProps {
  searchParams: { search?: string };
}

export default async function RecordsPage({ searchParams }: PageProps) {
  const search = searchParams.search ?? "";
  const rows = await getAllRecords(search);

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Detailed Records</h1>
          <p className="text-sm text-muted-foreground mt-0.5">{rows.length} records</p>
        </div>
        <Button variant="outline" size="sm" className="gap-2" id="export-csv">
          <Download className="h-4 w-4" />
          Export CSV
        </Button>
      </div>

      <PatientsSearch defaultValue={search} />

      <div className="rounded-lg border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-xs">
            <thead className="bg-muted/50 border-b">
              <tr>
                {["Name", "Reg No", "Age", "Sex", "Date Joined", "Address", "Phone",
                  "Diagnosis", "CC1", "CC2", "CC3", "Treatment", "Paid", "Balance", ""].map((h) => (
                  <th key={h} className="text-left px-3 py-2.5 font-medium text-muted-foreground whitespace-nowrap">{h}</th>
                ))}
              </tr>
            </thead>
            <tbody className="divide-y">
              {rows.length === 0 && (
                <tr>
                  <td colSpan={15} className="text-center py-10 text-muted-foreground">No records found.</td>
                </tr>
              )}
              {rows.map((p: any) => (
                <tr key={p.id} className="hover:bg-muted/30 transition-colors">
                  <td className="px-3 py-2 font-medium whitespace-nowrap">{p.firstName} {p.lastName}</td>
                  <td className="px-3 py-2 text-muted-foreground">{p.regno}</td>
                  <td className="px-3 py-2">{p.age}</td>
                  <td className="px-3 py-2">{p.sex}</td>
                  <td className="px-3 py-2 whitespace-nowrap">{p.dateJoined}</td>
                  <td className="px-3 py-2 max-w-[120px] truncate" title={p.address}>{p.address}</td>
                  <td className="px-3 py-2">{p.phone}</td>
                  <td className="px-3 py-2 max-w-[120px] truncate" title={p.diagnosis}>
                    {p.diagnosis && <Badge variant="secondary" className="text-xs">{p.diagnosis}</Badge>}
                  </td>
                  <td className="px-3 py-2 max-w-[100px] truncate" title={p.cc1}>{p.cc1}</td>
                  <td className="px-3 py-2 max-w-[100px] truncate" title={p.cc2}>{p.cc2}</td>
                  <td className="px-3 py-2 max-w-[100px] truncate" title={p.cc3}>{p.cc3}</td>
                  <td className="px-3 py-2 max-w-[120px] truncate" title={p.treatment}>{p.treatment}</td>
                  <td className="px-3 py-2 font-medium text-primary">₹{p.paid}</td>
                  <td className="px-3 py-2">₹{p.balance}</td>
                  <td className="px-3 py-2">
                    <Link href={`/patients/${p.id}`}>
                      <Button variant="ghost" size="icon" className="h-7 w-7">
                        <Eye className="h-3 w-3" />
                      </Button>
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* CSV export script */}
      <script dangerouslySetInnerHTML={{
        __html: `
          document.getElementById('export-csv')?.addEventListener('click', () => {
            const table = document.querySelector('table');
            if (!table) return;
            const rows = [...table.querySelectorAll('tr')].map(tr =>
              [...tr.querySelectorAll('th,td')].slice(0,-1).map(td => '"' + td.textContent.replace(/"/g,'""') + '"').join(',')
            );
            const blob = new Blob([rows.join('\\n')], {type:'text/csv'});
            const a = document.createElement('a');
            a.href = URL.createObjectURL(blob);
            a.download = 'patient-records.csv';
            a.click();
          });
        `,
      }} />
    </div>
  );
}
