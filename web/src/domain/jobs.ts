import { defaultConfig, type InvoiceConfig } from './config'
import { calculateInvoiceBasis, type ServiceLine } from './invoice'
import { formatHours } from './format'
import type { Job } from './types'

export function invoiceLines(job: Job): ServiceLine[] {
  const hours = job.actualHours ?? 0
  return [
    { kind: 'labour', description: `Städning ${formatHours(hours)} à ${job.hourlyRateOre / 100} kr/h`, amountOre: Math.round(hours * job.hourlyRateOre) },
    { kind: 'material', description: 'Material och förbrukning', amountOre: job.materialOre },
    { kind: 'travel', description: 'Resa', amountOre: job.travelOre },
  ]
}

export function jobInvoiceBasis(job: Job, config: InvoiceConfig = defaultConfig) {
  return calculateInvoiceBasis(invoiceLines(job), config)
}

export function isOverdue(job: Job, today: string): boolean {
  return job.status === 'planned' && job.date < today
}
