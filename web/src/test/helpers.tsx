import { render } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { App } from '../App'
import { StoreProvider } from '../store/StoreContext'

export function renderApp(route = '/') {
  return render(
    <StoreProvider>
      <MemoryRouter initialEntries={[route]}>
        <App />
      </MemoryRouter>
    </StoreProvider>,
  )
}

export const norm = (s: string | null | undefined) => (s ?? '').replace(/[\u00a0\u202f]/g, ' ').replace(/\u2212/g, '-')
