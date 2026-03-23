# UI Quick Start

## 1) Include Fonts

```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@600;700&family=Inter:wght@400;500&display=swap" rel="stylesheet">
```

## 2) Include Project Theme

If you serve static resources from Spring Boot:

```html
<link rel="stylesheet" href="/ui/theme.css">
```

Theme file location: `src/main/resources/static/ui/theme.css`.

## 3) UI Defaults

- Use page background: `var(--color-bg-page)`
- Use card surface: `var(--color-bg-card)`
- Keep card/input/button radius: `12px`
- Use soft shadow only: `0 4px 12px rgba(15, 23, 42, 0.06)`
- Section spacing: `24px` (mobile), `32px` (desktop)

## 4) Status Colors

- Success: `#16A34A`
- Warning: `#D97706`
- Danger: `#DC2626`

## 5) Accessibility Baseline

- Keep visible keyboard focus on interactive controls.
- Ensure text contrast is readable on light backgrounds.
- Do not rely on color alone for status (add text/icon labels).
