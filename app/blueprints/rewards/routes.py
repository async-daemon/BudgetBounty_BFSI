from flask import Blueprint, render_template, redirect, url_for, flash
from flask_login import login_required, current_user

from app.extensions import db
from app.models import RewardCatalogItem, Redemption, RewardTransaction
from app.utils.rewards import redeem_points

rewards_bp = Blueprint("rewards", __name__)


@rewards_bp.route("/")
@login_required
def index():
    catalog = RewardCatalogItem.query.filter_by(active=True).order_by(RewardCatalogItem.points_required).all()
    ledger = current_user.reward_transactions.order_by(RewardTransaction.created_at.desc()).limit(20).all()
    redemptions = (
        Redemption.query.filter_by(user_id=current_user.id).order_by(Redemption.redeemed_at.desc()).limit(10).all()
    )
    total_earned = sum(t.points for t in current_user.reward_transactions if t.entry_type == "earn")
    total_redeemed = sum(-t.points for t in current_user.reward_transactions if t.entry_type == "redeem")
    return render_template("rewards/index.html", catalog=catalog, ledger=ledger, redemptions=redemptions,
                            total_earned=total_earned, total_redeemed=total_redeemed)


@rewards_bp.route("/redeem/<int:item_id>", methods=["POST"])
@login_required
def redeem(item_id):
    item = RewardCatalogItem.query.filter_by(id=item_id, active=True).first_or_404()

    if (current_user.reward_points or 0) < item.points_required:
        flash(f"You need {item.points_required - (current_user.reward_points or 0)} more points to redeem this.", "warning")
        return redirect(url_for("rewards.index"))

    success = redeem_points(current_user, item.points_required, f"Redeemed: {item.name}")
    if success:
        redemption = Redemption(user_id=current_user.id, catalog_item_id=item.id, points_spent=item.points_required)
        db.session.add(redemption)
        db.session.commit()
        flash(f"Redeemed {item.name}! Check your email for the voucher (simulated).", "success")
    else:
        flash("Redemption failed. Please try again.", "danger")
    return redirect(url_for("rewards.index"))
