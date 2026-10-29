import '@testing-library/jest-dom/vitest'
import { afterEach, beforeEach, vi } from 'vitest'
import { cleanup } from '@testing-library/react'

// Freeze only Date (Thursday 2026-10-29) so tests never depend on the weekday they run on.
beforeEach(() => {
  vi.useFakeTimers({ toFake: ['Date'], now: new Date(2026, 9, 29, 12, 0, 0) })
})

afterEach(() => {
  vi.useRealTimers()
  cleanup()
  localStorage.clear()
})
