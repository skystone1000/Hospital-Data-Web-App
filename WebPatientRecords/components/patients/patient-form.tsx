"use client";

import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { patientSchema, PatientInput } from "@/lib/validations";
import { Patient } from "@/types";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Textarea } from "@/components/ui/textarea";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

interface PatientFormProps {
  defaultValues?: Partial<Patient>;
  patientId?: number;
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

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <Card>
      <CardHeader className="pb-3">
        <CardTitle className="text-sm font-semibold text-muted-foreground uppercase tracking-wide">{title}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">{children}</div>
      </CardContent>
    </Card>
  );
}

export function PatientForm({ defaultValues, patientId }: PatientFormProps) {
  const router = useRouter();
  const [serverError, setServerError] = useState("");
  const isEditing = !!patientId;

  const { register, handleSubmit, setValue, watch, formState: { errors, isSubmitting } } = useForm<PatientInput>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(patientSchema) as any,
    defaultValues: {
      firstName: defaultValues?.firstName ?? "",
      middleName: defaultValues?.middleName ?? "",
      lastName: defaultValues?.lastName ?? "",
      age: defaultValues?.age ?? 0,
      sex: defaultValues?.sex ?? "",
      occupation: defaultValues?.occupation ?? "",
      address: defaultValues?.address ?? "",
      phone: defaultValues?.phone ?? "",
      regno: defaultValues?.regno ?? "",
      height: defaultValues?.height ?? "",
      weight: defaultValues?.weight ?? "",
      diagnosis: defaultValues?.diagnosis ?? "",
      cc1: defaultValues?.cc1 ?? "",
      cc2: defaultValues?.cc2 ?? "",
      cc3: defaultValues?.cc3 ?? "",
      appetite: defaultValues?.appetite ?? "",
      desire: defaultValues?.desire ?? "",
      aversions: defaultValues?.aversions ?? "",
      thirst: defaultValues?.thirst ?? "",
      perspiration: defaultValues?.perspiration ?? "",
      sleep: defaultValues?.sleep ?? "",
      stool: defaultValues?.stool ?? "",
      urine: defaultValues?.urine ?? "",
      menses: defaultValues?.menses ?? "",
      thermal: defaultValues?.thermal ?? "",
      mind: defaultValues?.mind ?? "",
      hobbies: defaultValues?.hobbies ?? "",
      particulars: defaultValues?.particulars ?? "",
      on_examination: defaultValues?.on_examination ?? "",
      path_inv: defaultValues?.path_inv ?? "",
      previous_rx: defaultValues?.previous_rx ?? "",
      past_history: defaultValues?.past_history ?? "",
      family_history: defaultValues?.family_history ?? "",
      treatment: defaultValues?.treatment ?? "",
      paid: defaultValues?.paid ?? "",
      balance: defaultValues?.balance ?? "",
      dateJoined: defaultValues?.dateJoined?.slice(0, 10) ?? new Date().toISOString().slice(0, 10),
    },
  });

  async function onSubmit(data: PatientInput) {
    setServerError("");
    const url = isEditing ? `/api/patients/${patientId}` : "/api/patients";
    const method = isEditing ? "PUT" : "POST";
    try {
      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
      });
      if (res.ok) {
        const json = await res.json();
        router.push(isEditing ? `/patients/${patientId}` : `/patients/${json.id}`);
        router.refresh();
      } else {
        const json = await res.json();
        setServerError(json.error ?? "Failed to save patient");
      }
    } catch {
      setServerError("Network error");
    }
  }

  const sexVal = watch("sex");

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      {serverError && (
        <div className="rounded-md bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
          {serverError}
        </div>
      )}

      <Section title="Personal Information">
        <Field label="First Name *" error={errors.firstName?.message}>
          <Input {...register("firstName")} />
        </Field>
        <Field label="Middle Name" error={errors.middleName?.message}>
          <Input {...register("middleName")} />
        </Field>
        <Field label="Last Name *" error={errors.lastName?.message}>
          <Input {...register("lastName")} />
        </Field>
        <Field label="Age *" error={errors.age?.message}>
          <Input type="number" min={0} max={150} {...register("age")} />
        </Field>
        <Field label="Sex" error={errors.sex?.message}>
          <Select value={sexVal ?? ""} onValueChange={(v) => setValue("sex", v)}>
            <SelectTrigger><SelectValue placeholder="Select…" /></SelectTrigger>
            <SelectContent>
              <SelectItem value="Male">Male</SelectItem>
              <SelectItem value="Female">Female</SelectItem>
              <SelectItem value="Other">Other</SelectItem>
            </SelectContent>
          </Select>
        </Field>
        <Field label="Occupation" error={errors.occupation?.message}>
          <Input {...register("occupation")} />
        </Field>
        <Field label="Date Joined" error={errors.dateJoined?.message}>
          <Input type="date" {...register("dateJoined")} />
        </Field>
      </Section>

      <Section title="Contact & Registration">
        <Field label="Address" error={errors.address?.message}>
          <Input {...register("address")} />
        </Field>
        <Field label="Phone" error={errors.phone?.message}>
          <Input type="tel" {...register("phone")} />
        </Field>
        <Field label="Registration Number" error={errors.regno?.message}>
          <Input {...register("regno")} />
        </Field>
      </Section>

      <Section title="Vitals & Diagnosis">
        <Field label="Height (cm)" error={errors.height?.message}>
          <Input {...register("height")} />
        </Field>
        <Field label="Weight (kg)" error={errors.weight?.message}>
          <Input {...register("weight")} />
        </Field>
        <div className="md:col-span-2">
          <Field label="Clinical Diagnosis" error={errors.diagnosis?.message}>
            <Textarea rows={2} {...register("diagnosis")} />
          </Field>
        </div>
      </Section>

      <Section title="Chief Complaints">
        <Field label="Chief Complaint 1" error={errors.cc1?.message}>
          <Input {...register("cc1")} />
        </Field>
        <Field label="Chief Complaint 2" error={errors.cc2?.message}>
          <Input {...register("cc2")} />
        </Field>
        <Field label="Chief Complaint 3" error={errors.cc3?.message}>
          <Input {...register("cc3")} />
        </Field>
      </Section>

      <Section title="Homeopathic History">
        {[
          ["appetite", "Appetite"], ["desire", "Desire"], ["aversions", "Aversions"],
          ["thirst", "Thirst"], ["perspiration", "Perspiration"], ["sleep", "Sleep"],
          ["stool", "Stool"], ["urine", "Urine"], ["menses", "Menses"],
          ["thermal", "Thermal"], ["mind", "Mind"],
        ].map(([field, label]) => (
          <div key={field} className="md:col-span-2">
            <Field label={label}>
              <Textarea rows={2} {...register(field as keyof PatientInput)} />
            </Field>
          </div>
        ))}
      </Section>

      <Section title="Case Particulars">
        {[
          ["hobbies", "Hobbies"], ["particulars", "Particulars"],
          ["on_examination", "On Examination"], ["path_inv", "Pathological Investigations"],
          ["previous_rx", "Previous Rx"], ["past_history", "Past History"],
          ["family_history", "Family History"],
        ].map(([field, label]) => (
          <div key={field} className="md:col-span-2">
            <Field label={label}>
              <Textarea rows={2} {...register(field as keyof PatientInput)} />
            </Field>
          </div>
        ))}
      </Section>

      <Section title="Treatment & Billing">
        <div className="md:col-span-2">
          <Field label="Treatment" error={errors.treatment?.message}>
            <Textarea rows={3} {...register("treatment")} />
          </Field>
        </div>
        <Field label="Paid (₹)" error={errors.paid?.message}>
          <Input {...register("paid")} />
        </Field>
        <Field label="Balance (₹)" error={errors.balance?.message}>
          <Input {...register("balance")} />
        </Field>
      </Section>

      <div className="flex gap-3 sticky bottom-4">
        <Button type="submit" disabled={isSubmitting} className="shadow-lg">
          {isSubmitting ? "Saving…" : isEditing ? "Save Changes" : "Add Patient"}
        </Button>
        <Button type="button" variant="outline" onClick={() => router.back()} className="shadow-lg">
          Cancel
        </Button>
      </div>
    </form>
  );
}
