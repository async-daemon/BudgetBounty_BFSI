from functools import wraps
from flask import Blueprint, render_template, redirect, url_for, flash, request, abort
from flask_login import login_required, current_user

from app.extensions import db
from app.models import (
    User, Transaction, AdminRewardRule, RewardCatalogItem, Bill, Budget,
    InsurancePolicy, TaxRecord, Property, PropertyPayment,
)

admin_bp = Blueprint("admin", __name__)


def admin_required(f):
    @wraps(f)
    def wrapper(*args, **kwargs):
        if not current_user.is_authenticated or not current_user.is_admin:
            abort(403)
        return f(*args, **kwargs)
    return wrapper


@admin_bp.route("/")
@login_required
@admin_required
def index():
    stats = dict(
        total_users=User.query.count(),
        total_transactions=Transaction.query.count(),
        total_volume=sum(t.amount for t in Transaction.query.all()),
        total_points_issued=sum(u.reward_points or 0 for u in User.query.all()),
        pending_property_payments=PropertyPayment.query.filter_by(status="pending").count(),
        overdue_bills=Bill.query.filter_by(status="overdue").count(),
    )
    recent_users = User.query.order_by(User.created_at.desc()).limit(8).all()
    recent_tx = Transaction.query.order_by(Transaction.created_at.desc()).limit(8).all()
    return render_template("admin/index.html", stats=stats, recent_users=recent_users, recent_tx=recent_tx)


@admin_bp.route("/users")
@login_required
@admin_required
def users():
    all_users = User.query.order_by(User.created_at.desc()).all()
    return render_template("admin/users.html", users=all_users)


@admin_bp.route("/users/<int:user_id>/toggle-role", methods=["POST"])
@login_required
@admin_required
def toggle_role(user_id):
    user = User.query.get_or_404(user_id)
    if user.id == current_user.id:
        flash("You cannot change your own role.", "warning")
        return redirect(url_for("admin.users"))
    user.role = "admin" if user.role != "admin" else "user"
    db.session.commit()
    flash(f"{user.name} is now {'an admin' if user.role == 'admin' else 'a standard user'}.", "success")
    return redirect(url_for("admin.users"))


@admin_bp.route("/transactions")
@login_required
@admin_required
def transactions():
    method = request.args.get("method")
    query = Transaction.query
    if method:
        query = query.filter_by(method=method)
    all_tx = query.order_by(Transaction.created_at.desc()).all()
    by_method = {}
    for t in Transaction.query.all():
        by_method[t.method] = by_method.get(t.method, 0) + t.amount
    return render_template("admin/transactions.html", transactions=all_tx, by_method=by_method, method=method)


@admin_bp.route("/reward-rules")
@login_required
@admin_required
def reward_rules():
    rules = AdminRewardRule.query.all()
    return render_template("admin/reward_rules.html", rules=rules)


@admin_bp.route("/reward-rules/<int:rule_id>/update", methods=["POST"])
@login_required
@admin_required
def update_reward_rule(rule_id):
    rule = AdminRewardRule.query.get_or_404(rule_id)
    try:
        rule.points_value = int(request.form.get("points_value", rule.points_value))
    except ValueError:
        pass
    rule.active = request.form.get("active") == "on"
    db.session.commit()
    flash(f"Reward rule '{rule.label}' updated.", "success")
    return redirect(url_for("admin.reward_rules"))


@admin_bp.route("/catalog")
@login_required
@admin_required
def catalog():
    items = RewardCatalogItem.query.all()
    return render_template("admin/catalog.html", items=items)


@admin_bp.route("/catalog/add", methods=["POST"])
@login_required
@admin_required
def add_catalog_item():
    name = request.form.get("name", "").strip()
    try:
        points_required = int(request.form.get("points_required", 0))
    except ValueError:
        points_required = 0
    category = request.form.get("category", "giftcard")
    partner_name = request.form.get("partner_name", "").strip()
    description = request.form.get("description", "").strip()
    icon = request.form.get("icon", "fa-gift")

    if not name or points_required <= 0:
        flash("Please provide a valid reward name and points required.", "warning")
        return redirect(url_for("admin.catalog"))

    item = RewardCatalogItem(name=name, description=description, category=category, partner_name=partner_name,
                              points_required=points_required, icon=icon, active=True)
    db.session.add(item)
    db.session.commit()
    flash(f"Reward '{name}' added to catalog.", "success")
    return redirect(url_for("admin.catalog"))


@admin_bp.route("/catalog/<int:item_id>/toggle", methods=["POST"])
@login_required
@admin_required
def toggle_catalog_item(item_id):
    item = RewardCatalogItem.query.get_or_404(item_id)
    item.active = not item.active
    db.session.commit()
    flash(f"'{item.name}' is now {'active' if item.active else 'inactive'}.", "info")
    return redirect(url_for("admin.catalog"))


@admin_bp.route("/property-approvals")
@login_required
@admin_required
def property_approvals():
    pending = PropertyPayment.query.filter_by(status="pending").all()
    return render_template("admin/property_approvals.html", pending=pending)


@admin_bp.route("/property-approvals/<int:payment_id>/approve", methods=["POST"])
@login_required
@admin_required
def approve_property_payment(payment_id):
    payment = PropertyPayment.query.get_or_404(payment_id)
    payment.status = "approved"
    db.session.commit()
    flash("Property payment approved.", "success")
    return redirect(url_for("admin.property_approvals"))
