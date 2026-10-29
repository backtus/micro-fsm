# Norway competitors: field-service software for small cleaning and window-cleaning companies

Researched 2026-10-29. Scope: Norwegian renhold and vindusvask companies with 1–30 employees. ✅ = I checked this on the vendor's own page on the research date. ⚠️ = unverified (it comes from a third party, from memory, or the page did not load). The research was time-boxed, so several candidates on the brief were not checked one by one (see §6).

## TL;DR

- **The market is not empty, and that changed in 2025–26.** At least two Norwegian products now target exactly this segment. **Between** (Oslo) is a horizontal field-service product with dedicated renhold *and* vindusvask landing pages and integrations to all the major Norwegian accounting systems. **Effiora** (Lørenskog) launched in 2026 for renhold and vaktmester at 690 kr/month. Between is the closest thing to a Norwegian Wisegent or Jobber.
- **Wisegent itself is Sweden-only in practice.** It is built around RUT and Fortnox, with servers in Sweden. I found no evidence of Norwegian or Norway-specific support.
- **The weakest-served niche is window cleaning in particular:** route ("runde") planning, frequency per customer (every 4, 8 or 12 weeks), weather-driven rescheduling, door-to-door B2C plus small B2B, and pay-per-visit invoicing via Vipps or eFaktura. Between has a vindusvask page, but its core model is generic jobs. Nobody Norwegian looks like Squeegee (UK).
- **Verdict:** there is room, but not for a generic "cheap Norwegian cleaning FSM". A defensible wedge is **window-cleaning-rounds-first, phone-first, Vipps + Fiken/Tripletex, priced per company at about 199–399 kr/month**. Validate this in Trondheim before building anything.

## 1. Landscape: what Norwegian cleaning firms use

### 1a. Cleaning/field-service-specific products sold in Norway

| Product | Origin | Positioning | Norwegian UI | Norway compliance | Accounting integrations | Price (NOK, ex. VAT) | Target size |
|---|---|---|---|---|---|---|---|
| **Between** ✅ | Oslo (Between AS) | Horizontal field-service "operating system" for 30+ trades, with dedicated pages for renhold, vindusvask, fasadevask and flyttebyrå. Covers fixed agreements with recurring job plans, temporary jobs, geo-locked check-in, quality forms with photo and PDF to the customer, deviations, invoice plans, customer portal, quotes, CRM and HMS. Claims to be "Norges raskest voksende system innen renhold". | Yes | HMS module and quality/deviation handling. No explicit HMS-kort or renholdsregister feature seen. | Tripletex, Fiken, PowerOffice Go, 24SevenOffice, Finago, Unimicro (plus DNB, SpareBank 1, Eika and Azets via Unimicro), Fortnox, Systima | Not public (/priser returns 404) ⚠️ | ENK up to "several hundred employees" |
| **Effiora** ✅ | Lørenskog (HI Ventures) | Shift and roster planning per assignment site, GPS time stamping (works offline), QR documentation the customer scans, absence and substitute handling, payroll basis with night, weekend and holiday hours, app in 8 languages, chat with translation. Featured in Renholdsnytt in Oct 2026 as "Vil digitalisere små renholdsbedrifter". | Yes, plus 7 immigrant-worker languages | Norwegian payroll concepts (egenmelding, sykt barn, helligdag). No HMS-kort or renholdsregister feature seen. | Payroll basis exported as Excel. No accounting integration listed. | 690 kr/month for 2–9 employees, 1 490 for 10–24, 2 490+ for 25+. These are launch prices; from 1 Jan 2027 they become 990 / 2 290 / 3 990+. | 2–25+ |
| **CleanCat** ⚠️ | Norway | "Developed from scratch, by and for cleaners", by former cleaning-agency owners (Renholdsnytt, Nov 2023). Article is paywalled. | Yes (presumably) | Unknown | Unknown | Unknown | Small cleaning agencies |
| **CleanPilot** (Datec AS) ✅ | Heimdal, Trondheim | "Nordic leader in Smart Cleaning". Area- and plan-based cleaning, INSTA 800-style calculations, sensors and robots. Customers include hospitals and municipalities (OUS, Buskerud FK). New investor Driv Kapital (Sep 2026). | Yes | INSTA 800 / quality focus | ERP integrations | Enterprise ⚠️ | Large in-house and FM operations, not micro firms |
| **M_SOLUTION** (Giant Leap, owned by Visma) ✅ | Trondheim (NTNU spin-out) | Configurable mobile planning and QA for service companies (NFC, QR, GPS). About 300 customers. | Yes | Configurable | Visma | Enterprise ⚠️ | Mid and large |
| **Wisegent** ✅ | Stockholm | All-in-one for cleaning firms: AI scheduling, time tracking, key management, RUT invoicing, Fortnox. 2 387 users across "4 countries". | Not seen. Swedish/English site; demos in English only. | RUT (Swedish). Nothing Norwegian. | Fortnox | 199 SEK/month Starter, 499 SEK/month Growth | Small Swedish cleaning firms |
| **Drifti** ✅ | Oslo | Field-service system for tradespeople (carpenters, painters): quotes, planning, time, wholesaler link, invoicing | Yes | Construction-focused | Tripletex, PowerOffice, Fiken | Per company by headcount; not public | Small trades, not cleaning |
| **Infobric Workorder**, evolvit / Contracting Works ✅ | Nordic | Work orders for construction and installation | Yes | Construction | ERP | Quote-based | Mid-size trades |
| **EasyPlan** ✅ | Norway | Route and worklist planning for drivers (snow clearing etc.) | Yes | – | – | 4 280 kr/driver/year | Route-based services; a possible window-cleaning substitute |
| **Appreo** ✅ | NL | Planning, payroll and invoicing for cleaning, security etc. | No (NL/EN) | NL | NL | – | Not in Norway |

