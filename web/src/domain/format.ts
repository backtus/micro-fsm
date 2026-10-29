const withDecimals = new Intl.NumberFormat('sv-SE', { style: 'currency', currency: 'SEK' })
const whole = new Intl.NumberFormat('sv-SE', { style: 'currency', currency: 'SEK', maximumFractionDigits: 0 })

/** Formats integer öre as sv-SE kronor; whole kronor have no decimals. */
export function formatSek(ore: number): string {
  return (ore % 100 === 0 ? whole : withDecimals).format(ore / 100)
}

export function formatHours(h: number): string {
  return `${new Intl.NumberFormat('sv-SE', { maximumFractionDigits: 2 }).format(h)} h`
}
