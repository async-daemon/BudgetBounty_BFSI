from datetime import datetime, date, timedelta
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import Bill
from app.utils.rewards import award_points, get_rule_points, notify_user

bills_bp = Blueprint("bills", __name__)

CATEGORIES = ["Rent", "Electricity", "Water", "Internet", "Credit Card", "Mobile", "Subscription", "Other"]


def _next_due_date(current_due, frequency):
    if frequency == "weekly":
        return current_due + timedelta(weeks=1)
    if frequency == "quarterly":
        return current_due + timedelta(days=91)
    if frequency == "yearly":
        return current_due + timedelta(days=365)
    return current_due + timedelta(days=30)  # monthly default


@bills_bp.route("/")
@login_required
def index():
    bills = current_user.bills.order_by(Bill.due_date).all()
    for b in bills:
        if b.status == "scheduled" and b.is_overdue:
            b.status = "overdue"
    db.session.commit()
    upcoming = [b for b in bills if b.status in ("scheduled", "overdue")]
    paid = [b for b in bills if b.status == "paid"]
    return render_template("bills/index.html", upcoming=upcoming, paid=paid, categories=CATEGORIES)


@bills_bp.route("/create", methods=["POST"])
@login_required
def create():
    name = request.form.get("name", "").strip()
    category = request.form.get("category", "Other")
    try:
        amount = float(request.form.get("amount", 0))
    except ValueError:
        amount = 0
    due_date_str = request.form.get("due_date")
    recurring = request.form.get("recurring") == "on"
    frequency = request.form.get("frequency", "monthly")
    autopay = request.form.get("autopay") == "on"

    if not name or amount <= 0 or not due_date_str:
        flash("Please fill in bill name, amount and due date.", "warning")
        return redirect(url_for("bills.index"))

    due_date = datetime.strptime(due_date_str, "%Y-%m-%d").date()

    bill = Bill(user_id=current_user.id, name=name, category=category, amount=amount, due_date=due_date,
                recurring=recurring, frequency=frequency if recurring else None, autopay=autopay)
    db.session.add(bill)
    notify_user(current_user, "Bill scheduled", f"{name} (₹{amount:,.0f}) due on {due_date:%d %b %Y}.",
                 "info", "fa-calendar-plus")
    db.session.commit()
    flash(f"{name} scheduled for {due_date:%d %b %Y}.", "success")
    return redirect(url_for("bills.index"))


@bills_bp.route("/<int:bill_id>/pay", methods=["POST"])
@login_required
def pay(bill_id):
    bill = Bill.query.filter_by(id=bill_id, user_id=current_user.id).first_or_404()
    was_timely = bill.due_date >= date.today()

    bill.status = "paid"
    bill.paid_at = datetime.utcnow()

    if was_timely:
        points = get_rule_points("bill_timely", 25)
        award_points(current_user, points, "bill_timely", f"Timely payment of {bill.name}")
        bill.reward_points_earned = points
        flash_msg = f"{bill.name} paid on time! You earned {points} reward points."
    else:
        flash_msg = f"{bill.name} paid (was overdue, no reward points for late payments)."

    if bill.recurring:
        next_due = _next_due_date(bill.due_date, bill.frequency)
        new_bill = Bill(user_id=current_user.id, name=bill.name, category=bill.category, amount=bill.amount,
                         due_date=next_due, recurring=True, frequency=bill.frequency, autopay=bill.autopay)
        db.session.add(new_bill)

    db.session.commit()
    flash(flash_msg, "success")
    return redirect(url_for("bills.index"))


@bills_bp.route("/<int:bill_id>/cancel", methods=["POST"])
@login_required
def cancel(bill_id):
    bill = Bill.query.filter_by(id=bill_id, user_id=current_user.id).first_or_404()
    bill.status = "cancelled"
    db.session.commit()
    flash(f"{bill.name} cancelled.", "info")
    return redirect(url_for("bills.index"))
