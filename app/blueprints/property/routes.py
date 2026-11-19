from datetime import datetime, date
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import Property, PropertyPayment
from app.utils.rewards import award_points, get_rule_points, notify_user

property_bp = Blueprint("property", __name__)

PAYMENT_TYPES = ["Rent", "Maintenance", "Utility Fee", "Society Fee", "Other"]


@property_bp.route("/")
@login_required
def index():
    properties = current_user.properties.all()
    all_payments = []
    for p in properties:
        all_payments.extend(p.payments.order_by(PropertyPayment.due_date).all())
    all_payments.sort(key=lambda x: x.due_date)
    return render_template("property/index.html", properties=properties, payments=all_payments,
                            payment_types=PAYMENT_TYPES)


@property_bp.route("/add", methods=["POST"])
@login_required
def add_property():
    name = request.form.get("name", "").strip()
    address = request.form.get("address", "").strip()
    manager = request.form.get("manager", "").strip()
    if not name:
        flash("Please provide a property name.", "warning")
        return redirect(url_for("property.index"))

    prop = Property(user_id=current_user.id, name=name, address=address, property_manager=manager)
    db.session.add(prop)
    db.session.commit()
    flash(f"Property '{name}' added.", "success")
    return redirect(url_for("property.index"))


@property_bp.route("/<int:property_id>/schedule-payment", methods=["POST"])
@login_required
def schedule_payment(property_id):
    prop = Property.query.filter_by(id=property_id, user_id=current_user.id).first_or_404()
    payment_type = request.form.get("payment_type", "Rent")
    try:
        amount = float(request.form.get("amount", 0))
    except ValueError:
        amount = 0
    due_date_str = request.form.get("due_date")

    if amount <= 0 or not due_date_str:
        flash("Enter a valid amount and due date.", "warning")
        return redirect(url_for("property.index"))

    due_date = datetime.strptime(due_date_str, "%Y-%m-%d").date()
    payment = PropertyPayment(property_id=prop.id, payment_type=payment_type, amount=amount, due_date=due_date)
    db.session.add(payment)
    db.session.commit()
    flash(f"{payment_type} payment of ₹{amount:,.0f} scheduled for {prop.name}.", "success")
    return redirect(url_for("property.index"))


@property_bp.route("/payments/<int:payment_id>/pay", methods=["POST"])
@login_required
def pay(payment_id):
    payment = (
        PropertyPayment.query.join(Property)
        .filter(PropertyPayment.id == payment_id, Property.user_id == current_user.id)
        .first_or_404()
    )
    payment.status = "paid"
    payment.paid_at = datetime.utcnow()

    points = get_rule_points("property_payment", 20)
    award_points(current_user, points, "property_payment",
                 f"{payment.payment_type} payment for {payment.property.name}")
    payment.reward_points_earned = points
    db.session.commit()
    flash(f"Payment marked as paid. You earned {points} reward points.", "success")
    return redirect(url_for("property.index"))
