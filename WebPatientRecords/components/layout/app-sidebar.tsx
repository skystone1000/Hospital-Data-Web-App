"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  LayoutDashboard,
  Users,
  UserPlus,
  FileSpreadsheet,
  LogOut,
  Activity,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { ThemeToggle } from "./theme-toggle";

const navItems = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/patients", label: "Patients", icon: Users },
  { href: "/patients/new", label: "Add Patient", icon: UserPlus },
  { href: "/records", label: "Detailed Records", icon: FileSpreadsheet },
];

interface AppSidebarProps {
  doctorName: string;
}

export function AppSidebar({ doctorName }: AppSidebarProps) {
  const pathname = usePathname();

  return (
    <aside className="hidden md:flex flex-col w-64 shrink-0 border-r bg-sidebar h-screen sticky top-0">
      {/* Logo */}
      <div className="flex items-center gap-2 px-6 h-16 border-b border-sidebar-border">
        <Activity className="h-5 w-5 text-primary" />
        <span className="font-semibold text-sm text-sidebar-foreground">Mahajan Homeo</span>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
        {navItems.map((item) => {
          const active = pathname === item.href || pathname.startsWith(item.href + "/");
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors",
                active
                  ? "bg-sidebar-accent text-sidebar-accent-foreground"
                  : "text-sidebar-foreground hover:bg-sidebar-accent/50 hover:text-sidebar-accent-foreground"
              )}
            >
              <item.icon className="h-4 w-4 shrink-0" />
              {item.label}
            </Link>
          );
        })}
      </nav>

      {/* Footer */}
      <div className="px-4 py-4 border-t border-sidebar-border space-y-3">
        <div className="flex items-center gap-2">
          <div className="h-7 w-7 rounded-full bg-primary/10 flex items-center justify-center">
            <span className="text-xs font-semibold text-primary">
              {doctorName.charAt(0).toUpperCase()}
            </span>
          </div>
          <span className="text-xs text-sidebar-foreground truncate">Dr. {doctorName}</span>
        </div>
        <div className="flex items-center justify-between">
          <ThemeToggle />
          <form action="/api/auth/logout" method="POST">
            <button
              type="submit"
              className="flex items-center gap-1 text-xs text-muted-foreground hover:text-destructive transition-colors"
            >
              <LogOut className="h-3.5 w-3.5" />
              Logout
            </button>
          </form>
        </div>
      </div>
    </aside>
  );
}
