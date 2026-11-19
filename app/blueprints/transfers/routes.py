import random
import string
from flask import Blueprint, render_template, redirect, url_for, flash, request
from flask_login import login_required, current_user

from app.extensions import db
from app.models import Transaction
from app.utils.rewards import award_points, get_rule_points, notify_user

transfers_bp = Blueprint("transfers", __name__)


def _generate_reference():
    return "BB" + "".join(random.choices(string.digits, k=10))


@transfers_bp.route("/")
@login_required
def index():
    transactions = current_user.transactions.order_by(Transaction.created_at.desc()).all()
    total_sent = sum(t.amount for t in transactions if t.status == "success")
    total_rewards = sum(t.reward_points_earned for t in transactions)
    return render_template(
        "transfers/index.html", transactions=transactions, total_sent=total_sent, total_rewards=total_rewards
    )


@transfers_bp.route("/new", methods=["GET", "POST"])
@login_required
def new():
    if request.method == "POST":
        method = request.form.get("method", "UPI")
        recipient_name = request.form.get("recipient_name", "").strip()
        recipient_handle = request.form.get("recipient_handle", "").strip()
        note = request.form.get("note", "").strip()
        try:
            amount = float(request.form.get("amount", 0))
        except ValueError:
            amount = 0

        if not recipient_name or not recipient_handle or amount <= 0:
            flash("Please fill in all required fields with a valid amount.", "warning")
            return redirect(url_for("transfers.new"))

        points_per_100 = get_rule_points("transfer", 1)
        points_earned = int((amount // 100) * points_per_100)

        tx = Transaction(
            user_id=current_user.id,
            reference_no=_generate_reference(),
            method=method,
            recipient_name=recipient_name,
            recipient_handle=recipient_handle,
            amount=amount,
            note=note,
            status="success",
            reward_points_earned=points_earned,
        )
        db.session.add(tx)
        db.session.flush()

        if points_earned:
            award_points(current_user, points_earned, "transfer",
                          f"{method} payment of ₹{amount:,.0f} to {recipient_name}", notify=False)

        notify_user(current_user, "Payment successful",
                     f"₹{amount:,.0f} sent to {recipient_name} via {method}. Ref: {tx.reference_no}",
                     "success", "fa-circle-check")
        db.session.commit()
        flash(f"Payment of ₹{amount:,.0f} sent successfully! You earned {points_earned} reward points.", "success")
        return redirect(url_for("transfers.receipt", tx_id=tx.id))

    return render_template("transfers/new.html")


@transfers_bp.route("/receipt/<int:tx_id>")
@login_required
def receipt(tx_id):
    tx = Transaction.query.filter_by(id=tx_id, user_id=current_user.id).first_or_404()
    return render_template("transfers/receipt.html", tx=tx)
