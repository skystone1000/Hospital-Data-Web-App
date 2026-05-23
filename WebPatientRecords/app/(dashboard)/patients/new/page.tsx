import { PatientForm } from "@/components/patients/patient-form";
import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { Button } from "@/components/ui/button";

export default function NewPatientPage() {
  return (
    <div className="max-w-3xl mx-auto space-y-5 animate-fade-in">
      <div className="flex items-center gap-3">
        <Link href="/patients">
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">New Patient</h1>
          <p className="text-sm text-muted-foreground">Fill in the patient details below.</p>
        </div>
      </div>
      <PatientForm />
    </div>
  );
}
