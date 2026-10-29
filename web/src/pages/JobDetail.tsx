import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { formatDay } from '../domain/dates'
import { formatHours } from '../domain/format'
import { useAppState, useDispatch } from '../store/StoreContext'
import styles from './JobDetail.module.css'

export const MAX_HOURS = 16
export const HOUR_STEP = 0.25

/** Returns hours or an error message. Accepts comma or dot decimals. */
export function parseHours(input: string): { hours?: number; error?: string } {
  const h = Number(input.trim().replace(',', '.'))
  if (input.trim() === '' || Number.isNaN(h)) return { error: 'Ange antal timmar.' }
  if (h <= 0) return { error: 'Timmar måste vara mer än 0.' }
  if (h > MAX_HOURS) return { error: `Timmar får vara högst ${MAX_HOURS}.` }
  if (Math.abs(h / HOUR_STEP - Math.round(h / HOUR_STEP)) > 1e-9) return { error: 'Ange timmar i steg om 0,25 (kvart).' }
  return { hours: h }
}

export function JobDetail() {
  const { id } = useParams()
  const { data } = useAppState()
  const dispatch = useDispatch()
  const job = data.jobs.find((j) => j.id === id)
  const [show, setShow] = useState(false)
  const [hours, setHours] = useState(() => String(job?.plannedHours ?? 1).replace('.', ','))
  const [note, setNote] = useState('')
  const [error, setError] = useState<string>()
  if (!job) return <p>Jobbet hittades inte. <Link to="/">Till start</Link></p>
  const c = data.customers.find((x) => x.id === job.customerId)!
  const address = `${c.street}, ${c.city}`
  const staffName = data.staff.find((s) => s.id === job.staffId)?.name ?? 'Ej tilldelad'

  const step = (d: number) => {
    const p = parseHours(hours)
    const base = p.hours ?? job.plannedHours
    setHours(String(Math.max(HOUR_STEP, base + d)).replace('.', ','))
    setError(undefined)
  }
  const submit = (e: React.FormEvent) => {
    e.preventDefault()
    const p = parseHours(hours)
    if (p.error) return setError(p.error)
    setError(undefined)
    dispatch({ type: 'completeJob', jobId: job.id, actualHours: p.hours!, note: note.trim() })
  }

  return (
    <section>
      <p><Link to="/">← Tillbaka</Link></p>
      <h1>{c.name}</h1>
      <div className="card">
        <strong>{address}</strong>
        <p className="muted">{formatDay(job.date)} kl. {job.startTime} · planerat {formatHours(job.plannedHours)} · {staffName}</p>
        <a className="btn secondary" href={`https://maps.google.com/?q=${encodeURIComponent(address)}`} target="_blank" rel="noreferrer">Öppna karta</a>
      </div>
      <h2>Tillträde</h2>
      <div className={`card ${styles.access}`}>
        {show ? <span>{c.accessNote}</span> : <span className={styles.masked} aria-label="Dold">••••••••••</span>}
        <button className="btn secondary" onClick={() => setShow((s) => !s)}>{show ? 'Dölj' : 'Visa'}</button>
      </div>
      <h2>Uppgifter</h2>
      <div className="card">
        <ul className={styles.tasks}>{job.tasks.map((t) => <li key={t}>{t}</li>)}</ul>
      </div>
      <h2>Rapportera</h2>
      {job.status === 'done' ? (
        <div className="card">
          <strong>Klar</strong> – faktisk tid {formatHours(job.actualHours ?? 0)}
          {job.note && <p className="muted">Anteckning: {job.note}</p>}
        </div>
      ) : (
        <form className="card" onSubmit={submit} noValidate>
          <label htmlFor="hours">Faktisk tid (timmar)</label>
          <div className={styles.stepper}>
            <button type="button" aria-label="Minska 0,25 timmar" onClick={() => step(-HOUR_STEP)}>−</button>
            <input id="hours" inputMode="decimal" value={hours} onChange={(e) => setHours(e.target.value)} aria-invalid={!!error} />
            <button type="button" aria-label="Öka 0,25 timmar" onClick={() => step(HOUR_STEP)}>+</button>
          </div>
          {error && <p className="error" role="alert">{error}</p>}
          <label htmlFor="note">Anteckning (valfritt)</label>
          <textarea id="note" rows={3} value={note} onChange={(e) => setNote(e.target.value)} />
          <p className="muted">Skriv inga känsliga uppgifter, till exempel om hälsa.</p>
          <button className="btn block" type="submit">Markera som klar</button>
        </form>
      )}
    </section>
  )
}
