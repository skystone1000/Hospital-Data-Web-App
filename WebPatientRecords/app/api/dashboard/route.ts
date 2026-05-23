import { NextResponse } from "next/server";
import pool from "@/lib/db";

async function count(sql: string): Promise<number> {
  const [rows] = await pool.execute(sql);
  return (rows as { c: string }[])[0]?.c ? Number((rows as any)[0].c) : 0;
}

async function sum(sql: string): Promise<number> {
  const [rows] = await pool.execute(sql);
  return (rows as { s: string }[])[0]?.s ? Number((rows as any)[0].s) : 0;
}

export async function GET() {
  try {
    const [today, week, month, year] = await Promise.all([
      buildPeriodStats(0),
      buildPeriodStats(7),
      buildPeriodStats(30),
      buildPeriodStats(365),
    ]);
    return NextResponse.json({ today, week, month, year });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

async function buildPeriodStats(days: number) {
  const interval = days === 0 ? "INTERVAL 0 DAY" : `INTERVAL ${days} DAY`;
  const dateFilter =
    days === 0
      ? "DATE(dateJoined) = CURDATE()"
      : `dateJoined >= DATE_SUB(CURDATE(), ${interval})`;
  const followDateFilter =
    days === 0
      ? "DATE(date) = CURDATE()"
      : `date >= DATE_SUB(CURDATE(), ${interval})`;

  const [newPatients, followUps, paidPatients, paidFollowUps] = await Promise.all([
    count(`SELECT COUNT(*) AS c FROM patient_data WHERE ${dateFilter}`),
    count(`SELECT COUNT(*) AS c FROM follow_up_data WHERE ${followDateFilter}`),
    sum(`SELECT SUM(CAST(paid AS DECIMAL(10,2))) AS s FROM patient_data WHERE ${dateFilter}`),
    sum(`SELECT SUM(CAST(paid AS DECIMAL(10,2))) AS s FROM follow_up_data WHERE ${followDateFilter}`),
  ]);

  return {
    newPatients,
    followUps,
    earnings: paidPatients + paidFollowUps,
  };
}
