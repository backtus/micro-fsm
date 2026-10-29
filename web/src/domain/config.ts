export interface InvoiceConfig {
  /** VAT rate as a fraction, e.g. 0.25 */
  vatRate: number
  /** RUT share of labour cost incl. VAT, e.g. 0.5. Rates change by budget decision, so keep them here. */
  rutRate: number
}

export const defaultConfig: InvoiceConfig = { vatRate: 0.25, rutRate: 0.5 }

/** Annual RUT cap per person, shown in information text only (öre). Only the customer can see the remaining allowance. */
export const RUT_ANNUAL_CAP_SEK = 75000
