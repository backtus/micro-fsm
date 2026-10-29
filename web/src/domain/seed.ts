import { addDays, toISO } from './dates'
import type { Customer, Data, Job, Staff } from './types'

export const MASKED_PERSONNUMMER = 'ÅÅMMDD-XXXX'

const staff: Staff[] = [
  { id: 's1', name: 'Amina Hassan' },
  { id: 's2', name: 'Johan Berg' },
  { id: 's3', name: 'Mirela Kovač' },
  { id: 's4', name: 'Lars Nyström' },
]

const customers: Customer[] = [
  { id: 'c1', name: 'Familjen Lindqvist', street: 'Sveavägen 41', city: 'Stockholm', accessNote: 'Portkod 1234, nyckel i nyckelskåp' },
  { id: 'c2', name: 'Eva Strömberg', street: 'Hornsgatan 88', city: 'Stockholm', accessNote: 'Ring på, hon är hemma' },
  { id: 'c3', name: 'Karlsson & Persson', street: 'Götgatan 17', city: 'Stockholm', accessNote: 'Portkod 4821. Nyckel hos grannen på 3 tr' },
  { id: 'c4', name: 'Omar Haddad', street: 'Fleminggatan 52', city: 'Stockholm', accessNote: 'Dörrkod 7300, hund i hallen (snäll)' },
  { id: 'c5', name: 'Birgitta Åkesson', street: 'Valhallavägen 120', city: 'Stockholm', accessNote: 'Nyckel i brevlådan, kod 2020' },
  { id: 'c6', name: 'Familjen Sandberg', street: 'Bondegatan 9', city: 'Stockholm', accessNote: 'Portkod 5566' },
  { id: 'c7', name: 'Tomas Ek', street: 'Drottninggatan 14', city: 'Uppsala', accessNote: 'Nyckel i nyckelskåp vid ingången' },
  { id: 'c8', name: 'Sara Wikström', street: 'Kungsgatan 33', city: 'Uppsala', accessNote: 'Portkod 9090' },
  { id: 'c9', name: 'Familjen Dahl', street: 'Dragarbrunnsgatan 60', city: 'Uppsala', accessNote: 'Ring på, ingen portkod' },
  { id: 'c10', name: 'Anders Forsberg', street: 'Vaksalagatan 22', city: 'Uppsala', accessNote: 'Portkod 3141, nyckel under krukan' },
  { id: 'c11', name: 'Helena Brandt', street: 'Södermalmsgatan 5', city: 'Stockholm', accessNote: 'Dörrkod 6112' },
  { id: 'c12', name: 'Familjen Öberg', street: 'Strandvägen 7', city: 'Stockholm', accessNote: 'Portvakt släpper in' },
]

const TASKS = {
  regular: ['Dammsuga alla rum', 'Våttorka golv', 'Badrum och toalett', 'Kök: bänkar och spis'],
  windows: ['Putsa fönster invändigt', 'Torka fönsterkarmar'],
  move: ['Flyttstäd: skåp invändigt', 'Ugn och spis', 'Fönster', 'Golv'],
}

interface Tpl {
  customer: string
  staff: string | null
  offset: number
  time: string
  hours: number
  tasks: string[]
  /** Same customer, same weekday, repeated a week earlier and later */
  recurring?: boolean
  travelKr?: number
  materialKr?: number
}

