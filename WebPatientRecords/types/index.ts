export interface Patient {
  id: number;
  firstName: string;
  middleName: string | null;
  lastName: string;
  age: number;
  sex: string | null;
  occupation: string | null;
  address: string | null;
  phone: string | null;
  regno: string | null;
  height: string | null;
  weight: string | null;
  diagnosis: string | null;
  cc1: string | null;
  cc2: string | null;
  cc3: string | null;
  appetite: string | null;
  desire: string | null;
  aversions: string | null;
  thirst: string | null;
  perspiration: string | null;
  sleep: string | null;
  stool: string | null;
  urine: string | null;
  menses: string | null;
  thermal: string | null;
  mind: string | null;
  hobbies: string | null;
  particulars: string | null;
  on_examination: string | null;
  path_inv: string | null;
  previous_rx: string | null;
  past_history: string | null;
  family_history: string | null;
  treatment: string | null;
  paid: string | null;
  balance: string | null;
  dateJoined: string | null;
  firebaseId: string | null;
  updatedAt: string | null;
}

export interface FollowUp {
  followUpId: number;
  id: number;
  date: string | null;
  regno: string | null;
  follow_up_num: number;
  weight: string | null;
  treatment_output: string | null;
  other_complains: string | null;
  treatment: string | null;
  medicine_duration: string | null;
  paid: string | null;
  balance: string | null;
  firebaseId: string | null;
  patientFirebaseId: string | null;
  updatedAt: string | null;
}

export interface DashboardStats {
  today: PeriodStats;
  week: PeriodStats;
  month: PeriodStats;
  year: PeriodStats;
}

export interface PeriodStats {
  newPatients: number;
  followUps: number;
  earnings: number;
}

export interface AdminUser {
  id_admin: number;
  uid_admin: string;
  firstName: string;
  lastName: string;
  email_admin: string;
  dateOfBirth: string | null;
  degree: string | null;
}

export interface PaginatedPatients {
  data: Patient[];
  total: number;
  page: number;
  perPage: number;
}
