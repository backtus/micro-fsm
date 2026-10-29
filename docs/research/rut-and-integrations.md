# RUT-avdrag and accounting integrations — research notes

Researched 2026-10-29. Every claim links to a source. Items marked ⚠️ are uncertain, inferred or possibly outdated. Primary sources are Skatteverket pages, read on the research date. This is not legal or tax advice.

## 1. RUT rules for cleaning and moving

### Amounts (2026)

- RUT is at most **50 % of the labour cost including VAT**. Source: [Skatteverket – Så fungerar rutavdraget för företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).
- The annual cap is **75 000 SEK per person per year** for ROT and RUT combined. At most 50 000 SEK of that can be ROT, so the full 75 000 SEK can be used for RUT. Source: [Skatteverket – Rot och rutavdrag företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut.4.2ef18e6a125660db8b080002674.html).
- The temporary 50 % ROT rate applied from 12 May to 31 Dec 2025. ROT went back to 30 % in 2026, and RUT was not affected. Source (secondary): [e-flytt.se](https://e-flytt.se/artiklar/rutavdrag-flytt).
- ⚠️ Rates and caps change by budget decision. Keep them as configuration, not code constants.
- The cap is checked against the buyer's remaining allowance. Only the customer can see what they have left, via Skatteverket's e-service "Mina skattereduktioner" or the app. The company cannot see it. Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### Who qualifies

The customer must:

- live in the home where the work is done (permanent or holiday home in the EU/EEA or Switzerland);
- be billed for the work or bear the cost;
- have allowance left;
- be taxed in Sweden;
- be 18 or older by year end.

The work must not be done by a relative. The company must have F-skatt when the agreement is made or when payment is made. Common areas in apartment buildings do not count as the customer's home. Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### Cleaning (städning)

- **Qualifies:** ordinary cleaning in the home, including move-in and move-out cleaning (flyttstäd).
- **Does not qualify:**
  - cleaning after construction work (that can be ROT instead);
  - heavy cleaning with advanced equipment;
  - deep cleaning of furniture;
  - common areas;
  - a landlord cleaning after a tenant.
- Sources: [Skatteverket – Ger arbetet rätt till rutavdrag?](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/gerarbetetratttillrutavdrag.4.2ef18e6a125660db8b080001531.html) and the summary at [e-flytt.se](https://e-flytt.se/artiklar/rutavdrag-flytt).
- ⚠️ Confirm the exact cleaning list on Skatteverket's page before building a service catalogue.

### Moving (flyttjänster)

Conditions: the household goods must belong to the customer's household. The move must be between the customer's homes or storage within the EU/EEA or Switzerland. Source: [Skatteverket – Ger arbetet rätt till rutavdrag?](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/gerarbetetratttillrutavdrag.4.2ef18e6a125660db8b080001531.html).

**Qualifies:**

- moving all or part of the household goods between homes, including within the same building;
- moving goods to or from storage;
- packing and unpacking, loading and unloading;
- driver and crew time while transporting;
- taking furniture apart and putting it back together;
- taking down and putting up pictures, curtains and light fittings in direct connection with the move;
- moving pianos and other heavy items.

**Does not qualify:**

- the moving truck, fuel, boxes or storage rent;
- travel between the company's premises and the customer's home;
- transport by air or sea;
- moving goods from a furniture retailer or to or from another private person;
- packing and taking junk to a recycling station.

### What must be separated

For cleaning-type services only labour **on site** qualifies. For moving services, crew and driver time spent transporting household goods is eligible labour, while travel to and from the customer is not. These do not qualify:

- materials, including consumables such as cleaning agents, which must always be charged;
- travel time to and from the customer;
- vehicle cost per mil (10 km);
- machines and equipment;
- waste handling;
- administration;
- accommodation.

If the company does not charge for travel, Skatteverket treats travel as included in the labour cost and may pay out less. **Actual hours worked** must be recorded per service type, also for fixed-price jobs, including subcontractor hours.

Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### Invoice contents

Skatteverket says the invoice "bör" (should) include:

1. the total amount and the RUT amount, including VAT;
2. labour, material and other costs shown separately;
3. the type of service;
4. a statement that the company has F-skatt;
5. the customer's name and personnummer.

Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

Worked example from the same page:

| Item | SEK |
|---|---|
| Labour | 2 600 |
| Material | 100 |
| Travel | 300 |
| VAT 25 % | 750 |
| RUT (50 % × 3 250, labour incl. VAT) | −1 625 |
| **Customer pays** | **2 125** |

### Property details

ROT needs a fastighetsbeteckning, or a BRF org.nr plus apartment number. The e-service validates a BRF org.nr where one is given. Source: [Regler för att importera fil](https://www.skatteverket.se/foretag/etjansterochblanketter/allaetjanster/schemalagerxml/rotochrutforetag/reglerforattimporterafiltillrotochrut.4.76a43be412206334b89800033198.html).

⚠️ For **RUT**, Skatteverket's invoice list above does not require property details. This needs verifying against the v6 XSD before final data modelling.

### Multiple buyers

Several people in the household can share the deduction, but the total can never exceed 50 % of the labour cost. Skatteverket says the invoice should show each person's share. If one person has used up their allowance, the company can apply for another household member. One request can hold up to 100 buyers. Sources: [Skatteverket privat](https://www.skatteverket.se/privat/fastigheterochbostad/rotarbeteochrutarbete/safungerarrutavdraget.4.d5e04db14b6fef2c866097.html), [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html) and the [import rules](https://www.skatteverket.se/foretag/etjansterochblanketter/allaetjanster/schemalagerxml/rotochrutforetag/reglerforattimporterafiltillrotochrut.4.76a43be412206334b89800033198.html).

### Payment rules

- **Electronic payment is required** (card, Swish, bank transfer or BankID payment). Cash, cheques and gift cards do not count.
- The work must be **completed and paid** before the company claims.
- The **payment date** decides which tax year the deduction belongs to, not the invoice date.
- For partial payments, claim in proportion to what has been paid.
- Each partial invoice must carry its own RUT deduction. You cannot put the whole RUT on the final invoice.
- With factoring, the company must wait until the customer has paid the factoring company.
- For subscriptions or "timbank" (prepaid hours), only hours actually performed can be claimed.

Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### VAT timing

Companies using the cash method (bokslutsmetoden) report VAT in two steps:

1. VAT on the amount the customer paid;
2. VAT on the RUT amount, once Skatteverket has paid it out.

Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

## 2. Claiming payment from Skatteverket (begäran om utbetalning)

### Channels

- **The e-service "Rot och rut – företag"** requires e-legitimation (BankID or similar). For a limited company, the person who submits must be registered as an ombud (representative) in "Ombud och behörigheter". A sole trader does not need this.
- The company must register a payout account with Skatteverket.
- Per buyer, the request contains:
  - actual hours and material cost per work type;
  - other costs;
  - labour cost (incl. VAT);
  - paid amount (labour after RUT);
  - requested amount.
- Invoices are not attached. Skatteverket asks for them if needed.
- Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### File import

- The request can be created as an **XML file** (Begaran.xsd plus BegaranCOMPONENT.xsd) and uploaded to the same e-service.
- The current schema is **XML version 6**, valid from 1 Jan 2021 and covering all RUT services, including moving.
- ROT and RUT cannot be mixed in one file, and neither can different payment years.
- Source: [Skatteverket – Schema för rot och rut](https://www.skatteverket.se/foretag/etjansterochblanketter/allaetjanster/schemalagerxml/rotochrutforetag.4.71004e4c133e23bf6db800063583.html).

Validation rules:

- the personnummer must be formally correct, and not the same as the performer's;
- the buyer must be 18 or older during the payment year;
- the payment date must not be after the application date;
- requested ≤ paid;
- requested + paid ≤ labour cost;
- all buyers must belong to the same payment year;
- hours and material must be given for at least one work type;
- 1–100 buyers per request.

Source: [Regler för att importera fil](https://www.skatteverket.se/foretag/etjansterochblanketter/allaetjanster/schemalagerxml/rotochrutforetag/reglerforattimporterafiltillrotochrut.4.76a43be412206334b89800033198.html).

### Decisions export

Decisions (beslut) can be downloaded from the e-service as a **JSON** file to match payouts against invoices. Source: [Schema för rot och rut](https://www.skatteverket.se/foretag/etjansterochblanketter/allaetjanster/schemalagerxml/rotochrutforetag.4.71004e4c133e23bf6db800063583.html).

### API / machine-to-machine

⚠️ None found. Skatteverket only documents manual entry, XML upload or the paper/digital form SKV 4541. A person has to log in with e-legitimation and submit. We could not confirm that no API exists, so check Skatteverket's API portal (developer.skatteverket.se) before committing to this assumption.

### Deadline

The request must reach Skatteverket by **31 January of the year after the customer paid**. If the customer prepays in December, the work must also be done by 31 January. Late requests cannot be fixed by re-invoicing. Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

### Rejections, corrections and repayment

- If Skatteverket pays out less or rejects the request, the company claims the difference from its customer. ⚠️ Standard practice, but this is not stated on the page cited.
- Corrections, supplements and requests for review (omprövning) go through "Ställ en fråga eller lämna ett svar".
- Repayment uses form **SKV 4533**. One example is when insurance later covers the work and it happens in the same year.
- Payouts can be set off against the company's tax debts.
- After-the-fact constructions are not allowed. For example, you cannot credit an invoice and re-bill after a cash payment or a late application.
- Source: [Skatteverket företag](https://www.skatteverket.se/foretag/skatterochavdrag/rotochrut/safungerarrutavdraget.4.64a656d113f4c7597011e41.html).

## 3. Accounting integrations

### Fortnox

**Auth**

- OAuth2 Authorization Code flow.
- Access token lasts 1 hour. The refresh token lasts 45 days and rotates on every use, so the old one becomes invalid.
- Scopes are fixed when the customer activates the integration. Adding scopes later means the customer has to reactivate.
- Sources: [Fortnox authorization](https://www.fortnox.se/developer/authorization), [Developer Portal](https://www.fortnox.se/developer/developer-portal).

**Becoming a partner**

- You register as an integration developer, which requires a Fortnox company account. Fortnox adds a developer licence.
- You then publish through the Developer Portal. A landing page is required.
- Sources: [Developer Portal](https://www.fortnox.se/developer/developer-portal), [Integrationspartner](https://www.fortnox.se/om-fortnox/partners/integrationspartner).

**Cost and business model**

- No fee to become a partner was found. ⚠️ Confirm with Fortnox.
- Option 1, "Buy via Fortnox": Fortnox bills the customer and the partner gets **75 %** (Fortnox keeps 25 %). A 30-day trial is mandatory. Sources: [fortnox.se/developer](https://www.fortnox.se/developer), [App market EULA](https://apps.fortnox.se/api/eula/document-v1/appmarket).
- Option 2, sell directly: the customer instead needs a Fortnox **integration licence**, about **189 SEK/month**, covering unlimited integrations. Source: [fortnox.se/integrationer](https://www.fortnox.se/integrationer). ⚠️ The price may change.

**Endpoints** ([API docs](https://api.fortnox.se/apidocs))

- `/3/customers`
- `/3/articles`
- `/3/invoices`
- `/3/taxreductions`

**RUT support** — yes, Fortnox invoices can carry RUT data:

- `Invoice.TaxReductionType = "rut"`;
- each row has `HouseWork`, `HouseWorkType` and `HouseWorkHoursToReport`;
- RUT work types include `CLEANING` and `MOVINGSERVICES`;
- non-work rows use `OTHERCOSTS` or `EMPTYHOUSEWORK`;
- the buyer and amount per person go in a TaxReduction record.

Sources: [Hubhus Fortnox guide](https://support.hubhus.com/support/solutions/articles/80001214864-fortnox-direct-integration), [Fortnox dev blog on TaxReductionAmounts](https://www.fortnox.se/developer/blog/deprecation-worktype-and-addition-taxreductionamounts).

Fortnox can produce the file for Skatteverket from these invoices, including "hours to report" for fixed-price work. Source: [Fortnox support – Skapa husfaktura](https://support.fortnox.se/produkthjalp/fakturering-standard/fakturering-standard-skapa-husfaktura).

### Spiris (formerly Visma eEkonomi)

**Auth**

- OAuth2 via `identity.vismaonline.com`.
- Required scopes: `ea:api` and `offline_access`. Area scopes: `ea:sales`, `ea:accounting` and others.
- The authorization code lasts 5 minutes and the access token 1 hour.
- Partner credentials come from registering for the Spiris Partner Programme.
- Sources: [developer.vismaonline.com – Authentication](https://developer.vismaonline.com/docs/authentication), [Overview](https://developer.vismaonline.com/docs/overview), [spiris.se/partner/developer](https://www.spiris.se/partner/developer).
- ⚠️ Partner fees and conditions are not published. Ask them.

**RUT support**

- **Invoice level:** `RotReducedInvoicingType`, `RotPropertyType`, `RotReducedInvoicingOrgNumber`.
- **Invoice amounts:** `RotReducedInvoicingAmount` and related fields.
- **Row level:** `IsWorkCost`, `WorkCostType`, `WorkHours`, `MaterialCosts`.
- **Article level:** `HouseWorkType`.

Source (secondary, ⚠️ verify against the official OpenAPI): [Maesn – Spiris ROT/RUT](https://www.maesn.com/blog/how-to-integrate-with-spiris/). A different secondary source shows different field names: [Apideck](https://www.apideck.com/blog/spiris-visma-eaccounting-api-integration). Treat field names as unverified.

### Market share

- **Fortnox is the clear leader** among Swedish small businesses: about 612 000 customers (Q1 2025) and about 37 % market share.
  - Fortnox's own segment figures: 34 % of micro companies (0–4 employees) and 60 % of companies with 5–9 employees.
  - Sources: [Placera](https://www.placera.se/analys/analys-stenen-i-skon-hos-fortnox-ar-varderingen-2025-01-10), [Apideck Sweden](https://www.apideck.com/integrations/country/sweden/lang/sv).
- Spiris/Visma and Bokio (owned by Visma) come next. Source: [bytredovisning.se](https://bytredovisning.se/guide/bokforingsprogram-jamforelse).
- ⚠️ No industry-specific figures for cleaning or moving firms were found.

## 4. Nordic notes

- **Norway:** there is no RUT equivalent when buying from a company. The proposal was rejected by Stortinget in 2011. Sources: [Vaskeladden](https://help.vaskeladden.com/en/kunde/skattefradrag-norge-sverige), [Stortinget](https://www.stortinget.no/no/Saker-og-publikasjoner/Saker/Sak/?p=50263).
- **Denmark:** servicefradrag allows up to 18 300 DKK per person in 2026, worth about 26 %. The customer claims it themselves in TastSelv, so the company does not claim it. Source: [skat.dk](https://skat.dk/borger/fradrag/servicefradrag/servicefradrag).
- **Finland:** kotitalousvähennys gives 40 % of the labour share. The maximum is €2 100 per person in 2026–27, and €150 of costs is paid by the person themselves. The customer claims it. Source: [vero.fi](https://www.vero.fi/henkiloasiakkaat/vahennykset/kotitalousvahennys/).

Only Sweden uses the model where the company invoices net of the deduction and claims the money itself. This makes Sweden the hardest market to support.

## 5. GDPR and personnummer

- **Legal basis**
  - Processing is needed to perform the contract (art. 6.1 b) and to meet legal obligations: the claim to Skatteverket and bookkeeping (art. 6.1 c).
  - Personnummer additionally requires that use is "klart motiverat" (clearly justified) under **3 kap. 10 § dataskyddslagen (2018:218)**. A RUT claim, where secure identification is legally needed, should meet that bar. ⚠️ This is our interpretation.
  - Sources: [Riksdagen – SFS 2018:218](https://www.riksdagen.se/sv/dokument-och-lagar/dokument/svensk-forfattningssamling/lag-2018218-med-kompletterande-bestammelser_sfs-2018-218/), [IMY – Personnummer](https://www.imy.se/verksamhet/dataskydd/det-har-galler-enligt-gdpr/introduktion-till-gdpr/personuppgifter/personnummer/).
- **Minimise exposure:** IMY says personnummer should be exposed as little as possible. Mask it in the UI and in field workers' apps, and never put it on job sheets or in SMS.
- **Retention:** accounting information, including invoices with personnummer, must be kept **until the end of the 7th year after the end of the calendar year in which the financial year ended**. It must be stored in Sweden, or in the EU if the location is reported to Skatteverket. Sources: [Bokföringslagen 7 kap. 2 §, 3 a §](https://www.lagar.se/lag/sfs-1999-1078/kapitel-7/), [BFN](https://www.bfn.se/fragor-och-svar/arkivering/).
  - If the accounting system (Fortnox) holds the official invoice, our app may not need to keep personnummer that long. Delete or pseudonymise it after the claim decision plus a buffer.
  - ⚠️ This depends on whether our system counts as an accounting record (räkenskapsinformation) for the customer.
- **Roles:** the cleaning or moving company is the data controller. We are a **data processor (personuppgiftsbiträde)** and need a DPA (data processing agreement). EU hosting is the simplest option.

## 6. Product implications

### Requirements and risks for the MVP

- **Data model**
  - Job lines need a cost type: labour, material, travel/other, or no reduction.
  - Each line needs a RUT service type: cleaning or moving.
  - Record **actual hours worked** per service type (on site for cleaning; including transport time for moving), separately from billed hours, because fixed-price jobs still need actual hours.
- **Customers**
  - Private or company flag.
  - Personnummer, validated with the Luhn check digit (the check digit in the personnummer); buyer must be 18+ in the payment year.
  - Support 1–n buyers per job, with a split that never exceeds 50 % of labour.
- **Quotes and invoices**
  - Show labour, material and travel separately, with VAT, the RUT amount and the amount to pay.
  - Mark F-skatt and the service type.
  - Make sure travel/call-out is charged by default; warn if it is zero.
- **Moving catalogue**
  - Default truck, fuel, boxes and storage to the non-RUT cost types.
  - Default packing, carrying and driver time to RUT labour.
- **Payment tracking**
  - Record the payment date and an electronic payment method. Block RUT claims for cash payments.
  - A job is claimable only when it is completed and paid.
- **Claims**
  - Group claimable items by payment year.
  - Remind users before **31 January**. This is the main business risk: missing it means lost revenue.
  - Track status: claimed, approved, rejected, or partly approved with a balance to collect from the customer.
- **Risks**
  - Allowance is invisible: the customer may have used it up, which leads to rejection and a follow-up invoice. Tell customers about this in the quote terms.
  - Rules and XML versions change.
  - Personnummer leaks.
  - Submission cannot be fully automated because it requires BankID.

### Recommendation

> **Superseded by DECISIONS.md 2026-10-29 Product scope:** the MVP includes RUT XML-file generation (Begaran.xsd v6), payment tracking and the 31 Jan reminder. The Fortnox integration is the first post-MVP item, not an MVP item, and Spiris comes later. Where the list below conflicts, DECISIONS.md wins.

- **MVP**
  - Correct RUT-ready quotes and invoices (the split, buyers and actual hours).
  - Payment tracking.
  - A "RUT claims due" list with a deadline reminder.
  - **Generate the Skatteverket XML (v6, RUT only) for the user to upload.**
  - ~~One accounting integration: **Fortnox**.~~ (Superseded: first post-MVP item.) It is the largest and its API carries RUT natively. Push customers, articles and invoices with `HouseWork` fields so that Fortnox can also create the Skatteverket file.
  - Pick one path per customer to avoid double claims: our XML or Fortnox's file.
  - Masked personnummer, DPA, EU hosting.
- **Later**
  - Spiris/Visma integration.
  - Import of decision JSON and automatic matching of payouts.
  - Handling of partial approvals and re-invoicing.
  - Partial payment and prepayment edge cases.
  - Factoring.
  - Fortnox marketplace listing ("Buy via Fortnox", 25 % commission).
  - ROT and green tech.
  - Denmark and Finland, where only an invoice breakdown is needed and no claim flow.
- **Not planned:** any Skatteverket API submission. None was found to exist.
