import { notFound } from "next/navigation";
import Link from "next/link";
import pool from "@/lib/db";
import { Patient, FollowUp } from "@/types";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { Accordion, AccordionContent, AccordionItem, AccordionTrigger } from "@/components/ui/accordion";
import { ArrowLeft, Pencil, UserRoundPlus, Phone, MapPin, Calendar, Hash } from "lucide-react";
import { DeletePatientButton } from "@/components/patients/delete-patient-button";

async function getPatient(id: number) {
  const [[patient]] = await pool.execute("SELECT * FROM patient_data WHERE id = ?", [id]) as any;
  if (!patient) return null;
  const [followUps] = await pool.execute(
    "SELECT * FROM follow_up_data WHERE id = ? ORDER BY CAST(follow_up_num AS UNSIGNED) DESC",
    [id]
  ) as any;
  return { patient: patient as Patient, followUps: followUps as FollowUp[] };
}

function DetailRow({ label, value }: { label: string; value?: string | number | null }) {
  if (!value) return null;
  return (
    <div className="space-y-0.5">
      <p className="text-xs text-muted-foreground font-medium uppercase tracking-wide">{label}</p>
      <p className="text-sm">{value}</p>
    </div>
  );
}

export default async function PatientDetailsPage({ params }: { params: { id: string } }) {
  const id = parseInt(params.id);
  if (isNaN(id)) notFound();
  const data = await getPatient(id);
  if (!data) notFound();
  const { patient: p, followUps } = data;

  return (
    <div className="max-w-4xl mx-auto space-y-5 animate-fade-in">
      {/* Header */}
      <div className="flex items-start justify-between gap-4">
        <div className="flex items-center gap-3">
          <Link href="/patients">
            <Button variant="ghost" size="icon" className="h-8 w-8">
              <ArrowLeft className="h-4 w-4" />
            </Button>
          </Link>
          <div>
            <h1 className="text-2xl font-bold">{p.firstName} {p.middleName ? `${p.middleName} ` : ""}{p.lastName}</h1>
            <div className="flex items-center gap-3 mt-1 text-sm text-muted-foreground">
              {p.regno && <span className="flex items-center gap-1"><Hash className="h-3 w-3" />{p.regno}</span>}
              {p.dateJoined && <span className="flex items-center gap-1"><Calendar className="h-3 w-3" />{p.dateJoined}</span>}
              {p.phone && <span className="flex items-center gap-1"><Phone className="h-3 w-3" />{p.phone}</span>}
            </div>
          </div>
        </div>
        <div className="flex items-center gap-2 shrink-0">
          <Link href={`/patients/${id}/followup`}>
            <Button size="sm" variant="outline" className="gap-2">
              <UserRoundPlus className="h-4 w-4" />
              <span className="hidden sm:inline">Add Follow-up</span>
            </Button>
          </Link>
          <Link href={`/patients/${id}/edit`}>
            <Button size="sm" variant="outline" className="gap-2">
              <Pencil className="h-4 w-4" />
              <span className="hidden sm:inline">Edit</span>
            </Button>
          </Link>
          <DeletePatientButton patientId={id} patientName={`${p.firstName} ${p.lastName}`} />
        </div>
      </div>

      {/* Summary card */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {[
          { label: "Age", value: p.age ? `${p.age} yrs` : null },
          { label: "Sex", value: p.sex },
          { label: "Paid", value: p.paid ? `₹${p.paid}` : null },
          { label: "Balance", value: p.balance ? `₹${p.balance}` : null },
        ].map(({ label, value }) => value ? (
          <Card key={label} className="text-center py-3">
            <p className="text-xs text-muted-foreground uppercase tracking-wide">{label}</p>
            <p className="text-lg font-semibold mt-1">{value}</p>
          </Card>
        ) : null)}
      </div>

      {/* Follow-ups accordion */}
      <Card>
        <CardHeader className="flex flex-row items-center justify-between pb-3">
          <CardTitle className="text-base">Follow-ups ({followUps.length})</CardTitle>
          <Link href={`/patients/${id}/followup`}>
            <Button size="sm" variant="ghost" className="gap-1 text-primary h-7 px-2">
              <UserRoundPlus className="h-3.5 w-3.5" /> Add
            </Button>
          </Link>
        </CardHeader>
        <CardContent>
          {followUps.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-4">No follow-ups yet.</p>
          ) : (
            <Accordion type="multiple" className="space-y-1">
              {followUps.map((f) => (
                <AccordionItem key={f.followUpId} value={String(f.followUpId)} className="border rounded-md px-3">
                  <AccordionTrigger className="text-sm hover:no-underline py-3">
                    <div className="flex items-center gap-3">
                      <Badge variant="secondary" className="text-xs">#{f.follow_up_num}</Badge>
                      <span>{f.date}</span>
                      {f.treatment_output && <span className="text-muted-foreground text-xs hidden sm:inline">— {f.treatment_output}</span>}
                    </div>
                  </AccordionTrigger>
                  <AccordionContent>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-3 pb-2 text-sm">
                      {f.weight && <div><span className="text-muted-foreground text-xs">Weight</span><p>{f.weight} kg</p></div>}
                      {f.treatment_output && <div><span className="text-muted-foreground text-xs">Treatment Output</span><p>{f.treatment_output}</p></div>}
                      {f.medicine_duration && <div><span className="text-muted-foreground text-xs">Medicine Duration</span><p>{f.medicine_duration}</p></div>}
                      {f.paid && <div><span className="text-muted-foreground text-xs">Paid</span><p className="font-medium text-primary">₹{f.paid}</p></div>}
                      {f.balance && <div><span className="text-muted-foreground text-xs">Balance</span><p>₹{f.balance}</p></div>}
                      {f.other_complains && <div className="col-span-full"><span className="text-muted-foreground text-xs">Other Complaints</span><p>{f.other_complains}</p></div>}
                      {f.treatment && <div className="col-span-full"><span className="text-muted-foreground text-xs">Treatment</span><p>{f.treatment}</p></div>}
                    </div>
                  </AccordionContent>
                </AccordionItem>
              ))}
            </Accordion>
          )}
        </CardContent>
      </Card>

      {/* Initial details accordion */}
      <Card>
        <CardHeader className="pb-0">
          <Accordion type="single" collapsible>
            <AccordionItem value="details" className="border-0">
              <AccordionTrigger className="py-4 hover:no-underline">
                <CardTitle className="text-base">Initial Patient Details</CardTitle>
              </AccordionTrigger>
              <AccordionContent>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-4 pb-4">
                  <DetailRow label="Occupation" value={p.occupation} />
                  <DetailRow label="Address" value={p.address} />
                  <DetailRow label="Height" value={p.height} />
                  <DetailRow label="Weight" value={p.weight} />
                  <DetailRow label="Diagnosis" value={p.diagnosis} />
                  <DetailRow label="CC1" value={p.cc1} />
                  <DetailRow label="CC2" value={p.cc2} />
                  <DetailRow label="CC3" value={p.cc3} />
                  <Separator className="col-span-full" />
                  <DetailRow label="Appetite" value={p.appetite} />
                  <DetailRow label="Desire" value={p.desire} />
                  <DetailRow label="Aversions" value={p.aversions} />
                  <DetailRow label="Thirst" value={p.thirst} />
                  <DetailRow label="Perspiration" value={p.perspiration} />
                  <DetailRow label="Sleep" value={p.sleep} />
                  <DetailRow label="Stool" value={p.stool} />
                  <DetailRow label="Urine" value={p.urine} />
                  <DetailRow label="Menses" value={p.menses} />
                  <DetailRow label="Thermal" value={p.thermal} />
                  <DetailRow label="Mind" value={p.mind} />
                  <Separator className="col-span-full" />
                  <DetailRow label="Hobbies" value={p.hobbies} />
                  <DetailRow label="Particulars" value={p.particulars} />
                  <DetailRow label="On Examination" value={p.on_examination} />
                  <DetailRow label="Path. Investigations" value={p.path_inv} />
                  <DetailRow label="Previous Rx" value={p.previous_rx} />
                  <DetailRow label="Past History" value={p.past_history} />
                  <DetailRow label="Family History" value={p.family_history} />
                  <Separator className="col-span-full" />
                  <DetailRow label="Treatment" value={p.treatment} />
                </div>
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        </CardHeader>
      </Card>
    </div>
  );
}
