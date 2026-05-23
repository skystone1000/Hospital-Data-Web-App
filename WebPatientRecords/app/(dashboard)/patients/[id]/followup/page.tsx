"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { followUpSchema, FollowUpInput } from "@/lib/validations";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}

export default function AddFollowUpPage({ params }: { params: { id: string } }) {
  const router = useRouter();
  const patientId = params.id;
  const [serverError, setServerError] = useState("");

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<FollowUpInput>({
    resolver: zodResolver(followUpSchema),
  });

  const treatmentOutputVal = watch("treatment_output");
  const medicineDurationVal = watch("medicine_duration");

  async function onSubmit(data: FollowUpInput) {
    setServerError("");
    try {
      const res = await fetch(`/api/patients/${patientId}/followups`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
      if (res.ok) {
        router.push(`/patients/${patientId}`);
        router.refresh();
      } else {
        const json = await res.json();
        setServerError(json.error ?? "Failed to save follow-up");
      }
    } catch {
      setServerError("Network error");
    }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-5 animate-fade-in">
      <div className="flex items-center gap-3">
        <Link href={`/patients/${patientId}`}>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Add Follow-up</h1>
          <p className="text-sm text-muted-foreground">Record a new follow-up visit.</p>
        </div>
      </div>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {serverError && (
          <div className="rounded-md bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
            {serverError}
          </div>
        )}

        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">
              Follow-up Details
            </CardTitle>
          </CardHeader>
          <CardContent className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <Field label="Weight (kg)" error={errors.weight?.message}>
              <Input {...register("weight")} placeholder="e.g. 68" />
            </Field>
            <Field label="Treatment Output" error={errors.treatment_output?.message}>
              <Select value={treatmentOutputVal ?? ""} onValueChange={(v) => setValue("treatment_output", v)}>
                <SelectTrigger><SelectValue placeholder="Select…" /></SelectTrigger>
                <SelectContent>
                  {["Much Better", "Better", "Same", "Worse", "Much Worse"].map((v) => (
                    <SelectItem key={v} value={v}>{v}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <div className="sm:col-span-2">
              <Field label="Other Complaints" error={errors.other_complains?.message}>
                <Textarea rows={2} {...register("other_complains")} />
              </Field>
            </div>
            <div className="sm:col-span-2">
              <Field label="Treatment" error={errors.treatment?.message}>
                <Textarea rows={3} {...register("treatment")} />
              </Field>
            </div>
            <Field label="Medicine Duration" error={errors.medicine_duration?.message}>
              <Select value={medicineDurationVal ?? ""} onValueChange={(v) => setValue("medicine_duration", v)}>
                <SelectTrigger><SelectValue placeholder="Select…" /></SelectTrigger>
                <SelectContent>
                  {["1 week", "2 weeks", "1 month", "2 months", "3 months", "6 months"].map((v) => (
                    <SelectItem key={v} value={v}>{v}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </Field>
            <Field label="Paid (₹)" error={errors.paid?.message}>
              <Input {...register("paid")} placeholder="0" />
            </Field>
            <Field label="Balance (₹)" error={errors.balance?.message}>
              <Input {...register("balance")} placeholder="0" />
            </Field>
          </CardContent>
        </Card>

        <div className="flex gap-3">
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? "Saving…" : "Save Follow-up"}
          </Button>
          <Button type="button" variant="outline" onClick={() => router.back()}>
            Cancel
          </Button>
        </div>
      </form>
    </div>
  );
}
