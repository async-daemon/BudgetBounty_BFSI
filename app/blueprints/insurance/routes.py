from datetime import datetime, date, timedelta
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import InsurancePolicy, InsurancePayment
from app.utils.rewards import award_points, get_rule_points, notify_user

insurance_bp = Blueprint("insurance", __name__)

POLICY_TYPES = ["Life", "Health", "Vehicle", "Home", "Travel"]


@insurance_bp.route("/")
@login_required
def index():
    policies = current_user.insurance_policies.order_by(InsurancePolicy.due_date).all()
    return render_template("insurance/index.html", policies=policies, policy_types=POLICY_TYPES)


@insurance_bp.route("/add", methods=["POST"])
@login_required
def add_policy():
    provider = request.form.get("provider", "").strip()
    policy_number = request.form.get("policy_number", "").strip()
    policy_type = request.form.get("policy_type", "Life")
    try:
        premium_amount = float(request.form.get("premium_amount", 0))
    except ValueError:
        premium_amount = 0
    due_date_str = request.form.get("due_date")

    if not provider or not policy_number or premium_amount <= 0 or not due_date_str:
        flash("Please fill in all policy details.", "warning")
        return redirect(url_for("insurance.index"))

    due_date = datetime.strptime(due_date_str, "%Y-%m-%d").date()
    policy = InsurancePolicy(user_id=current_user.id, provider=provider, policy_number=policy_number,
                              policy_type=policy_type, premium_amount=premium_amount, due_date=due_date)
    db.session.add(policy)
    notify_user(current_user, "Insurance policy linked",
                 f"{policy_type} policy from {provider} linked. Premium due {due_date:%d %b %Y}.",
                 "info", "fa-shield-heart")
    db.session.commit()
    flash(f"{policy_type} policy from {provider} added.", "success")
    return redirect(url_for("insurance.index"))


@insurance_bp.route("/<int:policy_id>/pay", methods=["POST"])
@login_required
def pay_premium(policy_id):
    policy = InsurancePolicy.query.filter_by(id=policy_id, user_id=current_user.id).first_or_404()
    was_timely = policy.due_date >= date.today()

    payment = InsurancePayment(policy_id=policy.id, amount=policy.premium_amount, status="paid")

    if was_timely:
        points = get_rule_points("insurance_payment", 30)
        award_points(current_user, points, "insurance_payment", f"Timely premium payment for {policy.provider}")
        payment.reward_points_earned = points
        flash_msg = f"Premium paid on time! You earned {points} reward points."
    else:
        flash_msg = "Premium paid (was overdue, no reward points for late payments)."

    db.session.add(payment)
    # advance due date by 1 year (annual premium simulation)
    policy.due_date = policy.due_date + timedelta(days=365)
    db.session.commit()
    flash(flash_msg, "success")
    return redirect(url_for("insurance.index"))
