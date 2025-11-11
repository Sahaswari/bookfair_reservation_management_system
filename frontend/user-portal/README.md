# Book Fair User Portal

This Vite + React (TypeScript) app delivers the vendor-facing experience for reserving stalls at CIBF 2025. It provides the marketing landing page, vendor dashboard, stall reservation flow, confirmation experience, and self-serve reservation history.

## Getting Started

```bash
npm install
npm run dev
```

The portal persists lightweight session state in `localStorage` under the `public*` keys. It is now isolated from the organiser portal that lives in `../employee-portal`.

## Available Scripts

- `npm run dev` – start Vite in development mode
- `npm run build` – bundle for production
- `npm run preview` – serve the production build locally
- `npm run lint` – run ESLint

## Directory Layout

- `src/pages` – landing, login, dashboard, reservation, genres, success, and 404 views
- `src/components` – shared shadcn UI primitives plus the public `Header`
- `src/hooks/usePublicAuth.tsx` – simple localStorage guard for the vendor dashboard/reservation flow
- `src/data/mockData.ts` – mock stall + genre data used across screens

## Related Projects

- `../employee-portal` is the organiser/employee frontend. Each portal now has its own dependencies, `package.json`, and build outputs so they can be deployed separately.
