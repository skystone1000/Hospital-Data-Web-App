"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { followUpSchema, FollowUpInput } from "@/lib/validations";
import { FollowUp } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

function getLocalDateString() {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
}

interface FollowUpFormProps {
  patientId: string;
  followUpId?: number;
  defaultValues?: Partial<FollowUp>;
}

function Field({ label, error, children }: { label: string; error?: string; children: React.ReactNode }) {
  return (
    <div className="space-y-1.5">
      <Label>{label}</Label>
      {children}
      {error && <p className="text-xs text-destructive">{error}</p>}
    </div>
  );
}

export function FollowUpForm({ patientId, followUpId, defaultValues }: FollowUpFormProps) {
  const router = useRouter();
  const [serverError, setServerError] = useState("");
  const isEditing = !!followUpId;

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<FollowUpInput>({
    resolver: zodResolver(followUpSchema),
    defaultValues: {
      date: defaultValues?.date ?? getLocalDateString(),
      weight: defaultValues?.weight ?? "",
      treatment_output: defaultValues?.treatment_output ?? "",
      other_complains: defaultValues?.other_complains ?? "",
      treatment: defaultValues?.treatment ?? "",
      medicine_duration: defaultValues?.medicine_duration ?? "",
      paid: defaultValues?.paid ?? "",
      balance: defaultValues?.balance ?? "",
    },
  });

  const treatmentOutputVal = watch("treatment_output");
  const medicineDurationVal = watch("medicine_duration");

  async function onSubmit(data: FollowUpInput) {
    setServerError("");
    const url = isEditing
      ? `/api/patients/${patientId}/followups/${followUpId}`
      : `/api/patients/${patientId}/followups`;
    const method = isEditing ? "PUT" : "POST";
    try {
      const res = await fetch(url, {
        method,
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
          <Field label="Date" error={errors.date?.message}>
            <Input type="date" {...register("date")} />
          </Field>
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
          {isSubmitting ? "Saving…" : isEditing ? "Save Changes" : "Save Follow-up"}
        </Button>
        <Button type="button" variant="outline" onClick={() => router.back()}>
          Cancel
        </Button>
      </div>
    </form>
  );
}
