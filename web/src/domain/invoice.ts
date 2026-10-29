import type { InvoiceConfig } from './config'

export type LineKind = 'labour' | 'material' | 'travel'

export interface ServiceLine {
  kind: LineKind
  description: string
  /** Amount excluding VAT, integer öre */
  amountOre: number
}

export interface InvoiceBasis {
  labourExVat: number
  materialExVat: number
  travelExVat: number
  vat: number
  labourInclVat: number
  rutAmount: number
  totalInclVat: number
  /** Öresavrundning: customerPays minus the unrounded amount (can be negative) */
  rounding: number
  /** Amount to pay, rounded to whole kronor */
  customerPays: number
}

const ORE_PER_KRONA = 100

/**
 * All amounts are integer öre.
 * Rules (docs/research/rut-and-integrations.md §1 "Amounts", "What must be separated"):
 * only labour qualifies; RUT = rutRate × labour incl. VAT; material and travel never qualify.
 * VAT is rounded half up to whole öre, on the total and on labour separately.
 * The amount to pay is rounded half up to whole kronor (öresavrundning); `rounding` holds the difference.
 * PROVISIONAL: RUT is rounded DOWN to whole kronor. To be confirmed against Begaran.xsd (v6).
 */
export function calculateInvoiceBasis(lines: ServiceLine[], config: InvoiceConfig): InvoiceBasis {
  const sum = (kind: LineKind) => lines.filter((l) => l.kind === kind).reduce((s, l) => s + l.amountOre, 0)
  const labourExVat = sum('labour')
  const materialExVat = sum('material')
  const travelExVat = sum('travel')
  const totalExVat = labourExVat + materialExVat + travelExVat

  const vat = Math.round(totalExVat * config.vatRate)
  const labourInclVat = labourExVat + Math.round(labourExVat * config.vatRate)
  const totalInclVat = totalExVat + vat

  const rutRaw = Math.floor((labourInclVat * config.rutRate) / ORE_PER_KRONA) * ORE_PER_KRONA
  const rutAmount = Math.max(0, Math.min(rutRaw, labourInclVat))
  const unrounded = Math.max(0, totalInclVat - rutAmount)
  const customerPays = Math.round(unrounded / ORE_PER_KRONA) * ORE_PER_KRONA
  const rounding = customerPays - unrounded

  return { labourExVat, materialExVat, travelExVat, vat, labourInclVat, rutAmount, totalInclVat, rounding, customerPays }
}
