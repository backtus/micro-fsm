import { Link } from 'react-router-dom'
import { toISO } from '../domain/dates'
import { useAppState } from '../store/StoreContext'
import styles from './MyDay.module.css'

export function MyDay() {
  const { data, workerId } = useAppState()
  const today = toISO(new Date())
  const jobs = data.jobs.filter((j) => j.staffId === workerId && j.date === today).sort((a, b) => a.startTime.localeCompare(b.startTime))
  const done = jobs.filter((j) => j.status === 'done').length
  const worker = data.staff.find((s) => s.id === workerId)
  return (
    <section>
      <h1>Min dag</h1>
      <p className="muted">{worker?.name}</p>
      <p className={styles.progress}>Klart {done}/{jobs.length}</p>
      {jobs.length === 0 && <p className="card">Inga jobb idag.</p>}
      {jobs.map((j) => {
        const c = data.customers.find((x) => x.id === j.customerId)!
        return (
          <Link key={j.id} to={`/jobb/${j.id}`} className={`card ${styles.row}`}>
            <span className={styles.time}>{j.startTime}</span>
            <span className={styles.info}>
              <span className={styles.name}>{c.name}</span>
              <br />
              <span className="muted">{c.street}, {c.city}</span>
            </span>
            <span className={`${styles.badge} ${j.status === 'done' ? styles.done : ''}`}>{j.status === 'done' ? 'Klar' : 'Planerad'}</span>
          </Link>
        )
      })}
    </section>
  )
}
