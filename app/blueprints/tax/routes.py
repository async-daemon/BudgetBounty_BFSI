from datetime import datetime, date
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import TaxRecord, TaxCalculation
from app.utils.rewards import award_points, get_rule_points, notify_user

tax_bp = Blueprint("tax", __name__)


def calculate_new_regime_tax(taxable_income):
    """Simplified Indian new-regime slabs (illustrative, FY2023-24 style)."""
    slabs = [
        (300000, 0.0),
        (300000, 0.05),
        (300000, 0.10),
        (300000, 0.15),
        (300000, 0.20),
        (float("inf"), 0.30),
    ]
    remaining = taxable_income
    tax = 0.0
    for width, rate in slabs:
        if remaining <= 0:
            break
        amount_in_slab = min(remaining, width)
        tax += amount_in_slab * rate
        remaining -= amount_in_slab
    return tax


def calculate_old_regime_tax(taxable_income):
    """Simplified Indian old-regime slabs (illustrative)."""
    slabs = [
        (250000, 0.0),
        (250000, 0.05),
        (500000, 0.20),
        (float("inf"), 0.30),
    ]
    remaining = taxable_income
    tax = 0.0
    for width, rate in slabs:
        if remaining <= 0:
            break
        amount_in_slab = min(remaining, width)
        tax += amount_in_slab * rate
        remaining -= amount_in_slab
    return tax


@tax_bp.route("/")
@login_required
def index():
    records = current_user.tax_records.order_by(TaxRecord.due_date).all()
    calculations = TaxCalculation.query.filter_by(user_id=current_user.id).order_by(
        TaxCalculation.created_at.desc()).limit(5).all()
    return render_template("tax/index.html", records=records, calculations=calculations)


@tax_bp.route("/calculator", methods=["GET", "POST"])
@login_required
def calculator():
    result = None
    if request.method == "POST":
        try:
            annual_income = float(request.form.get("annual_income", 0))
            deductions = float(request.form.get("deductions", 0) or 0)
        except ValueError:
            annual_income = deductions = 0
        regime = request.form.get("regime", "new")

        taxable_income = max(annual_income - (deductions if regime == "old" else 0), 0)
        if regime == "new":
            # new regime standard deduction of 50,000
            taxable_income = max(annual_income - 50000, 0)
            tax_payable = calculate_new_regime_tax(taxable_income)
        else:
            taxable_income = max(annual_income - deductions - 50000, 0)
            tax_payable = calculate_old_regime_tax(taxable_income)

        cess = tax_payable * 0.04
        total_payable = tax_payable + cess

        calc = TaxCalculation(user_id=current_user.id, annual_income=annual_income, regime=regime,
                               deductions=deductions, taxable_income=taxable_income, tax_payable=tax_payable,
                               cess=cess, total_payable=total_payable)
        db.session.add(calc)
        db.session.commit()
        result = calc

    return render_template("tax/calculator.html", result=result)


@tax_bp.route("/records/add", methods=["POST"])
@login_required
def add_record():
    assessment_year = request.form.get("assessment_year", "").strip()
    tax_type = request.form.get("tax_type", "Income Tax")
    try:
        amount_due = float(request.form.get("amount_due", 0))
    except ValueError:
        amount_due = 0
    due_date_str = request.form.get("due_date")

    if not assessment_year or amount_due <= 0 or not due_date_str:
        flash("Please fill in all tax record details.", "warning")
        return redirect(url_for("tax.index"))

    due_date = datetime.strptime(due_date_str, "%Y-%m-%d").date()
    record = TaxRecord(user_id=current_user.id, assessment_year=assessment_year, tax_type=tax_type,
                        amount_due=amount_due, due_date=due_date)
    db.session.add(record)
    notify_user(current_user, "Tax account linked",
                 f"{tax_type} for AY {assessment_year} due {due_date:%d %b %Y}.", "info", "fa-file-invoice-dollar")
    db.session.commit()
    flash(f"{tax_type} record for AY {assessment_year} added.", "success")
    return redirect(url_for("tax.index"))


@tax_bp.route("/records/<int:record_id>/pay", methods=["POST"])
@login_required
def pay_record(record_id):
    record = TaxRecord.query.filter_by(id=record_id, user_id=current_user.id).first_or_404()
    was_timely = record.due_date >= date.today()
    record.status = "paid"
    record.paid_at = datetime.utcnow()

    if was_timely:
        points = get_rule_points("tax_payment", 40)
        award_points(current_user, points, "tax_payment", f"Timely {record.tax_type} payment for AY {record.assessment_year}")
        record.reward_points_earned = points
        flash_msg = f"Tax paid on time! You earned {points} reward points."
    else:
        flash_msg = "Tax paid (was overdue, no reward points for late payments)."

    db.session.commit()
    flash(flash_msg, "success")
    return redirect(url_for("tax.index"))
