# SolarMind design plan

## Tokens

| Group | Tokens |
|---|---|
| Color | Panel Night `#0B1F33`, Cell Blue `#1F4E79`, Sky Wash `#DCEEF5`, Paper `#F6F9FA`, Sun `#FFB627`, Signal Red `#C8402A` |
| Type | Display: Bricolage Grotesque fallback; UI: Instrument Sans fallback; body 16px/1.6; metrics use tabular numerals |
| Spacing | 4, 8, 12, 16, 24, 32, 48, 64px |
| Radius | 4px controls, 12px media, 999px status pills |
| Elevation | Hairline borders first; one restrained overlay shadow |
| Motion | 120/180/240/400/900ms; opacity/transform only; reduced motion removes travel and count-up |

## Wireframes

```text
Home desktop: [logo/nav] [hero copy / media] [what you get ledger] [1 enter] [2 recommend] [3 review] [4 ask] [honest note / CTA]
Home mobile: [logo/menu] [hero] [CTA] [ledger] [steps] [note]
Assessment: [title] [location + electricity + roof form] [result header: capacity] [ledger] [panel illustration]
Dashboard: [latest capacity] [latest ledger] [recent assessments hairline table]
Panels: [sticky filters] [dense panel list] [details drawer]
Assistant: [assessment ledger] [conversation] [question input]
```

## Self-review and revision

Generic risks were uniform cards, decorative gradients, all-caps labels, and arrow links. The revised plan uses ledger hierarchy, hairline rules, asymmetric whitespace, one sun-arc motif, restrained radii, sentence case, and a flat cool palette. The cell grid is restricted to hero, empty states, panel illustration, and result header.

Only returned backend values may be animated or displayed as metrics. No frontend recommendation math is introduced. Font packages/assets are not currently bundled, so the CSS uses documented fallbacks until licensed self-hosted files are supplied.
