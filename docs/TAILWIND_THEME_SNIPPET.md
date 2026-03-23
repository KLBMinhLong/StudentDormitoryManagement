# Tailwind Theme Snippet

Use this snippet inside `tailwind.config.js` or `tailwind.config.ts`.

```js
/** @type {import('tailwindcss').Config} */
export default {
  content: ['./src/**/*.{html,js,ts,jsx,tsx}'],
  theme: {
    container: {
      center: true,
      padding: '1rem',
      screens: {
        xl: '1200px',
      },
    },
    extend: {
      colors: {
        primary: '#0EA5A5',
        'primary-hover': '#0B8F8F',
        secondary: '#3B82F6',
        accent: '#10B981',
        'bg-page': '#F8FAFC',
        'bg-card': '#FFFFFF',
        'text-main': '#0F172A',
        'text-muted': '#475569',
        border: '#E2E8F0',
        success: '#16A34A',
        warning: '#D97706',
        danger: '#DC2626',
      },
      borderRadius: {
        base: '12px',
      },
      boxShadow: {
        soft: '0 4px 12px rgba(15, 23, 42, 0.06)',
      },
      fontFamily: {
        heading: ['"Be Vietnam Pro"', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
      },
      spacing: {
        'section-mobile': '24px',
        'section-desktop': '32px',
      },
    },
  },
  plugins: [],
};
```

## Usage Rules

- Page wrapper: `bg-bg-page text-text-main font-body`
- Card: `bg-bg-card border border-border rounded-base shadow-soft`
- Main button: `bg-primary hover:bg-primary-hover text-white rounded-base`
- Heading: `font-heading font-bold`
- Keep focus ring visible (`focus-visible:outline-secondary` or equivalent classes).
- Avoid purple-dominant alternatives unless user explicitly requests.
