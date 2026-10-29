import { Navigate, Route, Routes } from 'react-router-dom'
import { Shell } from './components/Shell'
import { MyDay } from './pages/MyDay'
import { JobDetail } from './pages/JobDetail'
import { WeekView } from './pages/WeekView'
import { InvoiceList, InvoiceDetail } from './pages/Invoice'
import { useAppState } from './store/StoreContext'

function Home() {
  const { role } = useAppState()
  return <Navigate to={role === 'owner' ? '/vecka' : '/dag'} replace />
}

export function App() {
  return (
    <Routes>
      <Route element={<Shell />}>
        <Route index element={<Home />} />
        <Route path="dag" element={<MyDay />} />
        <Route path="jobb/:id" element={<JobDetail />} />
        <Route path="vecka" element={<WeekView />} />
        <Route path="faktura" element={<InvoiceList />} />
        <Route path="faktura/:id" element={<InvoiceDetail />} />
        <Route path="*" element={<Home />} />
      </Route>
    </Routes>
  )
}
