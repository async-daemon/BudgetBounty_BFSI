from datetime import date
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import (
    LinkedAccount, Transaction, Budget, Bill, Investment, InvestmentGoal,
    Property, InsurancePolicy, TaxRecord, Document, RewardTransaction,
)
from app.utils.rewards import notify_user

dashboard_bp = Blueprint("dashboard", __name__)


@dashboard_bp.route("/dashboard")
@login_required
def index():
    user = current_user

    total_transacted = sum(t.amount for t in user.transactions)
    active_budgets = user.budgets.filter_by(period=date.today().strftime("%Y-%m")).all()
    upcoming_bills = user.bills.filter(Bill.status == "scheduled").order_by(Bill.due_date).limit(5).all()
    investments = user.investments.all()
    total_invested = sum(i.invested_amount for i in investments)
    total_current_value = sum(i.current_value for i in investments)
    goals = user.investment_goals.all()
    linked_accounts = user.linked_accounts.all()
    recent_tx = user.transactions.order_by(Transaction.created_at.desc()).limit(6).all()
    recent_rewards = user.reward_transactions.order_by(RewardTransaction.created_at.desc()).limit(6).all()
    doc_count = user.documents.count()

    overdue_bills = [b for b in user.bills.filter_by(status="scheduled").all() if b.is_overdue]

    module_summary = [
        dict(name="Digital Money Transfers", icon="fa-money-bill-transfer", color="#5b4bff",
             value=f"₹{total_transacted:,.0f}", sub=f"{user.transactions.count()} transactions",
             url=url_for("transfers.index")),
        dict(name="Cost Control", icon="fa-wallet", color="#16a085",
             value=f"{len(active_budgets)} active", sub="budgets this month",
             url=url_for("budgets.index")),
        dict(name="Bill Scheduling", icon="fa-calendar-check", color="#f39c12",
             value=f"{len(upcoming_bills)} upcoming", sub=f"{len(overdue_bills)} overdue",
             url=url_for("bills.index")),
        dict(name="Capital Companion", icon="fa-chart-line", color="#2980b9",
             value=f"₹{total_current_value:,.0f}", sub=f"{len(goals)} goals tracked",
             url=url_for("investments.index")),
        dict(name="Property Payment Pro", icon="fa-house-chimney", color="#8e44ad",
             value=f"{user.properties.count()} properties", sub="managed",
             url=url_for("property.index")),
        dict(name="Insurance Management", icon="fa-shield-heart", color="#c0392b",
             value=f"{user.insurance_policies.count()} policies", sub="protected",
             url=url_for("insurance.index")),
        dict(name="Tax Wise", icon="fa-file-invoice-dollar", color="#27ae60",
             value=f"{user.tax_records.count()} records", sub="tracked",
             url=url_for("tax.index")),
        dict(name="Digital Locker", icon="fa-folder-open", color="#d35400",
             value=f"{doc_count} documents", sub="securely stored",
             url=url_for("locker.index")),
    ]

    return render_template(
        "dashboard/index.html",
        total_transacted=total_transacted,
        active_budgets=active_budgets,
        upcoming_bills=upcoming_bills,
        total_invested=total_invested,
        total_current_value=total_current_value,
        goals=goals,
        linked_accounts=linked_accounts,
        recent_tx=recent_tx,
        recent_rewards=recent_rewards,
        module_summary=module_summary,
        overdue_bills=overdue_bills,
    )


@dashboard_bp.route("/accounts")
@login_required
def accounts():
    return render_template("dashboard/accounts.html", accounts=current_user.linked_accounts.all())


@dashboard_bp.route("/accounts/link", methods=["POST"])
@login_required
def link_account():
    institution = request.form.get("institution_name", "").strip()
    account_type = request.form.get("account_type", "bank")
    identifier = request.form.get("identifier", "").strip()

    if not institution or not identifier:
        flash("Please provide the institution name and account/UPI identifier.", "warning")
        return redirect(url_for("dashboard.accounts"))

    account = LinkedAccount(
        user_id=current_user.id,
        institution_name=institution,
        account_type=account_type,
        account_number_masked=("XXXX" + identifier[-4:]) if account_type != "upi" else None,
        upi_id=identifier if account_type == "upi" else None,
        balance=0.0,
        status="active",
    )
    db.session.add(account)
    notify_user(current_user, "Account linked", f"{institution} ({account_type.upper()}) linked successfully.",
                 "success", "fa-link")
    db.session.commit()
    flash(f"{institution} linked successfully!", "success")
    return redirect(url_for("dashboard.accounts"))


@dashboard_bp.route("/accounts/<int:account_id>/unlink", methods=["POST"])
@login_required
def unlink_account(account_id):
    account = LinkedAccount.query.filter_by(id=account_id, user_id=current_user.id).first_or_404()
    db.session.delete(account)
    db.session.commit()
    flash("Account unlinked.", "info")
    return redirect(url_for("dashboard.accounts"))


@dashboard_bp.route("/profile")
@login_required
def profile():
    return render_template("dashboard/profile.html")
