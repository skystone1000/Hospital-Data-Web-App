import { redirect } from "next/navigation";
import { getSession } from "@/lib/session";
import { AppSidebar } from "@/components/layout/app-sidebar";
import { AppNavbar } from "@/components/layout/app-navbar";

export default async function DashboardLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  if (!session.adminId) redirect("/login");

  const doctorName = `${session.firstName ?? ""} ${session.lastName ?? ""}`.trim();

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <AppSidebar doctorName={doctorName} />
      <div className="flex-1 flex flex-col overflow-hidden">
        <AppNavbar doctorName={doctorName} />
        <main className="flex-1 overflow-y-auto p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
