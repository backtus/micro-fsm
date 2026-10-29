import { describe, expect, it } from 'vitest'
import { addDays, toISO } from './dates'
import { createSeed } from './seed'

const today = new Date(2026, 9, 29)
const data = createSeed(today)

describe('seed data', () => {
  it('has the company, 4 staff and about 12 customers', () => {
    expect(data.company.name).toBe('Glans & Co Städ AB')
    expect(data.staff).toHaveLength(4)
    expect(data.customers).toHaveLength(12)
  })
  it('references only existing staff and customers', () => {
    for (const j of data.jobs) {
      expect(data.customers.some((c) => c.id === j.customerId)).toBe(true)
      if (j.staffId !== null) expect(data.staff.some((s) => s.id === j.staffId)).toBe(true)
    }
  })
  it('has at least one unassigned job and one access note', () => {
    expect(data.jobs.some((j) => j.staffId === null)).toBe(true)
    expect(data.customers.some((c) => /portkod/i.test(c.accessNote))).toBe(true)
  })
  it('is relative to today and every worker has jobs today', () => {
    const t = toISO(today)
    for (const s of data.staff) expect(data.jobs.some((j) => j.staffId === s.id && j.date === t)).toBe(true)
    expect(data.jobs.some((j) => j.date === addDays(t, -1))).toBe(true)
  })
  it('has recurring-looking jobs (same customer, same weekday, 7 days apart)', () => {
    const j = data.jobs.find((x) => x.customerId === 'c1' && x.date === toISO(today))!
    expect(data.jobs.some((x) => x.customerId === 'c1' && x.date === addDays(j.date, 7))).toBe(true)
  })
  it('has an overdue, not-done job', () => {
    const t = toISO(today)
    expect(data.jobs.some((j) => j.status === 'planned' && j.date < t)).toBe(true)
  })
  it('contains no personnummer-like values', () => {
    const text = JSON.stringify(data)
    expect(text).not.toMatch(/\b\d{6,8}[-+]?\d{4}\b/)
  })
})
