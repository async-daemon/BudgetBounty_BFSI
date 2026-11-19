from datetime import datetime
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import Investment, InvestmentGoal
from app.utils.rewards import award_points, get_rule_points, notify_user

investments_bp = Blueprint("investments", __name__)

ASSET_TYPES = ["Mutual Fund", "Stocks", "Bonds", "Fixed Deposit", "Gold", "PPF/EPF"]


@investments_bp.route("/")
@login_required
def index():
    investments = current_user.investments.all()
    goals = current_user.investment_goals.all()
    total_invested = sum(i.invested_amount for i in investments)
    total_current = sum(i.current_value for i in investments)
    total_gain = total_current - total_invested
    gain_percent = round((total_gain / total_invested) * 100, 2) if total_invested else 0
    by_type = {}
    for inv in investments:
        by_type[inv.asset_type] = by_type.get(inv.asset_type, 0) + inv.current_value

    return render_template(
        "investments/index.html", investments=investments, goals=goals, asset_types=ASSET_TYPES,
        total_invested=total_invested, total_current=total_current, total_gain=total_gain,
        gain_percent=gain_percent, by_type=by_type,
    )


@investments_bp.route("/add", methods=["POST"])
@login_required
def add_investment():
    name = request.form.get("name", "").strip()
    asset_type = request.form.get("asset_type", "Mutual Fund")
    goal_id = request.form.get("goal_id") or None
    try:
        invested_amount = float(request.form.get("invested_amount", 0))
        current_value = float(request.form.get("current_value") or invested_amount)
    except ValueError:
        invested_amount = current_value = 0

    if not name or invested_amount <= 0:
        flash("Please provide a valid investment name and amount.", "warning")
        return redirect(url_for("investments.index"))

    inv = Investment(user_id=current_user.id, goal_id=goal_id, name=name, asset_type=asset_type,
                      invested_amount=invested_amount, current_value=current_value)
    db.session.add(inv)
    notify_user(current_user, "Investment added", f"{name} (₹{invested_amount:,.0f}) added to your portfolio.",
                 "success", "fa-chart-line")
    db.session.commit()

    if goal_id:
        _check_goal_achievement(int(goal_id))

    flash(f"{name} added to your investment portfolio.", "success")
    return redirect(url_for("investments.index"))


@investments_bp.route("/<int:inv_id>/update-value", methods=["POST"])
@login_required
def update_value(inv_id):
    inv = Investment.query.filter_by(id=inv_id, user_id=current_user.id).first_or_404()
    try:
        inv.current_value = float(request.form.get("current_value", inv.current_value))
    except ValueError:
        pass
    db.session.commit()
    if inv.goal_id:
        _check_goal_achievement(inv.goal_id)
    flash(f"{inv.name} value updated.", "success")
    return redirect(url_for("investments.index"))


@investments_bp.route("/goals/create", methods=["POST"])
@login_required
def create_goal():
    name = request.form.get("name", "").strip()
    try:
        target_amount = float(request.form.get("target_amount", 0))
    except ValueError:
        target_amount = 0
    target_date_str = request.form.get("target_date")
    target_date = datetime.strptime(target_date_str, "%Y-%m-%d").date() if target_date_str else None

    if not name or target_amount <= 0:
        flash("Please provide a valid goal name and target amount.", "warning")
        return redirect(url_for("investments.index"))

    goal = InvestmentGoal(user_id=current_user.id, name=name, target_amount=target_amount, target_date=target_date)
    db.session.add(goal)
    db.session.commit()
    flash(f"Investment goal '{name}' created.", "success")
    return redirect(url_for("investments.index"))


def _check_goal_achievement(goal_id):
    goal = InvestmentGoal.query.get(goal_id)
    if not goal or goal.status == "achieved":
        return
    if goal.current_amount >= goal.target_amount:
        goal.status = "achieved"
        points = get_rule_points("investment_goal", 100)
        award_points(current_user, points, "investment_goal", f"Achieved investment goal '{goal.name}'")
        goal.reward_points_earned = points
        db.session.commit()
        flash(f"Goal achieved: {goal.name}! You earned {points} reward points.", "success")
