# Norway: regulation and market for small cleaning and window-cleaning businesses

Researched 2026-10-29. ⚠️ means the point is uncertain, inferred or likely to change soon. This is not legal advice.

## 1. Renholdsregisteret and the approval scheme (godkjenningsordningen)

- **Who must be approved:** every business that "helt eller delvis" (fully or partly) offers cleaning services, including staffing agencies that hire out cleaners. Sole traders must be approved too, with or without employees. Businesses that only clean their own premises with their own staff are exempt. Selling cleaning services without approval is illegal. Sources: [FOR-2012-05-08-408 §§2–3](https://lovdata.no/forskrift/2012-05-08-408), [Arbeidstilsynet – søk om godkjenning](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/).
- **Window cleaning is in scope.** Arbeidstilsynet's consumer page names "vindusvask" alongside flyttevask and trappevask ([renholdsregisteret](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/)). The open register filters on NACE 81.2x, which includes 81.22 "utvendig rengjøring av bygninger", and it lists window firms such as 123VINDUSPUSS AS ([register XML](https://registerdata.arbeidstilsynet.no/renhold_register.xml)). ⚠️ The regulation itself does not define "renhold". Borderline services such as facade washing at height, gutter cleaning or pressure washing should be checked case by case.
- **Private households:** the ban on buying from unapproved firms also applies to consumers buying cleaning for their own home ([Arbeidstilsynet – innkjøpers plikter](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/renholdskjopers-plikter/), [§17](https://lovdata.no/forskrift/2012-05-08-408)).
- **Requirements for firms with employees ([§5](https://lovdata.no/forskrift/2012-05-08-408)):**
  - an approved occupational health service (BHT)
  - a safety representative (verneombud) from 5 employees, an "annen ordning" (alternative arrangement) below 5, and a working environment committee (AMU) from 30
  - written employment contracts
  - compliance with the generally applicable collective agreement (allmenngjøring)
  - occupational injury insurance (yrkesskadeforsikring)
  - registration in Enhetsregisteret, the VAT register, Aa-registeret and similar registers
- **Application documents:** the application goes through Altinn. Attachments include the BHT contract and a paid BHT invoice, one sample contract per employee group, payslips that show the hourly rate, and the insurance certificate ([Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/)). Firms without employees attach nothing. A decision takes 1–8 weeks, and the firm may legally trade while its status is "søknad under behandling" (application being processed).
- **Renewal:** firms send an annual confirmation (årlig melding) on the anniversary of the approval decision. Every third year they must also send full documentation. If a deadline is missed, approval can be revoked ([§7](https://lovdata.no/forskrift/2012-05-08-408)). This recurring deadline is a good thing for software to track.
- **Cost:** ⚠️ I found no application fee on Arbeidstilsynet's pages, so it appears to be free. The real costs are BHT, insurance and HMS cards (see section 2).
- **Enforcement:** Arbeidstilsynet runs inspections, which may be announced or unannounced. Sanctions include orders (pålegg), coercive fines (tvangsmulkt), administrative fines (overtredelsesgebyr), revocation of approval and criminal liability under aml. ch. 18–19 ([Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/), [§§18–20](https://lovdata.no/forskrift/2012-05-08-408)).

## 2. HMS-kort for cleaning

- **Who needs a card:** every worker who does cleaning, plus sole traders, partners in ANS/DA companies and a sole trader's spouse if they clean ([§9](https://lovdata.no/forskrift/2012-05-08-408), [Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/)).
- **Issuing:** the employer orders cards at [hmskort.no](https://hmskort.no/), operated by Tieto on behalf of Arbeidstilsynet, after Arbeidstilsynet has cleared the firm. The employer must check the worker's identity before ordering. A card costs **NOK 139.10 + VAT** from 1 Feb 2026 ([hmskort.no](https://hmskort.no/news/ny-pris-per-hms-kort-fra-1-februar-2026)).
- **Validity:** 2 years, and only while the employment lasts ([§11](https://lovdata.no/forskrift/2012-05-08-408)).
- **Employer duties:** cancel the card immediately when employment ends, when the card expires or when approval is revoked ([§11a](https://lovdata.no/forskrift/2012-05-08-408)), and report lost cards ([§14](https://lovdata.no/forskrift/2012-05-08-408)).
- **Checks:** the card must be worn visibly. Workers must show it on request to Arbeidstilsynet, the tax authorities, the police, the client (oppdragsgiver), the safety representative and the main contractor ([§13](https://lovdata.no/forskrift/2012-05-08-408)).
- **Software opportunity:** track card expiry per worker, flag cards that need cancelling when someone leaves, and show card status on the B2B customer's documentation.

## 3. Buyer duties and register lookup

- **All buyers**, both businesses and consumers, may only buy from firms with status godkjent (approved), søknad under behandling (application being processed) or HMS-kortbestilling under behandling (card order being processed) ([§17](https://lovdata.no/forskrift/2012-05-08-408)). Buying from an illegal firm "kan medføre straffeansvar" (can lead to criminal liability) ([Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/renholdskjopers-plikter/)).
- **Business buyers**, meaning buyers engaged in commercial activity, have three duties:
  - an information duty: the contract must state the minimum terms under the generally applicable agreement
  - a duty to check (påseplikt) that the supplier complies
  - the contract must give the buyer access to wage and terms documentation, and set sanctions such as termination (heving) or withholding payment
  
  Sources: [Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/renholdskjopers-plikter/), [informasjons- og påseplikt](https://www.arbeidstilsynet.no/lonn-og-ansettelse/paseplikten/). Buyers must also coordinate HSE work with the supplier under aml. §2-2.
- **Consumers** have no duty to check (påseplikt). Their only obligation is to buy from a registered firm.
- **Effect on suppliers:** B2B customers will regularly ask small suppliers for documentation such as payslips or timesheets that show at least minimum wage, HMS card lists and proof of approval. ⚠️ How often this happens in practice is inferred, not measured.
- **Register data for automation:**
  - an open XML file, updated daily under the NLOD licence, with no API key: `https://registerdata.arbeidstilsynet.no/renhold_register.xml` (XSD: `RegisterXML6.xsd`) ([data.norge.no](https://data.norge.no/nb/datasets/b3642673-6855-3af9-93b0-fe6f01fbaa5d/renholdsvirksomheter-godkjent-av-arbeidstilsynet))
  - the file I downloaded today is about 22 MB, was generated 2026-10-29 03:03, and contains organisation number, name, NACE codes, approval status, contact details and sub-units
  - the same data is available through [data.altinn.no](https://docs.data.altinn.no/), which requires Maskinporten and an API key
  
  So an "approved ✅" badge, onboarding prefill by organisation number, and monitoring of subcontractor status are all simple to build.

## 4. Generally applicable collective agreement and solidarity liability

- **Current regulation:** [FOR-2024-10-21 as amended by FOR-2025-05-28-957](https://www.arbeidstilsynet.no/regelverk/forskrifter/forskrift-om-delvis-allmenngjoring-av-tariffavtale-for-renholdsbedrifter/). It applies to private businesses that sell cleaning services.
  - minimum wage **NOK 236.54/h**
  - under 18: NOK 185.55/h
  - work between 21:00 and 06:00: a supplement of at least NOK 29/h
  - travel time between back-to-back jobs counts as working time or must be paid separately
  - overnight jobs: the employer covers travel, board and lodging (by agreement, a fixed per diem or actual receipts)
  - the employer provides work clothes and shoes
- ⚠️ **The rates are likely to change soon.** The base agreement 2024–2026 has been replaced by "Renholdsoverenskomsten 2026–2028" ([lovdata TARO](https://lovdata.no/dokument/TARO/tariff/taro-10)). Tariffnemnda may set new rates, which happened mid-June in previous years. Store rates as data with effective dates; do not hard-code them.
- **Overtime:** the allmenngjøring regulation does not cover overtime, so the statutory rule applies: at least a 40% supplement ([aml. §10-6](https://lovdata.no/lov/2005-06-17-62/§10-6)). ⚠️ The tariff agreement itself, which binds only organised firms, has its own overtime, night and holiday rules ([TARO §§7–8](https://lovdata.no/dokument/TARO/tariff/taro-10)).
- **Solidarity liability:** contractors and their client are jointly liable for wages under the allmenngjøring regulation ([allmenngjøringsloven §13](https://lovdata.no/lov/1993-06-04-58/§13)). ⚠️ I did not verify how this applies to cleaning chains with subcontractors. Information duty, påseplikt and right of access: [FOR-2008-02-22-202](https://www.arbeidstilsynet.no/regelverk/forskrifter/forskrift-om-informasjons--og-paseplikt-og-innsynsrett/) ⚠️ (the FOR number is not verified).
- **Admin burden for the employer:**
  - pay at least the minimum rate and adjust when rates change. Arbeidstilsynet stresses this ([source](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/))
  - record travel time between jobs
  - track night hours
  - handle per diem for overnight jobs
  - hand payroll and timesheet documentation to B2B customers when they exercise their right of access

## 5. Other obligations

- **Working-time records:** the employer must keep a record of hours each employee has worked ([aml. §10-7](https://lovdata.no/lov/2005-06-17-62/§10-7)). Combined with the travel-time rule and the night supplement, cleaning firms need hours per job, per worker and per time-of-day band. This is a legal requirement.
- **A-melding:** monthly reporting of pay, hours and employment details to the a-ordningen ([Skatteetaten](https://www.skatteetaten.no/bedrift-og-organisasjon/arbeidsgiver/a-meldingen/)). This is normally handled by payroll software, so an export or integration is enough.
- **VAT:** cleaning is a standard-rated service at 25% ([Skatteetaten – satser](https://www.skatteetaten.no/satser/merverdiavgift/)). Registration is compulsory above NOK 50,000 turnover in 12 months ([mval. §2-1](https://lovdata.no/lov/2009-06-19-58/§2-1)). ⚠️ I found no special cleaning rule, and reverse charge does not apply domestically.
- **Household tax deduction:** **none exists.** NHO SH cites a study by Samfunnsøkonomisk analyse on *introducing* a tax-reduction scheme for home cleaning, and estimates that about 40% of home cleaning is undeclared work ([NHO SH Tall og Trender 2026](https://www.nhosh.no/tall-og-fakta/tall-og-trender/tall-og-trender-2026/tall-og-trender-2026/DriftogService26/)). ⚠️ Check budget news before relying on this, because NHO is lobbying for a scheme.
- **BHT:** compulsory for cleaning firms with employees ([aml. §3-3](https://lovdata.no/lov/2005-06-17-62/§3-3), [Arbeidstilsynet](https://www.arbeidstilsynet.no/godkjenninger/renholdsregisteret/godkjenningsordning-for-renholdsvirksomheter/)). It is also an approval condition.
- **Internal control (internkontroll / HMS):** written goals, a risk assessment, an action plan and deviation handling are required ([internkontrollforskriften §5](https://lovdata.no/forskrift/1996-12-06-1127/§5)). Under 5 employees, the documentation burden is reduced ⚠️. Relevant chemical risk is covered by [Arbeidstilsynet – kjemikalier](https://www.arbeidstilsynet.no/risikofylt-arbeid/kjemikalier/) (safety data sheets and a substance register).
- **Work at height (window cleaning):** this requires a risk assessment, collective fall protection before personal protection, and training. Ladders are allowed only for short, low-risk work. Lifts and fall-arrest equipment need documented training and inspection ([Arbeidstilsynet – arbeid i høyden](https://www.arbeidstilsynet.no/risikofylt-arbeid/arbeid-i-hoyden/), [forskrift om utførelse av arbeid kap. 17](https://lovdata.no/forskrift/2011-12-06-1357/KAPITTEL_17)). ⚠️ Lift operator training under ch. 10 applies when lifts are used.
- **B2B quality requirements (nice-to-have, but often in contracts):**
  - NS-INSTA 800 quality levels for cleaning, with checks on quality (kontroll)
  - service-level descriptions (rengjøringsbeskrivelser) per room or area
  - checklists, deviation reports and photo documentation
  
  ⚠️ I did not find a primary source in this pass that confirms how widely these appear in contracts; it is industry practice ([Standard Norge – INSTA 800](https://standard.no/)).

## 6. Market size

- **Register snapshot**, my own count from the [register XML](https://registerdata.arbeidstilsynet.no/renhold_register.xml) on 2026-10-29, main units (hovedenheter) only:

  | Status | Count |
  |---|---|
  | Approved without employees | 2,913 |
  | Approved with employees | 1,877 |
  | HMS card order in progress | 168 |
  | Application in progress | 71 |
  | Not approved | 2,974 |
  | **Legal total** | **about 5,030** |

  NHO SH says that "10 800 virksomheter inkludert underenheter" (10,800 businesses including sub-units) were approved in March 2024 ([NHO SH](https://www.nhosh.no/tall-og-fakta/tall-og-trender/tall-og-trender-2026/tall-og-trender-2026/DriftogService26/)). In early 2016 the figure was about 3,600 ([data.norge.no](https://data.norge.no/nb/datasets/b3642673-6855-3af9-93b0-fe6f01fbaa5d/renholdsvirksomheter-godkjent-av-arbeidstilsynet)).
- **Size distribution:** about 60% of approved firms have no employees. About 1,900 firms with employees make up the core target segment for 1–30 staff. ⚠️ The register does not give employee counts; Brønnøysund's `antallAnsatte` could be joined for exact size bands.
- **Window cleaning:** 320 entries carry NACE 81.220 (exterior/industrial cleaning). 135 have "VINDU" or "FASADE" in their name, of which 103 are approved (63 without employees, 40 with). ⚠️ This is a rough lower bound, because many window cleaners use generic names or code 81.210.
- **Market value and workforce:**
  - companies coded 81.21 had NOK 17.4 bn in revenue in 2023
  - municipalities spend a further NOK 23.4 bn on their own cleaning
  - 60,635 people had cleaner as their occupation in Q4 2025
  - 78% of cleaners are women and 66% are non-Western immigrants
  - 70% of cleaners work in the private sector
  
  Source: [NHO SH](https://www.nhosh.no/tall-og-fakta/tall-og-trender/tall-og-trender-2026/tall-og-trender-2026/DriftogService26/). That workforce profile points to a **multilingual, mobile-first UI**.
- **Customer mix:** NHO SH says most of these industries focus on the professional market (businesses and the public sector). Some firms also serve households directly or through housing cooperatives (borettslag) ([NHO SH](https://www.nhosh.no/tall-og-fakta/tall-og-trender/tall-og-trender-2026/tall-og-trender-2026/DriftogService26/)). ⚠️ I found no official B2B/household split. Small firms, and window cleaners in particular, probably have a larger share of household customers.

## 7. Implications: admin pain points that software could automate (1–30 people)

Ranked by likely value. **[L]** = legally required, **[C]** = commonly required by contract, **[N]** = nice-to-have.

1. **Hours per worker per job, with travel time between jobs and night (21–06) bands, ready for payroll and a-melding export [L].** Covers aml. §10-7 and the allmenngjøring rules, and is the core of a field-service product.
2. **Minimum-wage guard [L].** Rate table with effective dates, and a warning when an employee's rate is below the current rate. Also produces the evidence needed for B2B right-of-access requests and for the three-yearly documentation.
3. **Scheduling and recurring jobs, with route and travel-time capture [N, but needed for item 1].**
4. **Compliance calendar [L].** Annual report (årlig melding) and three-yearly documentation deadlines, HMS card expiry, cancelling cards when someone leaves, and BHT and insurance renewals. Missing the annual report can cost the firm its approval.
5. **"Documentation pack" for B2B customers [C/L for the buyer].** Approval status (from the open register), HMS card list, a minimum-wage statement and contract clauses. Answers the buyer's duty to check (påseplikt) with one click.
6. **Quality checklists, deviation reports and photos per job, following INSTA 800 [C].** These are often required in commercial contracts and give proof of work in disputes.
7. **Window cleaning: work-at-height risk assessment (SJA) template per site, and an equipment and training log [L under the work-at-height rules].**
8. **Invoicing with 25% VAT and per-visit or subscription pricing for households [N].** Integrate with Fiken, Tripletex and similar systems rather than rebuilding accounting.
9. **Internal control / HMS document templates (risk assessment, action plan, deviation log) [L, light for small firms].**
10. **Subcontractor check against the register XML [L for the buyer].** Useful for firms that buy in subcontractors.

Not worth building: payroll and a-melding themselves (integrate instead), or a household tax deduction (none exists today ⚠️).
