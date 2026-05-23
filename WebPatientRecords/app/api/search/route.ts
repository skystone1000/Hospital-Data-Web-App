import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";

export async function GET(req: NextRequest) {
  const q = req.nextUrl.searchParams.get("q")?.trim() ?? "";
  if (!q) return NextResponse.json({ data: [] });

  try {
    const term = `%${q}%`;
    const [rows] = await pool.execute(
      `SELECT id, firstName, lastName, regno, address, phone, diagnosis, dateJoined, paid, balance
       FROM patient_data
       WHERE firstName LIKE ? OR lastName LIKE ? OR regno LIKE ?
       LIMIT 20`,
      [term, term, term]
    );
    return NextResponse.json({ data: rows });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}
