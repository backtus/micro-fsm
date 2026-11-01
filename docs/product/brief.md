# Product Brief — micro-fsm

> **Status:** Draft from 2026-10-29; the project is shelved (see `STATE.md`). **Research caveat:** the competitor overview is a snapshot of public marketing pages from October 2026. Prices and features were not verified, may be outdated or wrong, and are not an assessment of the products' quality. Do your own competitor pass before relying on them.

## 1. Problem and target customer

Small Swedish cleaning and moving companies (städ/flytt) run operations from Excel, SMS, phone calls and memory. The owner is usually out cleaning or moving too, so scheduling and admin happen in the evening. A missed shift change or a wrong RUT invoice costs real money and customer trust.

**Segment size (unverified estimate):** SNI 81.2 (cleaning) and 49.42 (removals) probably cover 10,000+ companies in Sweden, most with 0–9 staff. Likely target — 2–30 staff selling to households (RUT) — roughly 3,000–6,000. Verify against SCB Företagsregister and Skatteverket RUT statistics.

**Personas**

- **Owner/manager ("Ägaren"):** works in the field 50–100% of the time; plans the week on a phone. Pains: schedule changes, sick calls, month-end invoicing.
- **Field worker:** often not a native Swedish speaker; phone, not laptop. Needs where, when, how to get in (keys/door code), what to do.
- **End customer (household):** uses RUT, wants reliability, the same cleaner, a correct invoice.
- **Office admin (only at ~10+ staff):** invoicing, RUT claims, payroll basis.

## 2. Jobs-to-be-done and today's pain

| Job | Today | Pain |
|---|---|---|
| Plan recurring cleanings (weekly / every other week) | Excel, Google Sheets, paper | Changes don't reach staff; holidays and sick days break the plan |
| Tell staff where to go | SMS/WhatsApp every evening | Errors, no single source of truth, door codes in plain text |
| Know what was done and how long it took | Calls, memory, paper timesheets | Payroll and invoices don't match reality |
| Invoice with RUT | Manual entry in Fortnox/Visma per job | Personnummer and property details re-typed; RUT rejections; slow cash flow |
| Claim RUT payout from Skatteverket | Manual file or e-service | Time-consuming; errors delay payout |

## 3. Competitor landscape

