# BudgetBounty (BFSI) — Java / Spring Boot / Hibernate / Oracle / Angular

A reward-driven personal finance platform for BFSI customers, built on the exact technology
stack called out in the project's BRD architecture diagram: **Java, Spring (MVC), Hibernate ORM,
Oracle Database, and Angular**. Every BFSI use case earns the user reward points for good
financial behaviour — paying bills on time, staying within budget, hitting investment goals,
paying premiums and taxes promptly — which can be redeemed from a central rewards catalog.

A separate prototype of this same application exists in Flask/Python
(`../BudgetBounty_BFSI` history) — this version is the one that matches the BRD's specified
stack and is the one intended for further development and deployment.

## Architecture: BRD vs. what was actually built

| Layer | BRD specifies | This build uses | Why |
|---|---|---|---|
| Language | Java 1.8 | **Java 17 (LTS)** | Java 8 is end-of-life and unsupported by current Spring Boot/Maven tooling |
| Web framework | Spring MVC (Tomcat, XML config) | **Spring Boot 4.2.0** (embedded Tomcat, Java config) | Same Spring MVC programming model, modern bootstrapping |
| ORM | Hibernate | **Hibernate ORM 7.4.15** (via Spring Data JPA) | Matches — Spring Data JPA is a thin repository layer over Hibernate |
| Database | Oracle 11g | **Oracle Database 26ai Free** | Oracle 11g is archived and unobtainable; 26ai Free is Oracle's current free edition |
| Frontend | Angular | **Angular 21.2.13** (standalone components, signals) | Matches |

No other framework (no alternate database, no non-Angular frontend) was substituted anywhere.

## Modules (all implemented, backend + Angular UI, and verified end-to-end against live Oracle data)

1. **Dashboard** — points balance, totals across all modules, recent notifications
2. **Digital Money Transfers** — UPI/NEFT/RTGS/IMPS transfers with reward points per transaction
3. **Cost Control (Budgets)** — category budgets, expense logging, period close-out rewards
4. **Bill Payments** — one-time & recurring bills, autopay flag, on-time payment rewards
5. **Capital Companion (Investments)** — investment goals + portfolio tracking, gain/loss, goal-completion rewards
6. **Property Payment Pro** — scheduled property payments **gated by admin approval** before they can be paid
7. **Insurance** — policy tracking and premium payments
8. **Tax Wise** — Indian income-tax calculator (old/new regime) + tax record payments
9. **Digital Locker** — document upload/download/delete (multipart file storage)
10. **Rewards** — points ledger, redemption catalog, redemption history
11. **Admin Console** — user list, reward-rule point values, reward catalog management, all transactions, property-payment approvals

## System requirements

- Windows 10/11, macOS, or Linux
- 8 GB RAM minimum (Oracle Database is the heaviest component)
- ~10 GB free disk space (Oracle install + Maven/npm caches)

## Prerequisites to install

| Software | Version used | Notes |
|---|---|---|
| JDK | 17 (LTS) | `java -version` |
| Maven | 3.9.9+ | bundled `mvnw` wrapper also included |
| Node.js | 20.x+ | for Angular CLI / npm |
| Angular CLI | 21.x | `npm install -g @angular/cli` |
| Oracle Database | 26ai Free | https://www.oracle.com/database/free/ |
| Oracle JDBC driver | `ojdbc11` (Maven Central) | pulled automatically by Maven |

## Database setup

1. Install Oracle Database 26ai Free and note the listener port (default `1521`) and the
   pluggable database service name (default `FREEPDB1`).
2. Create an application user/schema, e.g.:
   ```sql
   ALTER SESSION SET CONTAINER = FREEPDB1;
   CREATE USER budgetbounty IDENTIFIED BY "BudgetBounty_2026";
   GRANT CONNECT, RESOURCE, DBA TO budgetbounty;
   ALTER USER budgetbounty QUOTA UNLIMITED ON USERS;
   ```
3. Update `backend/src/main/resources/application.yml` if your username/password/service name differ
   from the defaults shown there.

Hibernate is configured with `ddl-auto: update`, so tables are created/updated automatically on
first run — no manual schema scripts are required.

## Running the backend

```bash
cd backend
./mvnw spring-boot:run
```

The API starts on `http://localhost:8080`, under the `/api` base path. On first run (empty
database), `SeedDataRunner` seeds:
- The reward catalog and reward-rule point values (always, idempotently)
- Two demo accounts (only when the `users` table is empty):
  - **Admin:** `admin@budgetbounty.app` / `Admin@123`
  - **Demo user:** `demo@budgetbounty.app` / `Demo@123` — pre-loaded with sample transactions,
    budgets, bills, an investment goal + investments, a property + payment, an insurance policy,
    and a tax record.

## Running the frontend

```bash
cd frontend
npm install
ng serve --port 4300
```

Open `http://localhost:4300`. The dev server proxies API calls straight to
`http://localhost:8080/api` (see `src/environments/environment.ts`).

> Note: port `4300` (not Angular's default `4200`) is used here to avoid clashing with other
> Angular projects that may already be running on this machine.

## Authentication

JWT-based, stateless. On register/login the backend issues a signed JWT (24h expiry) containing
the user's email and role; the Angular `authInterceptor` attaches it as a `Bearer` token to every
API call, and a route guard (`authGuard` / `adminGuard`) protects the authenticated shell and the
admin-only routes respectively.

## Property Payment approval workflow

Property payments follow a strict state machine enforced on the backend, not just the UI:
`pending` → (admin approves/rejects) → `approved`/`rejected` → (user pays) → `paid`. A payment
cannot be paid until an administrator has approved it — the `/api/property/payments/{id}/pay`
endpoint rejects the request otherwise.

## Testing performed

Every module listed above was exercised end-to-end through the running Angular UI against the
live Oracle-backed Spring Boot API (not just compiled): creating records, running transitions
(pay/approve/reject/redeem/close period), and confirming reward points, dashboard totals, and the
top-bar points badge all stay consistent with each other after every action.
