# micro-fsm web – klickbar prototyp

Mobile-first prototype for showing a design partner. All data is invented and there is no personnummer (a masked placeholder `ÅÅMMDD-XXXX` is shown). A fixed banner says "Prototyp – exempeldata". State lives in `localStorage` only; there is no backend.

## Run

Node 26 (see `.nvmrc`), npm 11.

```
npm --prefix web ci
npm --prefix web run dev      # http://localhost:5173
npm --prefix web test         # Vitest + React Testing Library
npm --prefix web run lint
npm --prefix web run build
```

Seed data is generated relative to today and regenerated automatically when the stored data was made on another day. "Återställ demo" resets it manually.

Notes: the RUT amount is rounded down to whole kronor (provisional, to confirm against Begaran.xsd). Rates are in `src/domain/config.ts`.

## Demo-manus (ca 5 min, öppna på mobil eller i 360 px bredd)

1. Notera bannern "Prototyp – exempeldata". Förklara att allt är påhittat.
2. Du är **Fältpersonal**, inloggad som Amina Hassan. **Min dag** visar dagens jobb i tidsordning och "Klart 0/3".
3. Öppna första jobbet. Tryck **Öppna karta**. Åtkomstnoteringen är dold – tryck **Visa** för portkod och nyckel.
4. Ange faktisk tid med **+** / **−** (steg om 0,25 h), prova 0 eller 17 för att se felmeddelandet, skriv en anteckning och tryck **Markera som klar**. Gå tillbaka: "Klart 1/3".
5. Byt till **Ägare**. **Vecka** visar personal × måndag–söndag. Orange = ej tilldelat, rött = försenat och inte klart, grönt = klart. Bläddra veckor med Föregående/Nästa.
6. Tryck på ett orange jobb i raden **Ej tilldelade**, välj medarbetare och dag i **Flytta till…** och tryck **Flytta**.
7. Byt till Fältpersonal och välj den medarbetaren: jobbet finns i hens **Min dag**.
8. Som Ägare: **Fakturaunderlag**. Öppna Sara Wikström (exemplet från Skatteverket): Arbete 2 600 kr, Material 100 kr, Resa 300 kr, moms 750 kr, RUT-avdrag −1 625 kr, **Att betala 2 125 kr**. Läs notisen om att endast kunden ser sitt RUT-utrymme.
9. Öppna Anders Forsberg (det senaste jobbet, för två dagar sedan): varning eftersom resa är 0 kr.
10. Tryck **Återställ demo** för att börja om.
