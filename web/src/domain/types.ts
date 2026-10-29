export interface Company {
  name: string
}
export interface Staff {
  id: string
  name: string
}
export interface Customer {
  id: string
  name: string
  street: string
  city: string
  accessNote: string
}
export type JobStatus = 'planned' | 'done'
export interface Job {
  id: string
  customerId: string
  /** null = unassigned */
  staffId: string | null
  /** Local date, YYYY-MM-DD */
  date: string
  /** HH:MM */
  startTime: string
  plannedHours: number
  tasks: string[]
  status: JobStatus
  actualHours?: number
  note?: string
  /** Consumer price per hour INCLUDING VAT, öre (prisinformationslagen: households see prices incl. moms) */
  hourlyRateInclVatOre: number
  materialOre: number
  travelOre: number
}
export interface Data {
  company: Company
  staff: Staff[]
  customers: Customer[]
  jobs: Job[]
  /** Date the seed was generated; used to regenerate when stale */
  generatedOn: string
}
