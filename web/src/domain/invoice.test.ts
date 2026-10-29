import { describe, expect, it } from 'vitest'
import { defaultConfig } from './config'
import { formatSek } from './format'
import { calculateInvoiceBasis, type ServiceLine } from './invoice'

const L = (kind: ServiceLine['kind'], kr: number): ServiceLine => ({ kind, description: kind, amountOre: Math.round(kr * 100) })

describe('calculateInvoiceBasis', () => {
  it("matches Skatteverket's worked example (research §1 'Invoice contents')", () => {
    const b = calculateInvoiceBasis([L('labour', 2600), L('material', 100), L('travel', 300)], defaultConfig)
    expect(b).toEqual({
      labourExVat: 260000, materialExVat: 10000, travelExVat: 30000,
      vat: 75000, labourInclVat: 325000, rutAmount: 162500, totalInclVat: 375000, customerPays: 212500,
    })
  })

  it('labour only: RUT is 50 % of labour incl. VAT (research §1 "Amounts")', () => {
    const b = calculateInvoiceBasis([L('labour', 1000)], defaultConfig)
    expect(b.labourInclVat).toBe(125000)
    expect(b.rutAmount).toBe(62500)
    expect(b.customerPays).toBe(62500)
  })

  it('no labour means no RUT; material and travel never qualify (research §1 "What must be separated")', () => {
    const b = calculateInvoiceBasis([L('material', 400), L('travel', 200)], defaultConfig)
    expect(b.rutAmount).toBe(0)
    expect(b.totalInclVat).toBe(75000)
    expect(b.customerPays).toBe(75000)
  })

  it('empty input gives all zeros', () => {
    expect(calculateInvoiceBasis([], defaultConfig).customerPays).toBe(0)
  })

  it('rounds VAT half up to öre and RUT down to whole kronor (provisional, to confirm against Begaran.xsd; research §2 "File import")', () => {
    const b = calculateInvoiceBasis([{ kind: 'labour', description: '', amountOre: 10033 }], defaultConfig)
    expect(b.vat).toBe(2508) // 2508.25
    expect(b.labourInclVat).toBe(12541)
    expect(b.rutAmount).toBe(6200) // 6270.5 öre -> 62 kr
    expect(b.customerPays).toBe(6341)
    expect(Number.isInteger(b.vat) && Number.isInteger(b.customerPays)).toBe(true)
  })

  it('reads rates from config, not literals (research §1 "Amounts": rates change by budget decision)', () => {
    const b = calculateInvoiceBasis([L('labour', 1000)], { vatRate: 0.25, rutRate: 0.3 })
    expect(b.rutAmount).toBe(37500)
    const c = calculateInvoiceBasis([L('labour', 1000)], { vatRate: 0.12, rutRate: 0.5 })
    expect(c.vat).toBe(12000)
  })

  it('RUT never exceeds labour incl. VAT and customer pays is never negative (research §1 "Amounts")', () => {
    const b = calculateInvoiceBasis([L('labour', 1000), L('material', 100)], { vatRate: 0.25, rutRate: 5 })
    expect(b.rutAmount).toBe(b.labourInclVat)
    expect(b.customerPays).toBeGreaterThanOrEqual(0)
    expect(calculateInvoiceBasis([L('labour', 100)], { vatRate: 0.25, rutRate: 2 }).customerPays).toBeGreaterThanOrEqual(0)
  })
})

describe('formatSek', () => {
  it('formats sv-SE with and without decimals', () => {
    const n = (s: string) => s.replace(/[\u00a0\u202f]/g, ' ').replace(/\u2212/g, '-')
    expect(n(formatSek(375000))).toBe('3 750 kr')
    expect(n(formatSek(-162500))).toBe('-1 625 kr')
    expect(n(formatSek(12541))).toBe('125,41 kr')
  })
})
