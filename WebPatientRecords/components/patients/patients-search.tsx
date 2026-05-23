"use client";

import { useRouter, usePathname, useSearchParams } from "next/navigation";
import { useRef, useState, useEffect } from "react";
import { Search, X, SlidersHorizontal } from "lucide-react";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Label } from "@/components/ui/label";

const SORT_OPTIONS = [
  { value: "dateJoined", label: "Date Joined" },
  { value: "firstName",  label: "First Name" },
  { value: "lastName",   label: "Last Name" },
  { value: "regno",      label: "Reg. Number" },
  { value: "diagnosis",  label: "Diagnosis" },
];

export function PatientsSearch({ defaultSearch }: { defaultSearch: string }) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const isFirstRender = useRef(true);

  // Local state only for the debounced text search
  const [search, setSearch] = useState(defaultSearch);
  const [showPanel, setShowPanel] = useState(false);

  // Read all filter/sort state directly from URL — no local state, no sync bugs
  const currentSex = searchParams.get("sex") ?? "";
  const currentSort = searchParams.get("sort") ?? "dateJoined";
  const currentOrder = searchParams.get("order") ?? "desc";
  const currentDateFrom = searchParams.get("dateFrom") ?? "";
  const currentDateTo = searchParams.get("dateTo") ?? "";

  const hasActiveFilters = !!(currentSex || currentDateFrom || currentDateTo);
  const hasNonDefaultSort = currentSort !== "dateJoined" || currentOrder !== "desc";

  // Reveal panel automatically when filters/sort are active on page load
  useEffect(() => {
    if (hasActiveFilters || hasNonDefaultSort) setShowPanel(true);
  }, []);

  /** Build a new URL from current params + overrides and push it */
  function navigate(overrides: Record<string, string>) {
    const sp = new URLSearchParams(typeof window !== "undefined" ? window.location.search : searchParams.toString());
    Object.entries(overrides).forEach(([k, v]) => {
      v ? sp.set(k, v) : sp.delete(k);
    });
    sp.set("page", "1");
    router.push(`${pathname}?${sp}`);
  }

  // Debounced search — skip on initial mount
  useEffect(() => {
    if (isFirstRender.current) {
      isFirstRender.current = false;
      return;
    }
    const timeout = setTimeout(() => {
      navigate({ search });
    }, 350);
    return () => clearTimeout(timeout);
  }, [search]);

  function clearAll() {
    setSearch("");
    router.push(`${pathname}?page=1`);
  }

  return (
    <div className="space-y-3">
      <div className="flex items-center gap-2 flex-wrap">
        {/* Text search */}
        <div className="relative flex-1 min-w-48 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="Search by name or reg no…"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="pl-9 pr-9"
          />
          {search && (
            <Button
              variant="ghost" size="icon"
              className="absolute right-1 top-1/2 -translate-y-1/2 h-7 w-7"
              onClick={() => setSearch("")}
            >
              <X className="h-3.5 w-3.5" />
            </Button>
          )}
        </div>

        {/* Single Sort & Filters toggle */}
        <Button
          variant={showPanel ? "secondary" : "outline"}
          size="sm" className="gap-1.5 h-9"
          onClick={() => setShowPanel((v) => !v)}
        >
          <SlidersHorizontal className="h-3.5 w-3.5" />
          Sort & Filters
          {(hasActiveFilters || hasNonDefaultSort) && (
            <span className="h-2 w-2 rounded-full bg-primary inline-block" />
          )}
        </Button>

        {(hasActiveFilters || hasNonDefaultSort || search) && (
          <Button variant="ghost" size="sm" className="text-muted-foreground h-9 gap-1" onClick={clearAll}>
            <X className="h-3.5 w-3.5" /> Clear all
          </Button>
        )}
      </div>

      {/* Unified collapsible panel: Sort + Filters */}
      {showPanel && (
        <div className="rounded-lg border bg-muted/30 p-4 space-y-4">
          {/* Sort section */}
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">Sort by</p>
            <div className="flex flex-wrap gap-4 items-end">
              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">Field</Label>
                <Select value={currentSort} onValueChange={(v) => navigate({ sort: v })}>
                  <SelectTrigger className="w-44 h-9">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {SORT_OPTIONS.map((o) => (
                      <SelectItem key={o.value} value={o.value}>{o.label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">Order</Label>
                <Select value={currentOrder} onValueChange={(v) => navigate({ order: v })}>
                  <SelectTrigger className="w-40 h-9">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="desc">Descending (↓)</SelectItem>
                    <SelectItem value="asc">Ascending (↑)</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>
          </div>

          {/* Divider */}
          <div className="border-t border-border" />

          {/* Filters section */}
          <div>
            <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wide mb-2">Filters</p>
            <div className="flex flex-wrap gap-4 items-end">
              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">Sex</Label>
                <Select value={currentSex || "all"} onValueChange={(v) => navigate({ sex: v === "all" ? "" : v })}>
                  <SelectTrigger className="w-36 h-9">
                    <SelectValue placeholder="All sexes" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="all">All sexes</SelectItem>
                    <SelectItem value="Male">Male</SelectItem>
                    <SelectItem value="Female">Female</SelectItem>
                    <SelectItem value="Other">Other</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">Date joined from</Label>
                <Input
                  type="date" value={currentDateFrom} className="w-40 h-9"
                  onChange={(e) => navigate({ dateFrom: e.target.value })}
                />
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs text-muted-foreground">Date joined to</Label>
                <Input
                  type="date" value={currentDateTo} className="w-40 h-9"
                  onChange={(e) => navigate({ dateTo: e.target.value })}
                />
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
