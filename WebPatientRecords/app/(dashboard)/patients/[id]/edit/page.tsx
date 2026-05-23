import { notFound } from "next/navigation";
import Link from "next/link";
import pool from "@/lib/db";
import { PatientForm } from "@/components/patients/patient-form";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";

export default async function EditPatientPage({ params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) notFound();

  const [[patient]] = await pool.execute("SELECT * FROM patient_data WHERE id = ?", [id]) as any;
  if (!patient) notFound();

  return (
    <div className="max-w-3xl mx-auto space-y-5 animate-fade-in">
      <div className="flex items-center gap-3">
        <Link href={`/patients/${id}`}>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Edit Patient</h1>
          <p className="text-sm text-muted-foreground">
            {patient.firstName} {patient.lastName}
          </p>
        </div>
      </div>
      <PatientForm defaultValues={patient} patientId={id} />
    </div>
  );
}
