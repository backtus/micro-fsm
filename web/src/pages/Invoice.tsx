import { Link, useParams } from 'react-router-dom'
import { defaultConfig, RUT_ANNUAL_CAP_SEK } from '../domain/config'
import { formatDay } from '../domain/dates'
import { formatSek } from '../domain/format'
import { invoiceLines, jobInvoiceBasis } from '../domain/jobs'
import { MASKED_PERSONNUMMER } from '../domain/seed'
import { useAppState } from '../store/StoreContext'
import styles from './Invoice.module.css'

export function InvoiceList() {
  const { data } = useAppState()
  const jobs = data.jobs.filter((j) => j.status === 'done').sort((a, b) => b.date.localeCompare(a.date) || a.startTime.localeCompare(b.startTime))
  return (
    <section>
      <h1>Fakturaunderlag</h1>
      <p className="muted">Slutförda jobb. Välj ett jobb för att se underlaget.</p>
      {jobs.length === 0 && <p className="card">Inga slutförda jobb ännu.</p>}
      {jobs.map((j) => {
        const c = data.customers.find((x) => x.id === j.customerId)!
        return (
          <Link key={j.id} to={`/faktura/${j.id}`} className={`card ${styles.item}`}>
            <span><strong>{c.name}</strong><br /><span className="muted">{formatDay(j.date)} · {c.street}</span></span>
            <span>{formatSek(jobInvoiceBasis(j).customerPays)}</span>
          </Link>
        )
      })}
    </section>
  )
}

export function InvoiceDetail() {
  const { id } = useParams()
  const { data } = useAppState()
  const job = data.jobs.find((j) => j.id === id)
  if (!job || job.status !== 'done') return <p>Ingen faktura hittades. <Link to="/faktura">Tillbaka</Link></p>
  const c = data.customers.find((x) => x.id === job.customerId)!
  const lines = invoiceLines(job)
  const b = jobInvoiceBasis(job, defaultConfig)
  const amount = (kind: string) => lines.filter((l) => l.kind === kind).reduce((s, l) => s + l.amountOre, 0)
  const desc = (kind: string) => lines.find((l) => l.kind === kind)!.description
  return (
    <section>
      <p><Link to="/faktura">← Alla underlag</Link></p>
      <h1>Fakturaunderlag</h1>
      <div className="card">
        <strong>{c.name}</strong><br />
        <span className="muted">{c.street}, {c.city} · {formatDay(job.date)}</span><br />
        <span className="muted">Personnummer: {MASKED_PERSONNUMMER} (maskerat i prototypen)</span>
      </div>
      <div className="card">
        <table className={styles.table}>
          <tbody>
            <tr><td>Arbete<br /><span className="muted">{desc('labour')}</span></td><td>{formatSek(amount('labour'))}</td></tr>
            <tr><td>Material</td><td>{formatSek(amount('material'))}</td></tr>
            <tr><td>Resa</td><td>{formatSek(amount('travel'))}</td></tr>
            <tr><td>Moms {defaultConfig.vatRate * 100} %</td><td>{formatSek(b.vat)}</td></tr>
            <tr><td>Summa inkl. moms</td><td>{formatSek(b.totalInclVat)}</td></tr>
            <tr className={styles.rut}><td>RUT-avdrag ({defaultConfig.rutRate * 100} % av arbete inkl. moms {formatSek(b.labourInclVat)})</td><td>{formatSek(-b.rutAmount)}</td></tr>
            <tr className={styles.total}><td>Att betala</td><td>{formatSek(b.customerPays)}</td></tr>
          </tbody>
        </table>
      </div>
      {job.travelOre === 0 && (
        <div className="notice warn" role="alert">
          Ingen reskostnad debiterad. Om resan inte tas ut separat kan Skatteverket räkna den som en del av arbetskostnaden och betala ut mindre RUT.
        </div>
      )}
      <div className="notice">
        RUT förutsätter att kunden har utrymme kvar (max {new Intl.NumberFormat('sv-SE').format(RUT_ANNUAL_CAP_SEK)} kr/år) – endast kunden kan se det.
      </div>
      <p className="muted">Detta är ett underlag och inte en skatteberäkning. RUT-beloppet är preliminärt och avrundas här nedåt till hela kronor. Ansvaret för ansökan till Skatteverket ligger hos företaget.</p>
    </section>
  )
}
