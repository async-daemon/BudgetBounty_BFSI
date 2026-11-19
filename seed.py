"""Seed demo data for BudgetBounty so the app looks populated on first run.

Usage:
    python seed.py
"""
import random
from datetime import date, timedelta, datetime

from app import create_app
from app.extensions import db
from app.models import (
    User, LinkedAccount, Transaction, Budget, BudgetExpense, Bill,
    InvestmentGoal, Investment, Property, PropertyPayment, InsurancePolicy,
    InsurancePayment, TaxRecord, TaxCalculation, Document, RewardCatalogItem,
)
from app.utils.rewards import award_points, get_rule_points, notify_user


def seed():
    app = create_app()
    with app.app_context():
        if User.query.filter_by(email="admin@budgetbounty.app").first():
            print("Demo data already present. Skipping seed.")
            return

        # --- Admin user -----------------------------------------------------
        admin = User(name="BudgetBounty Admin", email="admin@budgetbounty.app", phone="+91 90000 00001", role="admin")
        admin.set_password("Admin@123")
        db.session.add(admin)

        # --- Demo user --------------------------------------------------------
        demo = User(name="Ananya Sharma", email="demo@budgetbounty.app", phone="+91 98765 43210", role="user")
        demo.set_password("Demo@123")
        db.session.add(demo)
        db.session.flush()

        # Linked accounts
        db.session.add_all([
            LinkedAccount(user_id=demo.id, institution_name="HDFC Bank", account_type="bank",
                          account_number_masked="XXXX4821", status="active"),
            LinkedAccount(user_id=demo.id, institution_name="PhonePe", account_type="upi",
                          upi_id="ananya@okhdfcbank", status="active"),
            LinkedAccount(user_id=demo.id, institution_name="Zerodha", account_type="investment",
                          account_number_masked="XXXX9031", status="active"),
        ])

        # Transactions
        recipients = [("Rohan Verma", "rohan@okaxis", "UPI"), ("Landlord - Mr. Iyer", "iyer@okhdfcbank", "UPI"),
                      ("Electricity Board", "1234567890 / SBIN0001234", "NEFT"), ("Priya Nair", "priya@okicici", "IMPS"),
                      ("Credit Card Bill", "9988776655 / HDFC0004455", "RTGS")]
        for i in range(10):
            name, handle, method = random.choice(recipients)
            amount = round(random.uniform(500, 15000), 2)
            points = int((amount // 100) * get_rule_points("transfer", 1))
            tx = Transaction(user_id=demo.id, reference_no=f"BB{random.randint(1000000000,9999999999)}",
                              method=method, recipient_name=name, recipient_handle=handle, amount=amount,
                              status="success", reward_points_earned=points,
                              created_at=datetime.utcnow() - timedelta(days=random.randint(0, 25)))
            db.session.add(tx)
            award_points(demo, points, "transfer", f"{method} payment to {name}", notify=False)

        # Budgets
        period = date.today().strftime("%Y-%m")
        for cat, limit, spent in [("Groceries", 8000, 5200), ("Entertainment", 3000, 3400), ("Transport", 4000, 1800)]:
            b = Budget(user_id=demo.id, category=cat, monthly_limit=limit, spent_amount=spent, period=period)
            db.session.add(b)
            db.session.flush()
            db.session.add(BudgetExpense(budget_id=b.id, amount=spent, description="Simulated monthly spend"))

        # Bills
        db.session.add_all([
            Bill(user_id=demo.id, name="Internet Bill", category="Internet", amount=1499,
                 due_date=date.today() + timedelta(days=5), recurring=True, frequency="monthly"),
            Bill(user_id=demo.id, name="House Rent", category="Rent", amount=25000,
                 due_date=date.today() + timedelta(days=10), recurring=True, frequency="monthly", autopay=True),
            Bill(user_id=demo.id, name="Mobile Recharge", category="Mobile", amount=599,
                 due_date=date.today() - timedelta(days=2), recurring=False),
        ])

        # Investments & goals
        goal = InvestmentGoal(user_id=demo.id, name="Dream Home Down Payment", target_amount=1000000,
                               target_date=date.today() + timedelta(days=700))
        db.session.add(goal)
        db.session.flush()
        db.session.add_all([
            Investment(user_id=demo.id, goal_id=goal.id, name="Axis Bluechip Fund", asset_type="Mutual Fund",
                       invested_amount=200000, current_value=238500),
            Investment(user_id=demo.id, goal_id=goal.id, name="HDFC Fixed Deposit", asset_type="Fixed Deposit",
                       invested_amount=150000, current_value=159000),
            Investment(user_id=demo.id, name="Reliance Industries", asset_type="Stocks",
                       invested_amount=80000, current_value=92500),
            Investment(user_id=demo.id, name="Sovereign Gold Bond", asset_type="Gold",
                       invested_amount=50000, current_value=54200),
        ])

        # Property
        prop = Property(user_id=demo.id, name="Green Valley Apartment 4B", address="Koramangala, Bengaluru",
                         property_manager="Prestige Property Management")
        db.session.add(prop)
        db.session.flush()
        db.session.add(PropertyPayment(property_id=prop.id, payment_type="Maintenance", amount=3500,
                                        due_date=date.today() + timedelta(days=7), status="pending"))

        # Insurance
        db.session.add(InsurancePolicy(user_id=demo.id, provider="LIC of India", policy_number="LIC998877665",
                                        policy_type="Life", premium_amount=24000,
                                        due_date=date.today() + timedelta(days=20)))

        # Tax
        db.session.add(TaxRecord(user_id=demo.id, assessment_year="2024-25", tax_type="Advance Tax",
                                  amount_due=18500, due_date=date.today() + timedelta(days=15)))
        db.session.add(TaxCalculation(user_id=demo.id, annual_income=1200000, regime="new", deductions=0,
                                       taxable_income=1150000, tax_payable=95000, cess=3800, total_payable=98800))

        notify_user(demo, "Welcome to BudgetBounty!", "Your demo account is fully loaded. Explore every module.",
                     "success", "fa-hand-sparkles")

        db.session.commit()
        print("Seed complete.")
        print("  Admin login: admin@budgetbounty.app / Admin@123")
        print("  Demo login:  demo@budgetbounty.app / Demo@123")


if __name__ == "__main__":
    seed()
