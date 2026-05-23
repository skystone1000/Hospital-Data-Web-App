import { Suspense } from "react";
import Link from "next/link";
import pool from "@/lib/db";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import { UserPlus, ChevronLeft, ChevronRight, Eye, UserRoundPlus, Trash2 } from "lucide-react";
import { DeletePatientButton } from "@/components/patients/delete-patient-button";
import { PatientsSearch } from "@/components/patients/patients-search";

const PER_PAGE = 20;
const SORT_WHITELIST = ["firstName", "lastName", "dateJoined", "regno", "diagnosis"] as const;

async function getPatients(search: string, page: number, sort: string, order: string) {
  const safeSort = SORT_WHITELIST.includes(sort as any) ? sort : "dateJoined";
  const safeOrder = order === "asc" ? "ASC" : "DESC";
  const offset = (page - 1) * PER_PAGE;

  let where = "1=1";
  const params: string[] = [];
  if (search) {
    where = "(firstName LIKE ? OR lastName LIKE ? OR regno LIKE ?)";
    const t = `%${search}%`;
    params.push(t, t, t);
  }

  const [[{ total }]] = await pool.execute(
    `SELECT COUNT(*) AS total FROM patient_data WHERE ${where}`, params
  ) as any;

  const [rows] = await pool.execute(
    `SELECT id, firstName, middleName, lastName, age, sex, phone, address, regno,
            diagnosis, dateJoined, paid, balance, treatment
     FROM patient_data WHERE ${where}
     ORDER BY ${safeSort} ${safeOrder}
     LIMIT ${PER_PAGE} OFFSET ${offset}`,
    params
  ) as any;

  return { rows, total: Number(total) };
}

interface PageProps {
  searchParams: { search?: string; page?: string; sort?: string; order?: string };
}

export default async function PatientsPage({ searchParams }: PageProps) {
  const search = searchParams.search ?? "";
  const page = Math.max(1, Number(searchParams.page ?? 1));
  const sort = searchParams.sort ?? "dateJoined";
  const order = searchParams.order ?? "desc";
  const { rows, total } = await getPatients(search, page, sort, order);
  const totalPages = Math.ceil(total / PER_PAGE);

  function sortLink(col: string) {
    const newOrder = sort === col && order === "desc" ? "asc" : "desc";
    const sp = new URLSearchParams({ sort: col, order: newOrder, page: "1" });
    if (search) sp.set("search", search);
    return `?${sp}`;
  }

  function SortHeader({ col, label }: { col: string; label: string }) {
    const active = sort === col;
    return (
      <Link href={sortLink(col)} className={`flex items-center gap-1 hover:text-primary transition-colors ${active ? "text-primary font-semibold" : ""}`}>
        {label}
        {active && <span className="text-xs">{order === "desc" ? "↓" : "↑"}</span>}
      </Link>
    );
  }

  return (
    <div className="space-y-5 animate-fade-in">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Patients</h1>
          <p className="text-sm text-muted-foreground mt-0.5">{total} total records</p>
        </div>
        <Link href="/patients/new">
          <Button size="sm" className="gap-2">
            <UserPlus className="h-4 w-4" />
            Add Patient
          </Button>
        </Link>
      </div>

      <PatientsSearch defaultValue={search} />

      <div className="rounded-lg border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 border-b">
              <tr>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground">
                  <SortHeader col="firstName" label="Name" />
                </th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground hidden sm:table-cell">
                  <SortHeader col="regno" label="Reg No" />
                </th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground hidden md:table-cell">
                  <SortHeader col="dateJoined" label="Date Joined" />
                </th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground hidden lg:table-cell">
                  Phone
                </th>
                <th className="text-left px-4 py-3 font-medium text-muted-foreground hidden lg:table-cell">
                  <SortHeader col="diagnosis" label="Diagnosis" />
                </th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Paid</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Balance</th>
                <th className="text-right px-4 py-3 font-medium text-muted-foreground">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y">
              {rows.length === 0 && (
                <tr>
                  <td colSpan={8} className="text-center py-12 text-muted-foreground">
                    {search ? "No patients found for your search." : "No patients yet."}
                  </td>
                </tr>
              )}
              {rows.map((p: any) => (
                <tr key={p.id} className="hover:bg-muted/30 transition-colors">
                  <td className="px-4 py-3">
                    <div className="font-medium">{p.firstName} {p.lastName}</div>
                    <div className="text-xs text-muted-foreground sm:hidden">{p.regno}</div>
                  </td>
                  <td className="px-4 py-3 text-muted-foreground hidden sm:table-cell">{p.regno}</td>
                  <td className="px-4 py-3 text-muted-foreground hidden md:table-cell">{p.dateJoined}</td>
                  <td className="px-4 py-3 text-muted-foreground hidden lg:table-cell">{p.phone}</td>
                  <td className="px-4 py-3 hidden lg:table-cell">
                    {p.diagnosis && (
                      <Badge variant="secondary" className="text-xs max-w-[150px] truncate">
                        {p.diagnosis}
                      </Badge>
                    )}
                  </td>
                  <td className="px-4 py-3 text-right font-medium">₹{p.paid ?? 0}</td>
                  <td className="px-4 py-3 text-right text-muted-foreground">₹{p.balance ?? 0}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1">
                      <Link href={`/patients/${p.id}`}>
                        <Button variant="ghost" size="icon" className="h-8 w-8" title="View">
                          <Eye className="h-3.5 w-3.5" />
                        </Button>
                      </Link>
                      <Link href={`/patients/${p.id}/followup`}>
                        <Button variant="ghost" size="icon" className="h-8 w-8" title="Add Follow-up">
                          <UserRoundPlus className="h-3.5 w-3.5" />
                        </Button>
                      </Link>
                      <DeletePatientButton patientId={p.id} patientName={`${p.firstName} ${p.lastName}`} />
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-between text-sm">
          <span className="text-muted-foreground">
            Page {page} of {totalPages} ({total} records)
          </span>
          <div className="flex gap-2">
            {page > 1 && (
              <Link href={`?page=${page - 1}&sort=${sort}&order=${order}${search ? `&search=${search}` : ""}`}>
                <Button variant="outline" size="sm" className="gap-1">
                  <ChevronLeft className="h-3.5 w-3.5" /> Previous
                </Button>
              </Link>
            )}
            {page < totalPages && (
              <Link href={`?page=${page + 1}&sort=${sort}&order=${order}${search ? `&search=${search}` : ""}`}>
                <Button variant="outline" size="sm" className="gap-1">
                  Next <ChevronRight className="h-3.5 w-3.5" />
                </Button>
              </Link>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
