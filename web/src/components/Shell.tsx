import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAppState, useDispatch } from '../store/StoreContext'
import styles from './Shell.module.css'

export function Shell() {
  const { role, workerId, data } = useAppState()
  const dispatch = useDispatch()
  const navigate = useNavigate()
  const switchRole = (r: 'worker' | 'owner') => {
    dispatch({ type: 'setRole', role: r })
    navigate(r === 'owner' ? '/vecka' : '/dag')
  }
  return (
    <>
      <div className={styles.banner} role="note">Prototyp – exempeldata</div>
      <header className={styles.header}>
        <div className={styles.top}>
          <span className={styles.company}>{data.company.name}</span>
          <button
            className={styles.reset}
            onClick={() => {
              if (window.confirm('Återställa all exempeldata?')) dispatch({ type: 'reset' })
            }}
          >
            Återställ demo
          </button>
        </div>
        <div className={styles.roles} role="group" aria-label="Roll">
          <button className={styles.role} aria-pressed={role === 'worker'} onClick={() => switchRole('worker')}>
            Fältpersonal
          </button>
          <button className={styles.role} aria-pressed={role === 'owner'} onClick={() => switchRole('owner')}>
            Ägare
          </button>
        </div>
        {role === 'worker' && (
          <div className={styles.picker}>
            <label htmlFor="worker">Inloggad som</label>
            <select id="worker" value={workerId} onChange={(e) => dispatch({ type: 'setWorker', workerId: e.target.value })}>
              {data.staff.map((s) => (
                <option key={s.id} value={s.id}>{s.name}</option>
              ))}
            </select>
          </div>
        )}
      </header>
      <main className={styles.main}>
        <Outlet />
      </main>
      <nav className={styles.nav} aria-label="Huvudmeny">
        {role === 'worker' ? (
          <NavLink to="/dag">Min dag</NavLink>
        ) : (
          <>
            <NavLink to="/vecka">Vecka</NavLink>
            <NavLink to="/faktura">Fakturaunderlag</NavLink>
          </>
        )}
      </nav>
    </>
  )
}
