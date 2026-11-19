from datetime import datetime, date
from flask_login import UserMixin
from werkzeug.security import generate_password_hash, check_password_hash
from app.extensions import db


def utcnow():
    return datetime.utcnow()


# ---------------------------------------------------------------------------
# Core: User & Auth
# ---------------------------------------------------------------------------
class User(UserMixin, db.Model):
    __tablename__ = "users"

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(120), nullable=False)
    email = db.Column(db.String(160), unique=True, nullable=False, index=True)
    phone = db.Column(db.String(20))
    password_hash = db.Column(db.String(255), nullable=False)
    role = db.Column(db.String(20), default="user")  # user | admin
    reward_points = db.Column(db.Integer, default=0)
    two_factor_enabled = db.Column(db.Boolean, default=False)
    avatar_color = db.Column(db.String(20), default="#5b4bff")
    created_at = db.Column(db.DateTime, default=utcnow)

    linked_accounts = db.relationship("LinkedAccount", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    transactions = db.relationship("Transaction", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    budgets = db.relationship("Budget", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    bills = db.relationship("Bill", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    investments = db.relationship("Investment", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    investment_goals = db.relationship("InvestmentGoal", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    properties = db.relationship("Property", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    insurance_policies = db.relationship("InsurancePolicy", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    tax_records = db.relationship("TaxRecord", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    documents = db.relationship("Document", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    reward_transactions = db.relationship("RewardTransaction", backref="user", lazy="dynamic", cascade="all, delete-orphan")
    notifications = db.relationship("Notification", backref="user", lazy="dynamic", cascade="all, delete-orphan")

    def set_password(self, password):
        self.password_hash = generate_password_hash(password)

    def check_password(self, password):
        return check_password_hash(self.password_hash, password)

    @property
    def initials(self):
        parts = self.name.split()
        if len(parts) >= 2:
            return (parts[0][0] + parts[-1][0]).upper()
        return self.name[:2].upper()

    @property
    def is_admin(self):
        return self.role == "admin"

    def __repr__(self):
        return f"<User {self.email}>"


class LinkedAccount(db.Model):
    __tablename__ = "linked_accounts"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    institution_name = db.Column(db.String(120), nullable=False)
    account_type = db.Column(db.String(30), nullable=False)  # bank | upi | investment | insurance | tax | property
    account_number_masked = db.Column(db.String(40))
    upi_id = db.Column(db.String(80))
    balance = db.Column(db.Float, default=0.0)
    status = db.Column(db.String(20), default="active")
    linked_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 1 - Digital Money Transfers
# ---------------------------------------------------------------------------
class Transaction(db.Model):
    __tablename__ = "transactions"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    reference_no = db.Column(db.String(40), unique=True)
    method = db.Column(db.String(10), nullable=False)  # UPI | NEFT | RTGS | IMPS
    recipient_name = db.Column(db.String(120), nullable=False)
    recipient_handle = db.Column(db.String(120), nullable=False)  # UPI id or account+IFSC
    amount = db.Column(db.Float, nullable=False)
    note = db.Column(db.String(200))
    status = db.Column(db.String(20), default="success")  # success | failed | pending
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 2 - Cost Control (Budgets)
# ---------------------------------------------------------------------------
class Budget(db.Model):
    __tablename__ = "budgets"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    category = db.Column(db.String(60), nullable=False)
    monthly_limit = db.Column(db.Float, nullable=False)
    spent_amount = db.Column(db.Float, default=0.0)
    period = db.Column(db.String(7), default=lambda: date.today().strftime("%Y-%m"))
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)

    expenses = db.relationship("BudgetExpense", backref="budget", lazy="dynamic", cascade="all, delete-orphan")

    @property
    def percent_used(self):
        if not self.monthly_limit:
            return 0
        return min(round((self.spent_amount / self.monthly_limit) * 100), 999)

    @property
    def is_over_budget(self):
        return self.spent_amount > self.monthly_limit


class BudgetExpense(db.Model):
    __tablename__ = "budget_expenses"

    id = db.Column(db.Integer, primary_key=True)
    budget_id = db.Column(db.Integer, db.ForeignKey("budgets.id"), nullable=False)
    amount = db.Column(db.Float, nullable=False)
    description = db.Column(db.String(200))
    created_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 3 - Timed Payment Scheduling (Bills)
# ---------------------------------------------------------------------------
class Bill(db.Model):
    __tablename__ = "bills"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    name = db.Column(db.String(120), nullable=False)
    category = db.Column(db.String(60), default="Utilities")
    amount = db.Column(db.Float, nullable=False)
    due_date = db.Column(db.Date, nullable=False)
    recurring = db.Column(db.Boolean, default=False)
    frequency = db.Column(db.String(20))  # weekly | monthly | quarterly | yearly
    autopay = db.Column(db.Boolean, default=False)
    status = db.Column(db.String(20), default="scheduled")  # scheduled | paid | overdue | cancelled
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)
    paid_at = db.Column(db.DateTime)

    @property
    def is_overdue(self):
        return self.status == "scheduled" and self.due_date < date.today()


# ---------------------------------------------------------------------------
# Use Case 4 - Capital Companion (Investments)
# ---------------------------------------------------------------------------
class InvestmentGoal(db.Model):
    __tablename__ = "investment_goals"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    name = db.Column(db.String(120), nullable=False)
    target_amount = db.Column(db.Float, nullable=False)
    target_date = db.Column(db.Date)
    status = db.Column(db.String(20), default="in_progress")  # in_progress | achieved
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)

    investments = db.relationship("Investment", backref="goal", lazy="dynamic")

    @property
    def current_amount(self):
        return sum(i.current_value for i in self.investments)

    @property
    def percent_complete(self):
        if not self.target_amount:
            return 0
        return min(round((self.current_amount / self.target_amount) * 100), 100)


class Investment(db.Model):
    __tablename__ = "investments"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    goal_id = db.Column(db.Integer, db.ForeignKey("investment_goals.id"), nullable=True)
    name = db.Column(db.String(120), nullable=False)
    asset_type = db.Column(db.String(40), default="Mutual Fund")  # Stocks | Bonds | Mutual Fund | FD | Gold
    invested_amount = db.Column(db.Float, nullable=False)
    current_value = db.Column(db.Float, nullable=False)
    created_at = db.Column(db.DateTime, default=utcnow)

    @property
    def gain_loss(self):
        return self.current_value - self.invested_amount

    @property
    def gain_loss_percent(self):
        if not self.invested_amount:
            return 0
        return round((self.gain_loss / self.invested_amount) * 100, 2)


# ---------------------------------------------------------------------------
# Use Case 5 - Property Payment Pro
# ---------------------------------------------------------------------------
class Property(db.Model):
    __tablename__ = "properties"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    name = db.Column(db.String(120), nullable=False)
    address = db.Column(db.String(200))
    property_manager = db.Column(db.String(120))
    created_at = db.Column(db.DateTime, default=utcnow)

    payments = db.relationship("PropertyPayment", backref="property", lazy="dynamic", cascade="all, delete-orphan")


class PropertyPayment(db.Model):
    __tablename__ = "property_payments"

    id = db.Column(db.Integer, primary_key=True)
    property_id = db.Column(db.Integer, db.ForeignKey("properties.id"), nullable=False)
    payment_type = db.Column(db.String(40), default="Rent")  # Rent | Maintenance | Fee
    amount = db.Column(db.Float, nullable=False)
    due_date = db.Column(db.Date, nullable=False)
    status = db.Column(db.String(20), default="pending")  # pending | approved | paid | rejected
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)
    paid_at = db.Column(db.DateTime)


# ---------------------------------------------------------------------------
# Use Case 6 - Hassle-free Insurance Payment Management
# ---------------------------------------------------------------------------
class InsurancePolicy(db.Model):
    __tablename__ = "insurance_policies"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    provider = db.Column(db.String(120), nullable=False)
    policy_number = db.Column(db.String(60), nullable=False)
    policy_type = db.Column(db.String(40), default="Life")  # Life | Health | Vehicle | Home
    premium_amount = db.Column(db.Float, nullable=False)
    due_date = db.Column(db.Date, nullable=False)
    status = db.Column(db.String(20), default="active")
    created_at = db.Column(db.DateTime, default=utcnow)

    payments = db.relationship("InsurancePayment", backref="policy", lazy="dynamic", cascade="all, delete-orphan")


class InsurancePayment(db.Model):
    __tablename__ = "insurance_payments"

    id = db.Column(db.Integer, primary_key=True)
    policy_id = db.Column(db.Integer, db.ForeignKey("insurance_policies.id"), nullable=False)
    amount = db.Column(db.Float, nullable=False)
    status = db.Column(db.String(20), default="paid")
    reward_points_earned = db.Column(db.Integer, default=0)
    paid_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 7 - Tax Wise: Calculator and Reminder
# ---------------------------------------------------------------------------
class TaxRecord(db.Model):
    __tablename__ = "tax_records"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    assessment_year = db.Column(db.String(10), nullable=False)
    tax_type = db.Column(db.String(40), default="Income Tax")
    amount_due = db.Column(db.Float, nullable=False)
    due_date = db.Column(db.Date, nullable=False)
    status = db.Column(db.String(20), default="pending")  # pending | paid | overdue
    reward_points_earned = db.Column(db.Integer, default=0)
    created_at = db.Column(db.DateTime, default=utcnow)
    paid_at = db.Column(db.DateTime)


class TaxCalculation(db.Model):
    __tablename__ = "tax_calculations"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    annual_income = db.Column(db.Float, nullable=False)
    regime = db.Column(db.String(10), default="new")  # old | new
    deductions = db.Column(db.Float, default=0.0)
    taxable_income = db.Column(db.Float, nullable=False)
    tax_payable = db.Column(db.Float, nullable=False)
    cess = db.Column(db.Float, default=0.0)
    total_payable = db.Column(db.Float, nullable=False)
    created_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 8 - Digital Locker
# ---------------------------------------------------------------------------
class Document(db.Model):
    __tablename__ = "documents"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    folder = db.Column(db.String(60), default="General")
    original_name = db.Column(db.String(255), nullable=False)
    stored_name = db.Column(db.String(255), nullable=False)
    file_type = db.Column(db.String(20))
    size_bytes = db.Column(db.Integer, default=0)
    source = db.Column(db.String(20), default="manual")  # manual | auto
    uploaded_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Use Case 9 - Points Tracking and Redemption (Rewards)
# ---------------------------------------------------------------------------
class RewardTransaction(db.Model):
    __tablename__ = "reward_transactions"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    points = db.Column(db.Integer, nullable=False)  # positive = earn, negative = redeem
    entry_type = db.Column(db.String(10), nullable=False)  # earn | redeem
    source_module = db.Column(db.String(40), nullable=False)
    description = db.Column(db.String(200))
    created_at = db.Column(db.DateTime, default=utcnow)


class RewardCatalogItem(db.Model):
    __tablename__ = "reward_catalog_items"

    id = db.Column(db.Integer, primary_key=True)
    name = db.Column(db.String(120), nullable=False)
    description = db.Column(db.String(255))
    category = db.Column(db.String(30), default="giftcard")  # giftcard | discount | cashback
    partner_name = db.Column(db.String(120))
    points_required = db.Column(db.Integer, nullable=False)
    icon = db.Column(db.String(40), default="fa-gift")
    active = db.Column(db.Boolean, default=True)

    redemptions = db.relationship("Redemption", backref="item", lazy="dynamic")


class Redemption(db.Model):
    __tablename__ = "redemptions"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    catalog_item_id = db.Column(db.Integer, db.ForeignKey("reward_catalog_items.id"), nullable=False)
    points_spent = db.Column(db.Integer, nullable=False)
    status = db.Column(db.String(20), default="completed")
    redeemed_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Notifications (cross-cutting)
# ---------------------------------------------------------------------------
class Notification(db.Model):
    __tablename__ = "notifications"

    id = db.Column(db.Integer, primary_key=True)
    user_id = db.Column(db.Integer, db.ForeignKey("users.id"), nullable=False)
    title = db.Column(db.String(120), nullable=False)
    message = db.Column(db.String(255), nullable=False)
    category = db.Column(db.String(30), default="info")  # info | success | warning | danger
    icon = db.Column(db.String(40), default="fa-bell")
    is_read = db.Column(db.Boolean, default=False)
    created_at = db.Column(db.DateTime, default=utcnow)


# ---------------------------------------------------------------------------
# Admin - Reward Rules Configuration
# ---------------------------------------------------------------------------
class AdminRewardRule(db.Model):
    __tablename__ = "admin_reward_rules"

    id = db.Column(db.Integer, primary_key=True)
    module = db.Column(db.String(40), unique=True, nullable=False)
    label = db.Column(db.String(120))
    points_value = db.Column(db.Integer, nullable=False)
    description = db.Column(db.String(255))
    active = db.Column(db.Boolean, default=True)
