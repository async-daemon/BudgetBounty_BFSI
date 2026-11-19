"""Centralized reward-points engine used across all BudgetBounty modules.

Every module that wants to award or deduct points funnels through
`award_points` / `redeem_points` so that the ledger (RewardTransaction)
always stays consistent with User.reward_points, and a Notification is
raised so the user sees the activity on their dashboard bell.
"""
from app.extensions import db
from app.models import RewardTransaction, Notification, AdminRewardRule


def get_rule_points(module, default=0):
    rule = AdminRewardRule.query.filter_by(module=module, active=True).first()
    if rule:
        return rule.points_value
    return default


def award_points(user, points, source_module, description, notify=True):
    """Credit reward points to a user and log the ledger entry."""
    if points <= 0:
        return 0
    user.reward_points = (user.reward_points or 0) + points
    entry = RewardTransaction(
        user_id=user.id,
        points=points,
        entry_type="earn",
        source_module=source_module,
        description=description,
    )
    db.session.add(entry)
    if notify:
        notify_user(
            user,
            title="Reward points earned!",
            message=f"+{points} pts — {description}",
            category="success",
            icon="fa-coins",
        )
    return points


def redeem_points(user, points, description):
    """Debit reward points from a user. Returns False if insufficient balance."""
    if points <= 0:
        return False
    if (user.reward_points or 0) < points:
        return False
    user.reward_points -= points
    entry = RewardTransaction(
        user_id=user.id,
        points=-points,
        entry_type="redeem",
        source_module="rewards",
        description=description,
    )
    db.session.add(entry)
    notify_user(
        user,
        title="Reward redeemed",
        message=f"-{points} pts — {description}",
        category="info",
        icon="fa-gift",
    )
    return True


def notify_user(user, title, message, category="info", icon="fa-bell"):
    notification = Notification(
        user_id=user.id,
        title=title,
        message=message,
        category=category,
        icon=icon,
    )
    db.session.add(notification)
    return notification
