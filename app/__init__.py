import os
from flask import Flask
from flask_login import current_user
from config import Config
from app.extensions import db, login_manager, csrf


def create_app(config_class=Config):
    app = Flask(__name__)
    app.config.from_object(config_class)

    os.makedirs(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "instance"), exist_ok=True)
    os.makedirs(app.config["UPLOAD_FOLDER"], exist_ok=True)

    db.init_app(app)
    login_manager.init_app(app)
    csrf.init_app(app)

    from app.models import User

    @login_manager.user_loader
    def load_user(user_id):
        return User.query.get(int(user_id))

    # ---- Register blueprints -------------------------------------------------
    from app.blueprints.auth.routes import auth_bp
    from app.blueprints.dashboard.routes import dashboard_bp
    from app.blueprints.transfers.routes import transfers_bp
    from app.blueprints.budgets.routes import budgets_bp
    from app.blueprints.bills.routes import bills_bp
    from app.blueprints.investments.routes import investments_bp
    from app.blueprints.property.routes import property_bp
    from app.blueprints.insurance.routes import insurance_bp
    from app.blueprints.tax.routes import tax_bp
    from app.blueprints.locker.routes import locker_bp
    from app.blueprints.rewards.routes import rewards_bp
    from app.blueprints.admin.routes import admin_bp

    app.register_blueprint(auth_bp)
    app.register_blueprint(dashboard_bp)
    app.register_blueprint(transfers_bp, url_prefix="/transfers")
    app.register_blueprint(budgets_bp, url_prefix="/budgets")
    app.register_blueprint(bills_bp, url_prefix="/bills")
    app.register_blueprint(investments_bp, url_prefix="/investments")
    app.register_blueprint(property_bp, url_prefix="/property")
    app.register_blueprint(insurance_bp, url_prefix="/insurance")
    app.register_blueprint(tax_bp, url_prefix="/tax")
    app.register_blueprint(locker_bp, url_prefix="/locker")
    app.register_blueprint(rewards_bp, url_prefix="/rewards")
    app.register_blueprint(admin_bp, url_prefix="/admin")

    # ---- Global template context ---------------------------------------------
    @app.context_processor
    def inject_globals():
        unread_count = 0
        recent_notifications = []
        if current_user.is_authenticated:
            unread_count = current_user.notifications.filter_by(is_read=False).count()
            recent_notifications = (
                current_user.notifications.order_by(db.desc("created_at")).limit(6).all()
            )
        return dict(unread_notif_count=unread_count, recent_notifications=recent_notifications)

    # ---- Error handlers --------------------------------------------------
    @app.errorhandler(404)
    def not_found(e):
        from flask import render_template
        return render_template("errors/404.html"), 404

    with app.app_context():
        db.create_all()
        _seed_defaults()

    return app


def _seed_defaults():
    """Idempotent seed for reward rules + demo catalog so the app is usable on first run."""
    from app.models import AdminRewardRule, RewardCatalogItem
    from config import Config

    if AdminRewardRule.query.count() == 0:
        labels = {
            "transfer": "Digital Money Transfer (per ₹100)",
            "budget_adherence": "Stay within monthly budget",
            "bill_timely": "Timely bill payment",
            "investment_goal": "Investment goal achieved",
            "property_payment": "Property payment made",
            "insurance_payment": "Timely insurance payment",
            "tax_payment": "Timely tax payment",
        }
        for module, points in Config.DEFAULT_REWARD_RULES.items():
            db.session.add(
                AdminRewardRule(module=module, label=labels.get(module, module), points_value=points,
                                 description=f"Default rule for {module}")
            )

    if RewardCatalogItem.query.count() == 0:
        catalog = [
            ("Amazon Gift Card - ₹500", "Redeem for an Amazon shopping voucher", "giftcard", "Amazon", 2000, "fa-gift"),
            ("Flipkart Gift Card - ₹250", "Redeem for a Flipkart shopping voucher", "giftcard", "Flipkart", 1000, "fa-gift"),
            ("5% Cashback Voucher", "Cashback credited to your BudgetBounty wallet", "cashback", "BudgetBounty", 500, "fa-coins"),
            ("Swiggy 20% Off Coupon", "Discount on your next food order", "discount", "Swiggy", 300, "fa-tags"),
            ("Movie Ticket Discount", "Flat ₹100 off on BookMyShow", "discount", "BookMyShow", 400, "fa-ticket"),
            ("Electricity Bill Cashback ₹100", "Cashback on your next utility bill", "cashback", "Partner Utility Co.", 800, "fa-bolt"),
        ]
        for name, desc, cat, partner, pts, icon in catalog:
            db.session.add(
                RewardCatalogItem(name=name, description=desc, category=cat, partner_name=partner,
                                   points_required=pts, icon=icon, active=True)
            )

    db.session.commit()
