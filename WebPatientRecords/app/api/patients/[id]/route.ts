import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";
import { patientSchema } from "@/lib/validations";

export async function GET(_req: NextRequest, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const [rows] = await pool.execute("SELECT * FROM patient_data WHERE id = ?", [id]);
    const patient = (rows as any[])[0];
    if (!patient) return NextResponse.json({ error: "Not found" }, { status: 404 });
    return NextResponse.json(patient);
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

export async function PUT(req: NextRequest, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  try {
    const body = await req.json();
    const parsed = patientSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid input", fields: parsed.error.flatten().fieldErrors }, { status: 422 });
    }
    const d = parsed.data;

    await pool.execute(
      `UPDATE patient_data SET
       firstName=?, middleName=?, lastName=?, age=?, sex=?, occupation=?, address=?, phone=?,
       regno=?, height=?, weight=?, diagnosis=?, cc1=?, cc2=?, cc3=?, appetite=?, desire=?,
       aversions=?, thirst=?, perspiration=?, sleep=?, stool=?, urine=?, menses=?, thermal=?,
       mind=?, hobbies=?, particulars=?, on_examination=?, path_inv=?, previous_rx=?,
       past_history=?, family_history=?, treatment=?, paid=?, balance=?, dateJoined=?
       WHERE id=?`,
      [d.firstName, d.middleName ?? null, d.lastName, d.age, d.sex ?? null, d.occupation ?? null,
       d.address ?? null, d.phone ?? null, d.regno ?? null, d.height ?? null, d.weight ?? null,
       d.diagnosis ?? null, d.cc1 ?? null, d.cc2 ?? null, d.cc3 ?? null, d.appetite ?? null,
       d.desire ?? null, d.aversions ?? null, d.thirst ?? null, d.perspiration ?? null,
       d.sleep ?? null, d.stool ?? null, d.urine ?? null, d.menses ?? null, d.thermal ?? null,
       d.mind ?? null, d.hobbies ?? null, d.particulars ?? null, d.on_examination ?? null,
       d.path_inv ?? null, d.previous_rx ?? null, d.past_history ?? null,
       d.family_history ?? null, d.treatment ?? null, d.paid ?? null, d.balance ?? null,
       d.dateJoined ?? null, id]
    );

    return NextResponse.json({ ok: true });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

export async function DELETE(_req: NextRequest, { params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) return NextResponse.json({ error: "Invalid id" }, { status: 400 });

  const conn = await pool.getConnection();
  try {
    await conn.beginTransaction();
    await conn.execute("DELETE FROM follow_up_data WHERE id = ?", [id]);
    await conn.execute("DELETE FROM patient_data WHERE id = ?", [id]);
    await conn.commit();
    return NextResponse.json({ ok: true });
  } catch (err) {
    await conn.rollback();
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  } finally {
    conn.release();
  }
}