### 1b. Generic tools cleaning firms in Norway commonly fall back to (not individually verified ⚠️)

- **Rostering and time tracking:** Tidsbanken, Planday, Quinyx, Simployer (HR), SmartDok (construction-oriented, HMS-kort reading).
- **Accounting with built-in project, hour and recurring invoicing:** Tripletex (timeføring and prosjekt modules), PowerOffice Go (time and travel in the app), Fiken (repeating invoices only).
- **Craftsman field-service tools:** Cordel, Craftboxx, Kvalitetskontroll, Moment (Visma), Fieldly. These target electricians and carpenters rather than recurring cleaning.
- **International SMB field-service tools:** Jobber, Housecall Pro and Swept (US janitorial), and Squeegee (UK window cleaning). None localises to Norwegian, Vipps, EHF/eFaktura or the Norwegian accounting systems. Squeegee's own pricing page did not render (⚠️); per-customer "round" pricing in GBP is typical.
- **Booking:** Bokamera (and similar booking widgets) for B2C bookings.
- **Default reality:** Excel or Google Sheets, a paper calendar, SMS or WhatsApp to staff, and invoicing directly in Fiken or Tripletex.

**Window-cleaning-specific Norwegian software: none found.** Searches for "program for vindusvask", "rutekjøring vindusvask" and similar returned nothing relevant beyond Between's generic vindusvask page and EasyPlan's driver-route tool. (A "Vindusvask" landing page appears in Between's navigation, but I did not open it.)

### 1c. Norway-specific compliance context

- **Godkjenningsordningen for renhold (renholdsregisteret, Arbeidstilsynet):** since 1 Sept 2012 it is illegal to buy cleaning services from a company that is not approved, and cleaners must carry the renhold **ID-kort** (source: Renholdsnytt archive via xpertrenhold.no; Arbeidstilsynet's own register, linked from citymaid.no). It would be cheap for a product to show the company's approval status and track each employee's ID-card expiry. No competitor highlights this.
- **Allmenngjort lønn (minimum generally-applicable pay) in renhold:** kr 236.54 from 15 June 2025. The minimum tariff wage is kr 247.29 from 1 June 2026 (Renholdsnytt). Overtime rules for extra hours ("overtid for mertid") reportedly add 700 MNOK in costs to the sector (Renholdsnytt 2026). This matters for payroll-basis features: a tool that flags pay below the tariff or missing overtime has real value.
- **NS-INSTA 800** quality standard: relevant mainly for B2B and public contracts. CleanPilot owns that space. It is mostly irrelevant for window cleaning.
- **Window cleaning** is approval-exempt in the renhold scheme only in some cases (⚠️ verify with Arbeidstilsynet). Height work and HMS (ladders, lifts) is the more relevant compliance area.

## 2. Accounting systems used by small Norwegian companies

There is no single public market-share study. The best available signals are customer counts the vendors state themselves:

| System | Customers (vendor-stated) | Owner | Entry price (NOK/month, ex. VAT) | API / partner notes |
|---|---|---|---|---|
| **Tripletex** ✅ | "Over 160 000 bedrifter", "Norges mest valgte" ([tripletex.no](https://www.tripletex.no/)) | Visma | 199–649 (Luca comparison) | Public REST API (v2). Integrations require a partner/consumer token; listed in the Tripletex marketplace ⚠️ |
| **Fiken** ✅ | ">130 000" (third party, 2020-era ⚠️). Revenue 464 MNOK in 2025, up from 223 MNOK in 2022 ([proff.no](https://www.proff.no/selskap/fiken-as/oslo/dataprogramvare-og-utvikling/IF3RGEP009O)) | Independent (Visma's main challenger) | 219 flat ([blogg.fiken.no](https://blogg.fiken.no/fiken-regnskap)) | Public REST API v2 with OAuth2 for third-party apps. Free for developers; app registration required ⚠️ |
| **PowerOffice Go** ✅ | "100 000+ kunder, 250 000+ brukere" ([poweroffice.no](https://www.poweroffice.no/)). Highest customer satisfaction in Regnskap Norge's survey 2020–25. | Visma | ~100–425 | Go API v2. Partner agreement and application key required. Marketplace with 300+ integrations ⚠️ |
| **Visma eAccounting** | ⚠️ | Visma | 159–279 | Visma eAccounting API; Visma Developer partner programme ⚠️ |
| **Conta** | ⚠️ | Independent | 209–339 | Public API ⚠️ |
| **24SevenOffice** | ⚠️ | Independent | ⚠️ | SOAP/REST API; Between integrates with it ✅ |

Prices come from the Luca Regnskap comparison, Oct 2026 ([lucaregnskap.no](https://www.lucaregnskap.no/regnskap/regnskapsprogram-for-sma-bedrifter-dette-er-viktigst-nar-du-skal-velge)).

**Implication:** for 1–30-employee service firms, Tripletex and Fiken probably cover the large majority, with PowerOffice Go third, mostly via accounting bureaus. Visma owns Tripletex, PowerOffice and eAccounting. **Build Fiken and Tripletex first, PowerOffice second.** Between already integrates with all of them, so integrations alone are not a differentiator; they are table stakes.

## 3. Gaps: what a 1–30-person firm cannot get well today

### Office and home cleaning (renhold)

- Fairly well served now. Between covers recurring plans, quality documentation and invoicing. Effiora covers rosters, payroll basis and immigrant-language apps.
- Remaining gaps are mostly about **price and simplicity for 1–5-person firms**, and about **compliance helpers**: ID-kort expiry, approval status, tariff wage checks and overtime flags. No one markets these.
- Effiora is the main threat for low-price roster and time tracking: it is cheap, Norwegian and multilingual, but it has no invoicing or accounting integration.

### Window cleaning (vindusvask): the clearer gap

Window cleaning works differently from office cleaning:

- **Rounds, not shifts.** Hundreds of small customers sit on frequencies (4, 8 or 12 weeks, or spring and autumn) and are grouped geographically into a daily "runde". Recurring jobs per customer, not rosters per site.
- **Weather and season.** Rain and frost push the whole round, and Trondheim winters halt exterior work. "Skip and re-queue" and "push the round 1 day" need to be single actions.
- **Pay per visit.** Many low-value invoices (300–800 kr each). The firm needs Vipps, eFaktura/AvtaleGiro, EHF for B2B (housing co-ops, shops), and automatic "done today, invoice sent" messages.
- **Mixed B2C and small B2B** (borettslag, shops, offices), often with add-ons: gutters, facade, solar panels.
- **Owner works in the field**, so the product must be phone-first with minimal office time.

What exists: Squeegee (UK) does exactly this but is not localised. Between is generic. EasyPlan is route-based but not built for cleaning or invoicing. **I found no Norwegian Squeegee and no Norwegian Wisegent focused on window cleaners.**

## 4. Is there a Wisegent equivalent in Norway?

**Partly.** Between is functionally the closest: all-in-one, cleaning-aware and Norwegian-integrated. It is horizontal across 30+ trades, though, its pricing is not public (probably higher than Wisegent's 199–499 SEK), and it is sales- and demo-led. Effiora is a cheaper but narrower renhold-only alternative without invoicing. **Nobody combines Wisegent-level simplicity and price (about 200–500 kr/month, self-serve signup) with Norwegian invoicing and accounting.** Nobody serves window-cleaning rounds specifically.

## 5. Verdict and positioning

**Is there room?** Yes, but it is narrower than it looked a year ago. A generic "Norwegian cleaning FSM" would be competing head-on with Between (funded, broad, strong integrations) and Effiora (cheap, launched 2026). Do not build that.

**Most defensible positioning, in order:**

1. **Window-cleaning-rounds-first** (recommended). Rounds and frequencies, weather rescheduling, a map-ordered day list, automatic Vipps or eFaktura per visit, SMS "kommer i morgen" and "ferdig", Fiken and Tripletex sync. Phone-first, self-serve, priced per company at about 199–399 kr/month. The niche is small, so expand later to facade, gutters and solar-panel cleaning (same round model) and then to small home-cleaning firms. The founder's Trondheim window-cleaning network is a real distribution edge here.
2. **Simplicity and price for 1–5-person renhold firms.** Self-serve, cheaper than Between, and with invoicing (which Effiora lacks). This is weaker because it is easy to copy and Effiora already anchors the price at 690 kr.
3. **Compliance-first** (ID-kort, renholdsregister, tariff checks). This is a good *feature set*, not a product on its own: low willingness to pay, and not relevant to window cleaning. Bundle it into option 2 later.

**Key risks:** the market is small (window-cleaning firms in Trøndelag number in the low hundreds at most ⚠️). Between could ship round features. Seasonality hurts both the firms' cash flow and the product's churn.

## 6. Five questions for the founder's network

1. "Walk me through last Monday: how did you decide which customers to do, in what order, and what happened when it rained or someone wasn't home?" (Tests whether round and weather pain is real.)
2. "How do you invoice today, per visit or monthly? Which tool, how long does it take each week, and how much is unpaid after 30 days?" (Tests whether Vipps, eFaktura and accounting sync is the hook.)
3. "Which accounting system do you or your regnskapsfører use, and who would need to approve switching or connecting a new tool?" (Tests Fiken vs Tripletex vs PowerOffice priority and the bureau's role.)
4. "Have you tried Between, Effiora, Tripletex timeføring, Squeegee or anything else? Why did you stop, or never start?" (Finds real competitors and objections.)
5. "If a phone app removed X hours a week of admin, what would you pay per month? Would you pay for it today, before it's finished, if it worked with your customer list?" (Tests willingness to pay and early commitment; ask for a letter of intent or pre-order.)

## Sources

- Between, renhold page: https://between.as/bransjer/programvare-for-renholdsbedrifter (✅ fetched). Taktekker page with example renhold job: https://between.as/bransjer/system-for-taktekker
- Effiora: https://www.effiora.no/renhold and https://www.effiora.no/priser (✅ fetched)
- Renholdsnytt, "Vil digitalisere små renholdsbedrifter" (Oct 2026): https://www.renholdsnytt.no/2026-dataverktoy-kvalitetssikring/vil-digitalisere-sma-renholdsbedrifter/1571499 (paywalled)
- Renholdsnytt, CleanCat (Nov 2023): https://www.renholdsnytt.no/2023-bransjenytt-dataverktoy/ny-programvare-for-renholdsbedrifter/979777
- Renholdsnytt, Datec/Driv Kapital (Sep 2026): https://www.renholdsnytt.no/2026-bedrifter-og-organisasjoner-bransjenytt/driv-kapital-blir-ny-vekstpartner-i-datec/1562791
- CleanPilot: https://www.cleanpilot.com/
- M_SOLUTION / Giant Leap: https://msolution.no/om-oss
- Wisegent: https://app.wisegent.se/en and https://wisegent.se/en. Founder: https://kostrzewski.se/
- Drifti: https://drifti.no/tomrer
- Infobric Workorder: https://infobric.com/no/produkter/workorder
- EasyPlan: https://easyplan.no/
- Squeegee: https://squeeg.ee/professional-window-cleaner-software (pricing page did not render)
- Tripletex: https://www.tripletex.no/
- PowerOffice: https://www.poweroffice.no/
- Fiken price: https://blogg.fiken.no/fiken-regnskap. Fiken financials: https://www.proff.no/selskap/fiken-as/oslo/dataprogramvare-og-utvikling/IF3RGEP009O
- Accounting price comparison (Luca Regnskap, a competitor-authored source): https://www.lucaregnskap.no/regnskap/regnskapsprogram-for-sma-bedrifter-dette-er-viktigst-nar-du-skal-velge
- Regnskap Norge Teknologiundersøkelsen 2025, via Tripletex: https://www.tripletex.no/fagblogg/regnskap/beste-regnskapsprogram-med-lonn-og-arsoppgjor/
- Godkjenningsordning and ID-kort (Renholdsnytt archive): https://xpertrenhold.no/arkiv.html. Arbeidstilsynet register example: https://www.arbeidstilsynet.no/renholdsvirksomhet/948283697

**Not verified in this pass:** Tidsbanken, Planday, Quinyx, SmartDok, Cordel, Craftboxx, Kvalitetskontroll, Simployer, Visma Moment, Fieldly, Jobber, Housecall Pro, Swept, Cleaner Planner, Aworkspace, Upflow/Serviq, Bokamera, Easyserv. Accounting API terms are also unverified. Check these before making pricing or integration decisions.
