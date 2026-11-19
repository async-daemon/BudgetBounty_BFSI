# BudgetBounty — Reward-driven Budgeting Application (BFSI)

A full-stack implementation of the BudgetBounty Business Requirements Document: a personal finance
platform that rewards users with points for good financial behaviour — digital payments, budgeting,
bill payments, investing, insurance, tax, and secure document storage.

- **Backend:** Python 3 / Flask (Flask-SQLAlchemy, Flask-Login, Flask-WTF, SQLite)
- **Frontend:** Server-rendered Jinja2 templates styled with Bootstrap 5, Font Awesome 6, Google Fonts,
  Chart.js and CSS animations — a custom design system (`app/static/css/style.css`)
- **Architecture:** Flask application factory + blueprints, one blueprint per BRD module

## Modules implemented (from the BRD's 9 use cases)

| # | Use case | Blueprint |
|---|----------|-----------|
| 1 | Digital Money Transfers (UPI / NEFT / RTGS / IMPS) | `app/blueprints/transfers` |
| 2 | Cost Control (category budgets, spend tracking, alerts) | `app/blueprints/budgets` |
| 3 | Timed Payment Scheduling (one-off & recurring bills) | `app/blueprints/bills` |
| 4 | Capital Companion (investments & goals) | `app/blueprints/investments` |
| 5 | Property Payment Pro (rent/maintenance + admin approval) | `app/blueprints/property` |
| 6 | Hassle-free Insurance Payment Management | `app/blueprints/insurance` |
| 7 | Tax Wise: Calculator & Reminder | `app/blueprints/tax` |
| 8 | Digital Locker (secure document storage) | `app/blueprints/locker` |
| 9 | Points Tracking & Redemption | `app/blueprints/rewards` |

Plus: onboarding/auth (`app/blueprints/auth`), an overview dashboard (`app/blueprints/dashboard`),
and an Admin Console (`app/blueprints/admin`) covering reward-rule configuration, the reward catalog,
user management, transaction monitoring, and property-payment approvals — implementing the BRD's
Admin user stories.

A central reward engine (`app/utils/rewards.py`) is the single place every module credits or debits
points, keeping the ledger (`RewardTransaction`) and `User.reward_points` consistent, and raising an
in-app notification for every award.

> **Note on scope:** Real bank/UPI/insurance/tax-authority APIs are not available in this environment,
> so those third-party integrations (Use Case requirements like "integrate with insurance payment API")
> are simulated: payments complete instantly and reward points are awarded exactly as the BRD specifies,
> but no money actually moves and no external API is called. The BRD's architecture diagram specifies
> Java/Spring/Oracle; this build uses Python/Flask/SQLite per your request — the same layered design
> (presentation / business / data access) is preserved.

## Getting started

```bash
cd "BudgetBounty_BFSI"
python -m venv .venv
```

Activate the virtual environment, then install dependencies:

```bash
.venv\Scripts\activate
pip install -r requirements.txt
```

Seed demo data (creates an admin account, a demo user, and sample records across every module):

```bash
python seed.py
```

Run the app:

```bash
python run.py
```

Then open **http://127.0.0.1:5000** in your browser.

### Demo logins

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@budgetbounty.app` | `Admin@123` |
| Demo user | `demo@budgetbounty.app` | `Demo@123` |

Or register a brand-new account from the landing page — every new user starts with a clean,
zero-state dashboard.

## Project layout

```
BudgetBounty_BFSI/
├── app/
│   ├── blueprints/        one package per module (routes.py per blueprint)
│   ├── templates/         Jinja2 templates, one folder per blueprint
│   ├── static/css|js/     design system + small JS (sidebar, notifications, dropzone, count-up)
│   ├── models.py          all SQLAlchemy models (one section per use case)
│   ├── utils/rewards.py   central reward-points engine
│   └── __init__.py        app factory, blueprint registration, first-run seed of reward rules
├── config.py              app configuration (DB URI, upload folder, default reward values)
├── run.py                 dev server entrypoint
├── seed.py                demo data seed script
└── requirements.txt
```

## Notes

- Database: SQLite file at `instance/budgetbounty.db` (auto-created on first run).
- Uploaded Digital Locker files are stored under `uploads/` (gitignored).
- This repository's `.git` is managed manually, per project instructions — no git commands were run
  by the assistant that built this.
