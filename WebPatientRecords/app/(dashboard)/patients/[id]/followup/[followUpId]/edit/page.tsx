import { notFound } from "next/navigation";
import Link from "next/link";
import pool from "@/lib/db";
import { FollowUp } from "@/types";
import { FollowUpForm } from "@/components/patients/followup-form";
import { Button } from "@/components/ui/button";
import { ArrowLeft } from "lucide-react";

export default async function EditFollowUpPage({ params }: { params: { id: string; followUpId: string } }) {
  const id = parseInt(params.id);
  const followUpId = parseInt(params.followUpId);
  if (isNaN(id) || isNaN(followUpId)) notFound();

  const [[followUp]] = await pool.execute(
    "SELECT * FROM follow_up_data WHERE followUpId = ? AND id = ?",
    [followUpId, id]
  ) as any;
  if (!followUp) notFound();

  return (
    <div className="max-w-2xl mx-auto space-y-5 animate-fade-in">
      <div className="flex items-center gap-3">
        <Link href={`/patients/${id}`}>
          <Button variant="ghost" size="icon" className="h-8 w-8">
            <ArrowLeft className="h-4 w-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-2xl font-bold">Edit Follow-up #{followUp.follow_up_num}</h1>
          <p className="text-sm text-muted-foreground">Update the follow-up details.</p>
        </div>
      </div>
      <FollowUpForm patientId={params.id} followUpId={followUpId} defaultValues={followUp as FollowUp} />
    </div>
  );
}
