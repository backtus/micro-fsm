export function toISO(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`
}
export function fromISO(s: string): Date {
  const [y, m, d] = s.split('-').map(Number)
  return new Date(y, m - 1, d)
}
export function addDays(iso: string, n: number): string {
  const d = fromISO(iso)
  d.setDate(d.getDate() + n)
  return toISO(d)
}
export function mondayOf(iso: string): string {
  const d = fromISO(iso)
  return addDays(iso, -((d.getDay() + 6) % 7))
}
const dayNames = ['sön', 'mån', 'tis', 'ons', 'tors', 'fre', 'lör']
export function dayName(iso: string): string {
  return dayNames[fromISO(iso).getDay()]
}
const fullDayNames = ['söndag', 'måndag', 'tisdag', 'onsdag', 'torsdag', 'fredag', 'lördag']
export function fullDayName(iso: string): string {
  return fullDayNames[fromISO(iso).getDay()]
}
export function formatDay(iso: string): string {
  const d = fromISO(iso)
  return `${dayName(iso)} ${d.getDate()}/${d.getMonth() + 1}`
}
export function isoWeek(iso: string): number {
  const d = fromISO(iso)
  d.setDate(d.getDate() + 3 - ((d.getDay() + 6) % 7))
  const jan4 = new Date(d.getFullYear(), 0, 4)
  return 1 + Math.round(((d.getTime() - jan4.getTime()) / 86400000 - 3 + ((jan4.getDay() + 6) % 7)) / 7)
}
