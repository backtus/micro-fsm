import { describe, expect, it } from 'vitest'
import { defaultConfig } from './config'
import { invoiceLines, jobInvoiceBasis } from './jobs'
import type { Job } from './types'

const job = (over: Partial<Job>): Job =>
  ({ actualHours: 1, hourlyRateInclVatOre: 55000, materialOre: 0, travelOre: 0, ...over }) as Job

describe('job invoice lines', () => {
  it('converts 550 kr/h incl. VAT to 440 kr/h ex. VAT', () => {
    const [labour] = invoiceLines(job({ actualHours: 1 }), defaultConfig)
    expect(labour.kind).toBe('labour')
    expect(labour.amountOre).toBe(44000)
    expect(invoiceLines(job({ actualHours: 2.5 }), defaultConfig)[0].amountOre).toBe(110000)
  })

  it('keeps customerPays + rutAmount == totalInclVat + rounding with labour and öresavrundning', () => {
    for (const hours of [0.33, 1, 2.75, 3.1, 7.37]) {
      const b = jobInvoiceBasis(job({ actualHours: hours, materialOre: 12345, travelOre: 3333 }), defaultConfig)
      expect(b.rounding).not.toBe(undefined)
      expect(b.customerPays + b.rutAmount).toBe(b.totalInclVat + b.rounding)
    }
    const odd = jobInvoiceBasis(job({ actualHours: 1.17 }), defaultConfig)
    expect(odd.rounding).not.toBe(0)
    expect(odd.customerPays + odd.rutAmount).toBe(odd.totalInclVat + odd.rounding)
  })
})
