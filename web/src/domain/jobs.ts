import { defaultConfig, type InvoiceConfig } from './config'
import { calculateInvoiceBasis, type ServiceLine } from './invoice'
import { formatHours, formatSek } from './format'
import type { Job } from './types'

/** Labour ex VAT = actual hours × hourly price incl. VAT ÷ (1 + VAT rate), rounded to whole öre. */
export function invoiceLines(job: Job, config: InvoiceConfig = defaultConfig): ServiceLine[] {
  const hours = job.actualHours ?? 0
  return [
    {
      kind: 'labour',
      description: `Städning ${formatHours(hours)} à ${formatSek(job.hourlyRateInclVatOre)}/h inkl. moms`,
      amountOre: Math.round((hours * job.hourlyRateInclVatOre) / (1 + config.vatRate)),
    },
    { kind: 'material', description: 'Material och förbrukning', amountOre: job.materialOre },
    { kind: 'travel', description: 'Resa', amountOre: job.travelOre },
  ]
}

export function jobInvoiceBasis(job: Job, config: InvoiceConfig = defaultConfig) {
  return calculateInvoiceBasis(invoiceLines(job, config), config)
}

export function isOverdue(job: Job, today: string): boolean {
  return job.status === 'planned' && job.date < today
}
