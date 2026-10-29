import { createContext, useContext, useEffect, useReducer, type Dispatch, type ReactNode } from 'react'
import { toISO } from '../domain/dates'
import { initialState, reducer, type Action, type AppState } from './reducer'

export const STORAGE_KEY = 'micro-fsm-demo-v2'

function load(): AppState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      const parsed = JSON.parse(raw) as AppState
      // Regenerate when the seed was made on another day, so the demo never looks stale.
      if (parsed?.data?.generatedOn === toISO(new Date())) return parsed
    }
  } catch {
    // fall through to a fresh seed
  }
  return initialState()
}

const StateCtx = createContext<AppState | null>(null)
const DispatchCtx = createContext<Dispatch<Action> | null>(null)

export function StoreProvider({ children }: { children: ReactNode }) {
  const [state, dispatch] = useReducer(reducer, undefined, load)
  useEffect(() => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
  }, [state])
  return (
    <StateCtx.Provider value={state}>
      <DispatchCtx.Provider value={dispatch}>{children}</DispatchCtx.Provider>
    </StateCtx.Provider>
  )
}

export function useAppState(): AppState {
  const s = useContext(StateCtx)
  if (!s) throw new Error('StoreProvider missing')
  return s
}
export function useDispatch(): Dispatch<Action> {
  const d = useContext(DispatchCtx)
  if (!d) throw new Error('StoreProvider missing')
  return d
}
