import { createSeed } from '../domain/seed'
import type { Data } from '../domain/types'

export type Role = 'worker' | 'owner'

export interface AppState {
  role: Role
  workerId: string
  data: Data
}

export type Action =
  | { type: 'setRole'; role: Role }
  | { type: 'setWorker'; workerId: string }
  | { type: 'completeJob'; jobId: string; actualHours: number; note?: string }
  | { type: 'reassign'; jobId: string; staffId: string | null; date: string }
  | { type: 'reset'; today?: Date }

export function initialState(today: Date = new Date()): AppState {
  const data = createSeed(today)
  return { role: 'worker', workerId: data.staff[0].id, data }
}

export function reducer(state: AppState, action: Action): AppState {
  switch (action.type) {
    case 'setRole':
      return { ...state, role: action.role }
    case 'setWorker':
      return { ...state, workerId: action.workerId }
    case 'completeJob':
      return {
        ...state,
        data: {
          ...state.data,
          jobs: state.data.jobs.map((j) =>
            j.id === action.jobId ? { ...j, status: 'done', actualHours: action.actualHours, note: action.note || undefined } : j,
          ),
        },
      }
    case 'reassign':
      return {
        ...state,
        data: {
          ...state.data,
          jobs: state.data.jobs.map((j) => (j.id === action.jobId ? { ...j, staffId: action.staffId, date: action.date } : j)),
        },
      }
    case 'reset':
      return initialState(action.today)
  }
}