// Offsets are days relative to today, so a week of jobs always surrounds "today".
const templates: Tpl[] = [
  { customer: 'c1', staff: 's1', offset: 0, time: '08:00', hours: 3, tasks: TASKS.regular, recurring: true },
  { customer: 'c2', staff: 's1', offset: 0, time: '11:30', hours: 2.5, tasks: TASKS.regular, recurring: true },
  { customer: 'c3', staff: 's1', offset: 0, time: '14:30', hours: 2, tasks: TASKS.windows },
  { customer: 'c4', staff: 's2', offset: 0, time: '08:30', hours: 4, tasks: TASKS.regular, recurring: true },
  { customer: 'c5', staff: 's2', offset: 0, time: '13:00', hours: 3, tasks: TASKS.regular },
  { customer: 'c7', staff: 's3', offset: 0, time: '09:00', hours: 3.5, tasks: TASKS.regular, recurring: true },
  { customer: 'c8', staff: 's3', offset: 0, time: '13:30', hours: 2, tasks: TASKS.regular },
  { customer: 'c9', staff: 's4', offset: 0, time: '10:00', hours: 5, tasks: TASKS.move },
  { customer: 'c6', staff: 's1', offset: 1, time: '08:00', hours: 3, tasks: TASKS.regular, recurring: true },
  { customer: 'c10', staff: 's3', offset: 1, time: '09:00', hours: 3, tasks: TASKS.regular, recurring: true },
  { customer: 'c11', staff: null, offset: 1, time: '12:00', hours: 3, tasks: TASKS.regular },
  { customer: 'c12', staff: 's2', offset: 2, time: '09:00', hours: 4, tasks: TASKS.regular, recurring: true },
  { customer: 'c3', staff: 's4', offset: 2, time: '13:00', hours: 3, tasks: TASKS.windows },
  { customer: 'c7', staff: null, offset: 3, time: '10:00', hours: 2.5, tasks: TASKS.regular },
  { customer: 'c1', staff: 's1', offset: 4, time: '08:00', hours: 3, tasks: TASKS.regular },
  { customer: 'c9', staff: 's4', offset: 4, time: '09:30', hours: 4, tasks: TASKS.move },
  // Past: completed, one overdue and not done
  { customer: 'c2', staff: 's2', offset: -1, time: '08:00', hours: 4, tasks: TASKS.regular },
  { customer: 'c6', staff: 's3', offset: -1, time: '12:00', hours: 3, tasks: TASKS.regular },
  { customer: 'c10', staff: 's4', offset: -2, time: '09:00', hours: 3, tasks: TASKS.regular, travelKr: 0 },
  { customer: 'c11', staff: 's2', offset: -2, time: '13:00', hours: 2.5, tasks: TASKS.regular },
]

export function createSeed(today: Date = new Date()): Data {
  const todayISO = toISO(today)
  const jobs: Job[] = []
  let n = 0
  const make = (t: Tpl, offset: number, id?: string): Job => {
    const date = addDays(todayISO, offset)
    const past = offset < 0
    return {
      id: id ?? `j${++n}`,
      customerId: t.customer,
      staffId: t.staff,
      date,
      startTime: t.time,
      plannedHours: t.hours,
      tasks: t.tasks,
      status: past ? 'done' : 'planned',
      actualHours: past ? t.hours : undefined,
      hourlyRateOre: 55000,
      materialOre: (t.materialKr ?? 80) * 100,
      travelOre: (t.travelKr ?? 250) * 100,
    }
  }
  for (const t of templates) {
    jobs.push(make(t, t.offset))
    if (t.recurring) {
      jobs.push(make(t, t.offset - 7))
      jobs.push(make(t, t.offset + 7))
    }
  }
  // Overdue and still not reported: first past job of s3 yesterday
  const overdue = jobs.find((j) => j.customerId === 'c6' && j.date === addDays(todayISO, -1))
  if (overdue) {
    overdue.status = 'planned'
    delete overdue.actualHours
  }
  // Skatteverket example (research §1 "Invoice contents"): labour 2 600 + material 100 + travel 300 ex VAT
  jobs.push({
    id: 'job-skv',
    customerId: 'c8',
    staffId: 's1',
    date: addDays(todayISO, -1),
    startTime: '15:00',
    plannedHours: 4,
    tasks: TASKS.regular,
    status: 'done',
    actualHours: 4,
    hourlyRateOre: 65000,
    materialOre: 10000,
    travelOre: 30000,
  })
  return { company: { name: 'Glans & Co Städ AB' }, staff, customers, jobs, generatedOn: todayISO }
}
