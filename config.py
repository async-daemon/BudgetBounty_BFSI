import os

BASE_DIR = os.path.abspath(os.path.dirname(__file__))


class Config:
    SECRET_KEY = os.environ.get("SECRET_KEY", "budgetbounty-dev-secret-key-change-in-prod")
    SQLALCHEMY_DATABASE_URI = os.environ.get(
        "DATABASE_URL", "sqlite:///" + os.path.join(BASE_DIR, "instance", "budgetbounty.db")
    )
    SQLALCHEMY_TRACK_MODIFICATIONS = False
    UPLOAD_FOLDER = os.path.join(BASE_DIR, "uploads")
    MAX_CONTENT_LENGTH = 10 * 1024 * 1024  # 10 MB per upload
    ALLOWED_EXTENSIONS = {"pdf", "png", "jpg", "jpeg", "gif", "doc", "docx", "xls", "xlsx", "txt", "csv"}

    # Reward engine defaults (admin-configurable at runtime via AdminRewardRule)
    DEFAULT_REWARD_RULES = {
        "transfer": 1,          # points per Rs.100 transacted
        "budget_adherence": 50, # points for staying within budget for the period
        "bill_timely": 25,      # points per timely bill payment
        "investment_goal": 100, # points per investment goal achieved
        "property_payment": 20, # points per property payment
        "insurance_payment": 30,# points per timely insurance payment
        "tax_payment": 40,      # points per timely tax payment
    }

    REDEMPTION_POINT_VALUE_INR = 0.25  # 1 point = Rs 0.25 (for display purposes)
