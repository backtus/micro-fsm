import { useEffect, useRef, useState } from 'react'
import { addDays, formatDay, fullDayName, isoWeek, mondayOf, toISO } from '../domain/dates'
import { isOverdue } from '../domain/jobs'
import type { Job } from '../domain/types'
import { useAppState, useDispatch } from '../store/StoreContext'
import styles from './WeekView.module.css'

const UNASSIGNED = ''

export function WeekView() {
  const { data } = useAppState()
  const dispatch = useDispatch()
  const today = toISO(new Date())
  const [weekStart, setWeekStart] = useState(() => mondayOf(today))
  const [moving, setMoving] = useState<Job | null>(null)
  const [toStaff, setToStaff] = useState(UNASSIGNED)
  const [toDate, setToDate] = useState('')
  const sheetRef = useRef<HTMLDivElement>(null)
  const lastJobId = useRef<string | null>(null)
  const days = Array.from({ length: 7 }, (_, i) => addDays(weekStart, i))
  const rows = [...data.staff.map((s) => ({ id: s.id as string | null, name: s.name })), { id: null, name: 'Ej tilldelade' }]

  // Focus management for the sheet: move focus in on open, restore to the job's chip on close.
  const movingId = moving?.id ?? null
  useEffect(() => {
    if (movingId) {
      lastJobId.current = movingId
      sheetRef.current?.querySelector<HTMLElement>('select')?.focus()
    } else if (lastJobId.current) {
      document.querySelector<HTMLElement>(`[data-job-id="${lastJobId.current}"]`)?.focus()
      lastJobId.current = null
    }
  }, [movingId])
  const onSheetKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Escape') {
      e.stopPropagation()
      setMoving(null)
      return
    }
    if (e.key !== 'Tab') return
    const items = Array.from(sheetRef.current!.querySelectorAll<HTMLElement>('select, button'))
    const first = items[0]
    const last = items[items.length - 1]
    if (e.shiftKey && document.activeElement === first) {
      e.preventDefault()
      last.focus()
    } else if (!e.shiftKey && document.activeElement === last) {
      e.preventDefault()
      first.focus()
    }
  }

  const open = (j: Job) => {
    setMoving(j)
    setToStaff(j.staffId ?? UNASSIGNED)
    setToDate(j.date)
  }
  const confirm = () => {
    if (!moving) return
    dispatch({ type: 'reassign', jobId: moving.id, staffId: toStaff === UNASSIGNED ? null : toStaff, date: toDate })
    setMoving(null)
  }

  return (
    <section>
      <h1>Vecka {isoWeek(weekStart)}</h1>
      <div className={styles.nav}>
        <button className="btn secondary" onClick={() => setWeekStart(addDays(weekStart, -7))}>← Föregående</button>
        <button className="btn secondary" onClick={() => setWeekStart(mondayOf(today))}>Idag</button>
        <button className="btn secondary" onClick={() => setWeekStart(addDays(weekStart, 7))}>Nästa →</button>
      </div>
      <div className={styles.legend}>
        <span>🟧 Ej tilldelat</span><span>🟥 Försenat, ej klart</span><span>🟩 Klart</span>
      </div>
      <div className={styles.scroll}>
        <table className={styles.grid}>
          <thead>
            <tr>
              <th scope="col">Personal</th>
              {days.map((d) => (
                <th key={d} scope="col" className={d === today ? styles.today : ''}>{formatDay(d)}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.id ?? 'none'} className={r.id === null ? styles.unRow : ''} data-testid={`row-${r.id ?? 'none'}`}>
                <th scope="row">{r.name}</th>
                {days.map((d) => {
                  const jobs = data.jobs.filter((j) => j.staffId === r.id && j.date === d).sort((a, b) => a.startTime.localeCompare(b.startTime))
                  return (
                    <td key={d} data-testid={`cell-${r.id ?? 'none'}-${d}`} className={d === today ? styles.today : ''}>
                      {jobs.map((j) => {
                        const c = data.customers.find((x) => x.id === j.customerId)!
                        const cls = j.status === 'done' ? styles.chipDone : j.staffId === null ? styles.chipUn : isOverdue(j, today) ? styles.chipOver : ''
                        return (
                          <button
                            key={j.id}
                            className={`${styles.chip} ${cls}`}
                            data-status={j.staffId === null ? 'unassigned' : isOverdue(j, today) ? 'overdue' : j.status}
                            data-job-id={j.id}
                            aria-label={`${c.name} ${j.startTime}, ${j.staffId ? data.staff.find((s) => s.id === j.staffId)?.name : 'Ej tilldelad'}, ${fullDayName(j.date)} – flytta`}
                            onClick={() => open(j)}
                          >
                            <b>{j.startTime}</b>
                            {c.name}
                          </button>
                        )
                      })}
                    </td>
                  )
                })}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {moving && (
        <div className={styles.overlay} onClick={() => setMoving(null)}>
          <div ref={sheetRef} className={styles.sheet} role="dialog" aria-modal="true" aria-labelledby="sheet-title" onKeyDown={onSheetKeyDown} onClick={(e) => e.stopPropagation()}>
            <h2 id="sheet-title">Flytta till…</h2>
            <p className="muted">{data.customers.find((c) => c.id === moving.customerId)?.name} · {moving.startTime}</p>
            <label htmlFor="to-staff">Medarbetare</label>
            <select id="to-staff" value={toStaff} onChange={(e) => setToStaff(e.target.value)}>
              {data.staff.map((s) => <option key={s.id} value={s.id}>{s.name}</option>)}
              <option value={UNASSIGNED}>Ej tilldelad</option>
            </select>
            <label htmlFor="to-day">Dag</label>
            <select id="to-day" value={toDate} onChange={(e) => setToDate(e.target.value)}>
              {days.map((d) => <option key={d} value={d}>{formatDay(d)}</option>)}
            </select>
            <div className={styles.actions}>
              <button className="btn secondary" onClick={() => setMoving(null)}>Avbryt</button>
              <button className="btn" onClick={confirm}>Flytta</button>
            </div>
          </div>
        </div>
      )}
    </section>
  )
}
