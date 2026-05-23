import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";
import { followUpSchema } from "@/lib/validations";
import { buildLocalDateTime } from "@/lib/utils";

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

    await pool.execute(
      `INSERT INTO follow_up_data
       (id, date, regno, follow_up_num, weight, treatment_output, other_complains, treatment, medicine_duration, paid, balance)
       VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`,
      [id, buildLocalDateTime(d.date), regno, nextNum, d.weight ?? null, d.treatment_output ?? null,
       d.other_complains ?? null, d.treatment ?? null, d.medicine_duration ?? null,
       d.paid ?? null, d.balance ?? null]
    );

    return NextResponse.json({ ok: true }, { status: 201 });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}
