import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";
import { followUpSchema } from "@/lib/validations";

export async function GET(_req: NextRequest, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const [rows] = await pool.execute(
      "SELECT * FROM follow_up_data WHERE id = ? ORDER BY CAST(follow_up_num AS UNSIGNED) DESC",
      [id]
    );
    return NextResponse.json({ data: rows });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

export async function POST(req: NextRequest, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const body = await req.json();
    const parsed = followUpSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid input", fields: parsed.error.flatten().fieldErrors }, { status: 422 });
    }

    // Get next follow-up number
    const [[maxRow]] = await pool.execute(
      "SELECT MAX(CAST(follow_up_num AS UNSIGNED)) AS maxNum FROM follow_up_data WHERE id = ?",
      [id]
    ) as any;
    const nextNum = (maxRow?.maxNum ?? 0) + 1;

    // Get regno from patient
    const [[patient]] = await pool.execute(
      "SELECT regno FROM patient_data WHERE id = ?",
      [id]
    ) as any;
    const regno = patient?.regno ?? "";

    const d = parsed.data;
    const today = new Date().toISOString().slice(0, 10);

    await pool.execute(
      `INSERT INTO follow_up_data
       (id, date, regno, follow_up_num, weight, treatment_output, other_complains, treatment, medicine_duration, paid, balance)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [id, today, regno, nextNum, d.weight, d.treatment_output, d.other_complains,
       d.treatment, d.medicine_duration, d.paid, d.balance]
    );

    return NextResponse.json({ ok: true }, { status: 201 });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}
