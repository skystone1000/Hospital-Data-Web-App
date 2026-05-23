import { NextRequest, NextResponse } from "next/server";
import pool from "@/lib/db";
import { patientSchema } from "@/lib/validations";

const SORT_WHITELIST = ["firstName", "lastName", "dateJoined", "regno", "diagnosis"] as const;

export async function GET(req: NextRequest) {
  const sp = req.nextUrl.searchParams;
  const page = Math.max(1, Number(sp.get("page") ?? 1));
  const perPage = 20;
  const offset = (page - 1) * perPage;
  const sortParam = sp.get("sort") ?? "dateJoined";
  const sort = SORT_WHITELIST.includes(sortParam as any) ? sortParam : "dateJoined";
  const order = sp.get("order") === "asc" ? "ASC" : "DESC";
  const search = sp.get("search")?.trim() ?? "";

  try {
    let whereClause = "1=1";
    const params: (string | number)[] = [];

    if (search) {
      whereClause = "firstName LIKE ? OR lastName LIKE ? OR regno LIKE ?";
      const term = `%${search}%`;
      params.push(term, term, term);
    }

    const [[{ total }]] = await pool.execute(
      `SELECT COUNT(*) AS total FROM patient_data WHERE ${whereClause}`,
      params
    ) as any;

    const [rows] = await pool.execute(
      `SELECT id, firstName, middleName, lastName, age, sex, phone, address, regno,
              diagnosis, dateJoined, paid, balance, treatment
       FROM patient_data
       WHERE ${whereClause}
       ORDER BY ${sort} ${order}
       LIMIT ${perPage} OFFSET ${offset}`,
      params
    );

    return NextResponse.json({ data: rows, total, page, perPage });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const parsed = patientSchema.safeParse(body);
    if (!parsed.success) {
      return NextResponse.json({ error: "Invalid input", fields: parsed.error.flatten().fieldErrors }, { status: 422 });
    }
    const d = parsed.data;
    const today = new Date().toISOString().slice(0, 10);
    const dateJoined = d.dateJoined ?? today;

    const [result] = await pool.execute(
      `INSERT INTO patient_data
       (firstName, middleName, lastName, age, sex, occupation, address, phone, regno,
        height, weight, diagnosis, cc1, cc2, cc3, appetite, desire, aversions, thirst,
        perspiration, sleep, stool, urine, menses, thermal, mind, hobbies, particulars,
        on_examination, path_inv, previous_rx, past_history, family_history,
        treatment, paid, balance, dateJoined)
       VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`,
      [d.firstName, d.middleName ?? null, d.lastName, d.age, d.sex ?? null, d.occupation ?? null,
       d.address ?? null, d.phone ?? null, d.regno ?? null, d.height ?? null, d.weight ?? null,
       d.diagnosis ?? null, d.cc1 ?? null, d.cc2 ?? null, d.cc3 ?? null, d.appetite ?? null,
       d.desire ?? null, d.aversions ?? null, d.thirst ?? null, d.perspiration ?? null,
       d.sleep ?? null, d.stool ?? null, d.urine ?? null, d.menses ?? null, d.thermal ?? null,
       d.mind ?? null, d.hobbies ?? null, d.particulars ?? null, d.on_examination ?? null,
       d.path_inv ?? null, d.previous_rx ?? null, d.past_history ?? null,
       d.family_history ?? null, d.treatment ?? null, d.paid ?? null, d.balance ?? null,
       dateJoined]
    ) as any;

    return NextResponse.json({ id: result.insertId }, { status: 201 });
  } catch (err) {
    console.error(err);
    return NextResponse.json({ error: "Server error" }, { status: 500 });
  }
}
