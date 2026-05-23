import { z } from "zod";

export const loginSchema = z.object({
  identifier: z.string().min(1, "Required"),
  password: z.string().min(1, "Required"),
});

const optStr = z.string().optional();

export const patientSchema = z.object({
  firstName: z.string().min(1, "First name is required"),
  middleName: optStr,
  lastName: z.string().min(1, "Last name is required"),
  age: z.coerce.number().int().min(0).max(150),
  sex: optStr,
  occupation: optStr,
  address: optStr,
  phone: optStr,
  regno: optStr,
  height: optStr,
  weight: optStr,
  diagnosis: optStr,
  cc1: optStr,
  cc2: optStr,
  cc3: optStr,
  appetite: optStr,
  desire: optStr,
  aversions: optStr,
  thirst: optStr,
  perspiration: optStr,
  sleep: optStr,
  stool: optStr,
  urine: optStr,
  menses: optStr,
  thermal: optStr,
  mind: optStr,
  hobbies: optStr,
  particulars: optStr,
  on_examination: optStr,
  path_inv: optStr,
  previous_rx: optStr,
  past_history: optStr,
  family_history: optStr,
  treatment: optStr,
  paid: optStr,
  balance: optStr,
  dateJoined: optStr,
});

export const followUpSchema = z.object({
  date: optStr,
  weight: optStr,
  treatment_output: optStr,
  other_complains: optStr,
  treatment: optStr,
  medicine_duration: optStr,
  paid: optStr,
  balance: optStr,
});

export type LoginInput = z.infer<typeof loginSchema>;
export type PatientInput = z.infer<typeof patientSchema>;
export type FollowUpInput = z.infer<typeof followUpSchema>;
