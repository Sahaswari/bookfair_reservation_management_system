# Book Fair Employee Portal

This Vite + React (TypeScript) frontend powers the organiser/employee experience for the CIBF 2025 reservation system. It includes secure login, dashboards, stall management, and reservation oversight tools that are separate from the vendor-facing `user-portal` app.

## Getting Started

```bash
npm install
npm run dev
```

The app expects to run on its own origin (e.g. `employee.example.com`). Authentication state is stored in `localStorage` keys prefixed with `employee*` so it remains isolated from the vendor portal.

## Available Scripts

- `npm run dev` – start Vite in development mode
- `npm run build` – create a production build in `dist`
- `npm run preview` – serve the production build locally
- `npm run lint` – run ESLint

## Directory Layout

- `src/pages/employee` – dashboard, login, stall management, and reservation list screens
- `src/hooks/useEmployeeAuth.tsx` – simple localStorage-backed auth guard used by protected pages
- `src/components` – shared UI (shadcn) with an organiser-specific header

## Related Projects

- `../user-portal` contains the public/vendor-facing frontend. Each portal now owns its own build tooling, dependencies, and Dockerfile so they can be deployed independently.
