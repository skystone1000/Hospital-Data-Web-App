"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Activity } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loginSchema, LoginInput } from "@/lib/validations";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { ThemeToggle } from "@/components/layout/theme-toggle";

export default function LoginPage() {
  const router = useRouter();
  const [serverError, setServerError] = useState("");
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginInput>({ resolver: zodResolver(loginSchema) });

  async function onSubmit(data: LoginInput) {
    setServerError("");
    setLoading(true);
    try {
      const res = await fetch("/api/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
      if (res.ok) {
        router.push("/dashboard");
        router.refresh();
      } else {
        const json = await res.json();
        setServerError(json.error ?? "Login failed");
      }
    } catch {
      setServerError("Network error. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between bg-primary p-12">
        <div className="flex items-center gap-3">
          <Activity className="h-8 w-8 text-primary-foreground" />
          <span className="text-2xl font-bold text-primary-foreground tracking-tight">
            Mahajan Homeo Clinic
          </span>
        </div>
        <div className="space-y-4">
          <p className="text-4xl font-bold text-primary-foreground leading-tight">
            Patient Records
            <br />
            Management System
          </p>
          <p className="text-primary-foreground/70 text-lg">
            Manage patient records, follow-ups, and clinical history — all in one place.
          </p>
        </div>
        <p className="text-primary-foreground/50 text-sm">
          © {new Date().getFullYear()} Mahajan Homeo Clinic
        </p>
      </div>

      {/* Right panel — form */}
      <div className="flex-1 flex flex-col items-center justify-center p-8 bg-background relative">
        <div className="absolute top-4 right-4">
          <ThemeToggle />
        </div>

        {/* Mobile logo */}
        <div className="flex items-center gap-2 mb-8 lg:hidden">
          <Activity className="h-6 w-6 text-primary" />
          <span className="font-bold text-lg">Mahajan Homeo Clinic</span>
        </div>

        <Card className="w-full max-w-sm shadow-lg border-border/50">
          <CardHeader className="space-y-1">
            <CardTitle className="text-2xl font-bold">Sign in</CardTitle>
            <CardDescription>Enter your credentials to access the system</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              <div className="space-y-1.5">
                <Label htmlFor="identifier">Username or Email</Label>
                <Input
                  id="identifier"
                  placeholder="admin or admin@clinic.com"
                  autoComplete="username"
                  {...register("identifier")}
                />
                {errors.identifier && (
                  <p className="text-xs text-destructive">{errors.identifier.message}</p>
                )}
              </div>
              <div className="space-y-1.5">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  {...register("password")}
                />
                {errors.password && (
                  <p className="text-xs text-destructive">{errors.password.message}</p>
                )}
              </div>
              {serverError && (
                <p className="text-sm text-destructive rounded-md bg-destructive/10 px-3 py-2">
                  {serverError}
                </p>
              )}
              <Button type="submit" className="w-full" disabled={loading}>
                {loading ? "Signing in…" : "Sign in"}
              </Button>
            </form>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
