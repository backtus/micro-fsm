import { screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { norm, renderApp } from './test/helpers'

const today = '2026-10-29'

describe('shell', () => {
  it('shows the prototype banner and resets demo data', async () => {
    const user = userEvent.setup()
    renderApp('/dag')
    expect(screen.getByText('Prototyp – exempeldata')).toBeInTheDocument()
    await user.click(screen.getAllByRole('link')[0])
    await user.click(screen.getByRole('button', { name: 'Markera som klar' }))
    expect(screen.getByText(/faktisk tid/)).toBeInTheDocument()
    vi.spyOn(window, 'confirm').mockReturnValue(true)
    await user.click(screen.getByRole('button', { name: 'Återställ demo' }))
    const jobs = JSON.parse(localStorage.getItem('micro-fsm-demo-v2')!).data.jobs as { date: string; status: string }[]
    expect(jobs.filter((j) => j.date === today && j.status === 'done')).toHaveLength(0)
  })
})

describe('Min dag', () => {
  it('lists the selected worker\'s jobs today in time order with done x/y', async () => {
    const user = userEvent.setup()
    renderApp('/dag')
    const links = screen.getAllByRole('link')
    const times = links.map((l) => l.textContent!.match(/^\d\d:\d\d/)?.[0]).filter(Boolean)
    expect(times).toEqual([...times].sort())
    expect(times).toHaveLength(3)
    expect(screen.getByText('Klart 0/3')).toBeInTheDocument()
    await user.selectOptions(screen.getByLabelText('Inloggad som'), 's4')
    expect(screen.getByText('Klart 0/1')).toBeInTheDocument()
  })
})

describe('Jobbdetalj', () => {
  it('links to the map, masks access note until Visa, and completes with hours + persistence', async () => {
    const user = userEvent.setup()
    const { unmount } = renderApp('/dag')
    await user.click(screen.getByRole('link', { name: /Familjen Lindqvist/ }))
    expect(screen.getByRole('link', { name: 'Öppna karta' })).toHaveAttribute('href', 'https://maps.google.com/?q=' + encodeURIComponent('Sveavägen 41, Stockholm'))
    expect(screen.queryByText(/Portkod 1234/)).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Visa' }))
    expect(screen.getByText('Portkod 1234, nyckel i nyckelskåp')).toBeInTheDocument()
    expect(screen.getByText('Dammsuga alla rum')).toBeInTheDocument()
    expect(screen.getByText(/planerat 3 h/)).toBeInTheDocument()

    const hours = screen.getByLabelText('Faktisk tid (timmar)')
    await user.click(screen.getByRole('button', { name: 'Öka 0,25 timmar' }))
    expect(hours).toHaveValue('3,25')
    await user.type(screen.getByLabelText(/Anteckning/), 'Allt ok')
    await user.click(screen.getByRole('button', { name: 'Markera som klar' }))
    expect(screen.getByText(/faktisk tid 3,25 h/)).toBeInTheDocument()
    unmount()

    renderApp('/dag')
    expect(screen.getByText('Klart 1/3')).toBeInTheDocument()
  })

  it.each([['0'], ['-1'], ['16,25'], ['2,1'], ['abc']])('rejects invalid hours %s', async (v) => {
    const user = userEvent.setup()
    renderApp('/dag')
    await user.click(screen.getAllByRole('link')[0])
    const hours = screen.getByLabelText('Faktisk tid (timmar)')
    await user.clear(hours)
    await user.type(hours, v)
    await user.click(screen.getByRole('button', { name: 'Markera som klar' }))
    expect(screen.getByRole('alert')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Markera som klar' })).toBeInTheDocument()
  })

  it('accepts 16 hours', async () => {
    const user = userEvent.setup()
    renderApp('/dag')
    await user.click(screen.getAllByRole('link')[0])
    const hours = screen.getByLabelText('Faktisk tid (timmar)')
    await user.clear(hours)
    await user.type(hours, '16')
    await user.click(screen.getByRole('button', { name: 'Markera som klar' }))
    expect(screen.getByText(/faktisk tid 16 h/)).toBeInTheDocument()
  })
})

describe('Vecka', () => {
  it('renders 4 staff + unassigned rows × 7 day columns and navigates weeks', async () => {
    const user = userEvent.setup()
    renderApp('/vecka')
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    await user.click(screen.getByRole('link', { name: 'Vecka' }))
    const rows = screen.getAllByTestId(/^row-/)
    expect(rows).toHaveLength(5)
    for (const r of rows) expect(within(r).getAllByTestId(/^cell-/)).toHaveLength(7)
    expect(within(screen.getByTestId('row-none')).getByText('Ej tilldelade')).toBeInTheDocument()
    expect(document.querySelector('[data-status="unassigned"]')).not.toBeNull()
    const h = screen.getByRole('heading', { level: 1 }).textContent
    await user.click(screen.getByRole('button', { name: /Nästa/ }))
    expect(screen.getByRole('heading', { level: 1 }).textContent).not.toBe(h)
  })

  it('reassigns a job via the bottom sheet and it shows up in that worker\'s Min dag', async () => {
    const user = userEvent.setup()
    renderApp('/vecka')
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    await user.click(screen.getByRole('link', { name: 'Vecka' }))
    await user.click(screen.getByRole('button', { name: /^Familjen Lindqvist 08:00, Amina Hassan, .* – flytta$/ }))
    const dialog = screen.getByRole('dialog', { name: 'Flytta till…' })
    await user.selectOptions(within(dialog).getByLabelText('Medarbetare'), 's4')
    await user.click(within(dialog).getByRole('button', { name: 'Flytta' }))
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(within(screen.getByTestId(`cell-s4-${today}`)).getByText('Familjen Lindqvist')).toBeInTheDocument()

    await user.click(screen.getByRole('button', { name: 'Fältpersonal' }))
    await user.selectOptions(screen.getByLabelText('Inloggad som'), 's4')
    expect(screen.getByText('Familjen Lindqvist')).toBeInTheDocument()
    expect(screen.getByText('Klart 0/2')).toBeInTheDocument()
  })

  it('can move a job to another day in the week', async () => {
    const user = userEvent.setup()
    renderApp('/vecka')
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    await user.click(screen.getByRole('link', { name: 'Vecka' }))
    await user.click(screen.getByRole('button', { name: /^Familjen Lindqvist 08:00, Amina Hassan, .* – flytta$/ }))
    const target = screen.getAllByRole('option').map((o) => (o as HTMLOptionElement).value).filter((v) => /^\d{4}-/.test(v) && v !== today)[0]
    await user.selectOptions(screen.getByLabelText('Dag'), target)
    await user.click(screen.getByRole('button', { name: 'Flytta' }))
    expect(within(screen.getByTestId(`cell-s1-${target}`)).getAllByText('Familjen Lindqvist').length).toBeGreaterThan(0)
  })
})

describe('Fakturaunderlag', () => {
  it('shows the Skatteverket example in sv-SE format', () => {
    renderApp('/faktura/job-skv')
    const text = norm(document.body.textContent)
    expect(text).toContain('3 750 kr')
    expect(text).toContain('-1 625 kr')
    expect(text).toContain('2 125 kr')
    expect(text).toContain('750 kr')
    expect(text).toContain('Belopp exkl. moms')
    expect(text).toContain('Belopp inkl. moms')
    expect(text).toContain('812,50 kr/h inkl. moms')
    expect(text).not.toContain('Öresavrundning')
    expect(text).toContain('ÅÅMMDD-XXXX')
    expect(text).toContain('max 75 000 kr/år')
    expect(text).toContain('endast kunden kan se det')
    expect(text).toContain('inte en skatteberäkning')
    expect(screen.queryByText(/Ingen reskostnad/)).not.toBeInTheDocument()
  })

  it('shows the zero-travel warning for the seeded zero-travel job', async () => {
    const user = userEvent.setup()
    renderApp('/faktura')
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    await user.click(screen.getByRole('link', { name: 'Fakturaunderlag' }))
    await user.click(screen.getAllByRole('link', { name: /Anders Forsberg/ })[0])
    expect(screen.getByText(/Ingen reskostnad/)).toBeInTheDocument()
  })
})

describe('Vecka: tangentbord och dialog', () => {
  const openSheet = async (user: ReturnType<typeof userEvent.setup>) => {
    renderApp('/vecka')
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    const chip = screen.getByRole('button', { name: /^Familjen Lindqvist 08:00, Amina Hassan, torsdag – flytta$/ })
    chip.focus()
    await user.keyboard('{Enter}')
    return chip
  }

  it('moves focus into the sheet, traps Tab, closes on Escape and restores focus', async () => {
    const user = userEvent.setup()
    const chip = await openSheet(user)
    const dialog = screen.getByRole('dialog', { name: 'Flytta till…' })
    expect(dialog).toContainElement(document.activeElement as HTMLElement)
    // Tab around: stays inside the dialog
    for (let i = 0; i < 6; i++) {
      await user.tab()
      expect(dialog).toContainElement(document.activeElement as HTMLElement)
    }
    await user.tab({ shift: true })
    expect(dialog).toContainElement(document.activeElement as HTMLElement)
    await user.keyboard('{Escape}')
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
    expect(chip).toHaveFocus()
  })

  it('can reassign with the keyboard only', async () => {
    const user = userEvent.setup()
    await openSheet(user)
    await user.selectOptions(screen.getByLabelText('Medarbetare'), 's2')
    screen.getByRole('button', { name: 'Flytta' }).focus()
    await user.keyboard('{Enter}')
    expect(within(screen.getByTestId(`cell-s2-${today}`)).getByRole('button', { name: /^Familjen Lindqvist 08:00, Johan Berg, torsdag – flytta$/ })).toHaveFocus()
  })
})

describe('Söndag / veckogräns', () => {
  it('shows today on Sunday in the week that started Monday, with all views working', async () => {
    vi.setSystemTime(new Date(2026, 10, 1, 12, 0, 0)) // Sunday 2026-11-01
    const user = userEvent.setup()
    renderApp('/dag')
    expect(screen.getByText('Klart 0/3')).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Ägare' }))
    expect(screen.getByRole('heading', { level: 1 }).textContent).toBe('Vecka 44')
    const row = screen.getByTestId('row-s1')
    const cells = within(row).getAllByTestId(/^cell-/)
    expect(cells).toHaveLength(7)
    expect(cells[0]).toHaveAttribute('data-testid', 'cell-s1-2026-10-26')
    expect(cells[6]).toHaveAttribute('data-testid', 'cell-s1-2026-11-01')
    expect(within(cells[6]).getAllByRole('button').length).toBeGreaterThan(0)
    await user.click(screen.getByRole('button', { name: /Nästa/ }))
    expect(screen.getByRole('heading', { level: 1 }).textContent).toBe('Vecka 45')
  })
})

describe('Öresavrundning', () => {
  it('shows a rounding line and a whole-krona amount to pay when needed', async () => {
    const user = userEvent.setup()
    renderApp('/faktura')
    await user.click(screen.getAllByRole('link', { name: /Anders Forsberg/ })[1])
    const text = norm(document.body.textContent)
    expect(text).toContain('Öresavrundning')
    expect(text).not.toMatch(/Att betala[\d ]+,\d\d kr/)
  })
})
