import { type ClassValue, clsx } from "clsx";
import { twMerge } from "tailwind-merge";

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

/**
 * Returns "YYYY-MM-DD HH:MM:SS" for the given date in local timezone.
 * If `dateOnly` (YYYY-MM-DD) is provided, it's combined with the CURRENT local time
 * — used so the user-picked date gets a real timestamp when saved.
 */
export function buildLocalDateTime(dateOnly?: string | null): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  const time = `${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`;
  const date = dateOnly && /^\d{4}-\d{2}-\d{2}$/.test(dateOnly)
    ? dateOnly
    : `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`;
  return `${date} ${time}`;
}
