import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";
import { followUpSchema } from "@/lib/validations";
import { buildLocalDateTime } from "@/lib/utils";

export async function PUT(req: NextRequest, { params }: { params: { id: string; followUpId: string } }) {
  const id = parseInt(params.id);
  const followUpId = parseInt(params.followUpId);
  if (isNaN(id) || isNaN(followUpId)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const body = await req.json();
    const parsed = followUpSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid input", fields: parsed.error.flatten().fieldErrors }, { status: 422 });
    }
    const d = parsed.data;

    await pool.execute(
      `UPDATE follow_up_data SET
       date = ?, weight = ?, treatment_output = ?, other_complains = ?,
       treatment = ?, medicine_duration = ?, paid = ?, balance = ?
       WHERE followUpId = ? AND id = ?`,
      [buildLocalDateTime(d.date), d.weight ?? null, d.treatment_output ?? null, d.other_complains ?? null,
       d.treatment ?? null, d.medicine_duration ?? null, d.paid ?? null, d.balance ?? null,
       followUpId, id]
    );

    return NextResponse.json({ ok: true });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

export async function DELETE(_req: NextRequest, { params }: { params: { id: string; followUpId: string } }) {
  const id = parseInt(params.id);
  const followUpId = parseInt(params.followUpId);
  if (isNaN(id) || isNaN(followUpId)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    await pool.execute(
      "DELETE FROM follow_up_data WHERE followUpId = ? AND id = ?",
      [followUpId, id]
    );
    return NextResponse.json({ ok: true });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}