| Product | Positioning | Pricing | Notes / gaps |
|---|---|---|---|
| Upflow (Serviq) — [Fortnox](https://www.fortnox.se/integrationer/integration/upflow-en-del-av-serviq-connected-ab/upflow-for-stad-flyttfirmor) | All-in-one for städ/flytt: quotes, scheduling, work orders, Fortnox invoicing, RUT | not checked | Targets the same segment (städ/flytt with RUT and Fortnox). Depth and UX not assessed. |
| Wisegent — [wisegent.se](https://wisegent.se/se) | Cleaning-specific scheduling, free "AI scheduling", key handling, RUT invoicing | not checked | Targets the same segment; markets free scheduling features. |
| Bokning & Schema Online (Bird Vision) — [Fortnox](https://www.fortnox.se/integrationer/integration/bird-vision-ab/bokning-schema-online) | Quotes, scheduling, Fortnox invoicing, time reporting | not checked | More generic positioning; not assessed in depth. |
| Easyserv | Field service / work orders | not checked | Broader field-service scope; less household/RUT focus. |
| Timewise | Staff scheduling (healthcare, retail) | not checked | Staff scheduling rather than field jobs. |
| Moment | Projects/time/invoicing for agencies | not checked | Aimed at agencies, a different vertical. |
| Fieldly | Construction/trades (ROT) | not checked | Aimed at construction/trades (ROT), not RUT. |
| Bokamera | Booking calendar (salons etc.) | not checked | Booking calendar for self-booking, not crew scheduling. |
| Bokio / Fortnox / Visma | Accounting with RUT/ROT "husfaktura" ([Fortnox help](https://support.fortnox.se/produkthjalp/fakturering-plus/skapa-husfaktura-rot-rut-och-gront-avdrag)) | low | **Integration target**, no scheduling. |
| Jobber / Housecall Pro / ServiceM8 | International SMB FSM | ~USD 30–200+/month (not checked) | General-purpose international tools; Swedish RUT/Fortnox support not checked. |

**Takeaway:** the niche is **not empty** — Upflow and Wisegent target exactly städ/flytt with RUT and Fortnox. Having the features won't win; execution will: speed, field-first UX, low price, onboarding in under an hour.

## 4. Differentiation hypothesis

1. **Built for the owner who is in the field:** every manager action (reschedule, cover a sick call, mark done) in under 30 seconds on a phone. Competitors may be more desktop-oriented (not verified).
2. **RUT correct by construction:** automatic labour/material split, local tracking of RUT invoiced by us plus the customer-declared remaining allowance (the system cannot see deductions via other suppliers and cannot guarantee eligibility), missing fields flagged *before* invoicing. Fewer rejections, faster cash.
3. **Recurring cleaning as a first-class concept:** same cleaner, same customer, every other Tuesday, with holiday/sick exceptions.
4. **Simple, transparent, low price** with self-service signup — enabled by ~$0 idle infrastructure.
5. **Multilingual field view** (Swedish/English first, more later).

All need validation; 1 and 2 are the strongest bets.

## 5. MVP scope

**Segment order:** home cleaning first, then move-out cleaning (flyttstädning), then possibly moving.

**In**

- **Login:** company account; roles owner/admin and field worker. Field workers use a magic link (SMS or e-mail).
- **Customers and properties:** private or company customer, address, RUT eligibility, personnummer (encrypted), access notes (encrypted).
- **Services/price list:** hourly or fixed; each line marked RUT-eligible labour or material/travel.
- **Jobs:** single or recurring (weekly, biweekly, monthly); assign one or more staff; edit/cancel one occurrence.
- **Field worker "Min dag" (PWA):** today's jobs in order, map link, access notes, start/stop or actual hours, mark done, note.
- **Manager week view:** staff × days; unassigned/unfinished highlighted; quick reassign.
- **Invoice basis with RUT split:** per job or per customer per month; labour/material split, RUT amount, amount payable; validation of required RUT fields.
- **Export:** CSV/PDF invoice basis for manual entry in accounting.
- **RUT claim file:** Skatteverket XML (Begaran.xsd v6) for the user to upload.
- **Payment tracking** and a **RUT deadline reminder** (31 Jan).
- **Offline:** the PWA caches static app assets only. Offline storage of personal data on devices is out of MVP until data categories, protection and deletion rules are decided.

**Out:** Fortnox API integration (first post-MVP item); Spiris/Visma integration; payroll; mileage log; quotes and self-booking; SMS reminders to customers; customer portal; route optimisation; checklists/photos; moving-specific features (volume, trucks — moving uses the same job model); offline storage of personal data and offline-first sync; languages beyond Swedish (English fallback); Norway/Denmark.

## 6. Walking skeleton

**Goal:** a manager-created job appears on a field worker's phone, is marked done, and produces a RUT-split invoice line — proving every layer.

- IaC deploying to `eu-north-1` with a budget alarm.
- Quarkus/Kotlin on Lambda behind API Gateway HTTP API.
- Database, runtime and auth per the architecture ADRs.
- Minimal PWA on S3 + CloudFront.
- Flow: seed company/customer/worker → `POST /jobs` → worker's "Min dag" lists it → `POST /jobs/{id}/complete` with hours → `GET /jobs/{id}/invoice-basis` returns labour/material/RUT/payable.
- Tests: RUT calculation unit tests with documented examples; a tenant-isolation test.
- Excluded: recurrence and week view (next slices).

## 7. Pricing hypothesis

- Flat per company with staff caps, e.g. **Start** 299 SEK/month (≤3 users), **Team** 699 SEK (≤15), **Pro** 1,290 SEK (≤30, incl. Fortnox integration).
- Alternative: 99–149 SEK per active user/month, minimum 299.
- Anchor: one saved admin hour per week ≈ 1,500+ SEK/month of owner time.
- Validate: Van Westendorp questions in interviews; landing page with two price variants; pilots sign a letter of intent at full price with 3 months free.

## 8. Risks, assumptions, validation

| # | Assumption / risk | Validation |
|---|---|---|
| A1 | Owners will switch from Excel; pain is strong enough | Interview 5–8 companies (2–15 staff) |
| A2 | Existing tools leave a real gap | Trial 3 competitors; ask interviewees why they don't use them |
| A3 | Fortnox integration is required to pay | Ask directly; check Fortnox partner requirements/costs |
| A4 | Field workers will use a phone app | 4-week pilot with 1–2 companies; measure share of jobs marked done in-app |
| R1 | Personnummer is GDPR-sensitive; RUT errors have financial consequences | Encryption, DPA template, RUT rules documented with Skatteverket references, user responsible for the claim |
| R2 | Fortnox/Visma partner cost or certification | Find out before promising |
| R3 | Seasonality and churn | Track in pilot; offer annual pricing |

**Next step:** ~2 weeks of discovery in parallel with the walking skeleton — 5–8 interviews, 3 competitor trials, SCB numbers. No outreach to third parties without Kent's approval.

## 9. Open questions

1. Access to real companies for interviews and a pilot? Who would be the first design partner?
2. Brand/name and domain; Swedish-only UI at launch?
