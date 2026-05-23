import Link from "next/link";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";
import { FollowUpForm } from "@/components/patients/followup-form";

export default function AddFollowUpPage({ params }: { params: { id: string } }) {
  return (
    <div className="max-w-2xl mx-auto space-y-5 animate-fade-in">
      <div className="flex items-center gap-3">
        <Link href={`/patients/${params.id}`}>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Add Follow-up</h1>
          <p className="text-sm text-muted-foreground">Record a new follow-up visit.</p>
        </div>
      </div>
      <FollowUpForm patientId={params.id} />
    </div>
  );
}
