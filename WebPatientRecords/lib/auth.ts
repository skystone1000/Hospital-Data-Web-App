import bcrypt from "bcryptjs";
import pool from "./db";
import { getSession } from "./session";

interface AdminRow {
  id_admin: number;
  uid_admin: string;
  firstName: string;
  lastName: string;
  password_admin: string;
  email_admin: string;
}

export async function loginAdmin(
  identifier: string,
  password: string
): Promise<{ ok: true } | { ok: false; error: string }> {
  const [rows] = await pool.execute(
    "SELECT * FROM admin_users WHERE email_admin = ? OR uid_admin = ? LIMIT 1",
    [identifier, identifier]
  );

  const admin = (rows as AdminRow[])[0];
  if (!admin) return { ok: false, error: "Invalid credentials" };

  let matched = false;

  if (admin.password_admin.startsWith("$2")) {
    matched = await bcrypt.compare(password, admin.password_admin);
  } else {
    // Plaintext — transparent migration to bcrypt on successful login
    if (password === admin.password_admin) {
      matched = true;
      const hash = await bcrypt.hash(password, 12);
      await pool.execute(
        "UPDATE admin_users SET password_admin = ? WHERE id_admin = ?",
        [hash, admin.id_admin]
      );
    }
  }

  if (!matched) return { ok: false, error: "Invalid credentials" };

  const session = await getSession();
  session.adminId = admin.id_admin;
  session.adminUid = admin.uid_admin;
  session.firstName = admin.firstName;
  session.lastName = admin.lastName;
  await session.save();

  return { ok: true };
}

export async function logoutAdmin(): Promise<void> {
  const session = await getSession();
  session.destroy();
}
