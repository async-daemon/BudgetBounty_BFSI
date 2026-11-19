from datetime import date
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import Budget, BudgetExpense
from app.utils.rewards import award_points, get_rule_points, notify_user

budgets_bp = Blueprint("budgets", __name__)

CATEGORIES = ["Groceries", "Utilities", "Entertainment", "Transport", "Dining Out", "Shopping", "Healthcare", "Other"]


@budgets_bp.route("/")
@login_required
def index():
    period = date.today().strftime("%Y-%m")
    budgets = current_user.budgets.filter_by(period=period).all()
    total_limit = sum(b.monthly_limit for b in budgets)
    total_spent = sum(b.spent_amount for b in budgets)
    return render_template("budgets/index.html", budgets=budgets, categories=CATEGORIES,
                            total_limit=total_limit, total_spent=total_spent, period=period)


@budgets_bp.route("/create", methods=["POST"])
@login_required
def create():
    category = request.form.get("category", "Other")
    try:
        monthly_limit = float(request.form.get("monthly_limit", 0))
    except ValueError:
        monthly_limit = 0

    if monthly_limit <= 0:
        flash("Please set a valid monthly budget limit.", "warning")
        return redirect(url_for("budgets.index"))

    period = date.today().strftime("%Y-%m")
    existing = Budget.query.filter_by(user_id=current_user.id, category=category, period=period).first()
    if existing:
        flash(f"A budget for {category} already exists this month. Edit it instead.", "warning")
        return redirect(url_for("budgets.index"))

    budget = Budget(user_id=current_user.id, category=category, monthly_limit=monthly_limit, period=period)
    db.session.add(budget)
    db.session.commit()
    flash(f"Budget for {category} created.", "success")
    return redirect(url_for("budgets.index"))


@budgets_bp.route("/<int:budget_id>/log-expense", methods=["POST"])
@login_required
def log_expense(budget_id):
    budget = Budget.query.filter_by(id=budget_id, user_id=current_user.id).first_or_404()
    try:
        amount = float(request.form.get("amount", 0))
    except ValueError:
        amount = 0
    description = request.form.get("description", "").strip()

    if amount <= 0:
        flash("Enter a valid expense amount.", "warning")
        return redirect(url_for("budgets.index"))

    expense = BudgetExpense(budget_id=budget.id, amount=amount, description=description)
    db.session.add(expense)
    budget.spent_amount = (budget.spent_amount or 0) + amount

    if budget.spent_amount > budget.monthly_limit:
        notify_user(current_user, "Budget exceeded!",
                     f"You've gone over your {budget.category} budget (₹{budget.spent_amount:,.0f} / "
                     f"₹{budget.monthly_limit:,.0f}).", "warning", "fa-triangle-exclamation")
    elif budget.percent_used >= 80:
        notify_user(current_user, "Budget alert",
                     f"You've used {budget.percent_used}% of your {budget.category} budget.",
                     "warning", "fa-bell")

    db.session.commit()
    flash(f"Expense of ₹{amount:,.0f} logged under {budget.category}.", "success")
    return redirect(url_for("budgets.index"))


@budgets_bp.route("/<int:budget_id>/close", methods=["POST"])
@login_required
def close_period(budget_id):
    """Simulate end-of-month settlement: award points if the user stayed within budget."""
    budget = Budget.query.filter_by(id=budget_id, user_id=current_user.id).first_or_404()
    if budget.is_over_budget:
        flash("This budget was exceeded, so no reward points are awarded for this period.", "info")
    else:
        points = get_rule_points("budget_adherence", 50)
        award_points(current_user, points, "budget_adherence",
                     f"Stayed within {budget.category} budget for {budget.period}")
        budget.reward_points_earned = points
        db.session.commit()
        flash(f"Great job staying within budget! You earned {points} reward points.", "success")
    return redirect(url_for("budgets.index"))


@budgets_bp.route("/<int:budget_id>/delete", methods=["POST"])
@login_required
def delete(budget_id):
    budget = Budget.query.filter_by(id=budget_id, user_id=current_user.id).first_or_404()
    db.session.delete(budget)
    db.session.commit()
    flash("Budget removed.", "info")
    return redirect(url_for("budgets.index"))
