import os
import uuid
from flask import Blueprint, render_template, redirect, url_for, flash, request, send_from_directory, current_app, abort
from flask_login import login_required, current_user
from werkzeug.utils import secure_filename

from app.extensions import db
from app.models import Document
from app.utils.rewards import notify_user

locker_bp = Blueprint("locker", __name__)

FOLDERS = ["General", "Bank Statements", "Insurance", "Tax", "Property", "Investments", "Receipts", "Invoices"]


def _allowed_file(filename):
    ext = filename.rsplit(".", 1)[-1].lower() if "." in filename else ""
    return ext in current_app.config["ALLOWED_EXTENSIONS"]


@locker_bp.route("/")
@login_required
def index():
    folder_filter = request.args.get("folder")
    query = current_user.documents
    if folder_filter:
        query = query.filter_by(folder=folder_filter)
    documents = query.order_by(Document.uploaded_at.desc()).all()
    folder_counts = {}
    for d in current_user.documents.all():
        folder_counts[d.folder] = folder_counts.get(d.folder, 0) + 1
    total_size = sum(d.size_bytes for d in current_user.documents.all())
    return render_template("locker/index.html", documents=documents, folders=FOLDERS,
                            folder_counts=folder_counts, active_folder=folder_filter, total_size=total_size)


@locker_bp.route("/upload", methods=["POST"])
@login_required
def upload():
    file = request.files.get("document")
    folder = request.form.get("folder", "General")

    if not file or file.filename == "":
        flash("Please choose a file to upload.", "warning")
        return redirect(url_for("locker.index"))

    if not _allowed_file(file.filename):
        flash("File type not supported.", "danger")
        return redirect(url_for("locker.index"))

    original_name = secure_filename(file.filename)
    ext = original_name.rsplit(".", 1)[-1].lower()
    stored_name = f"{current_user.id}_{uuid.uuid4().hex}.{ext}"
    filepath = os.path.join(current_app.config["UPLOAD_FOLDER"], stored_name)
    file.save(filepath)
    size_bytes = os.path.getsize(filepath)

    doc = Document(user_id=current_user.id, folder=folder, original_name=original_name, stored_name=stored_name,
                    file_type=ext, size_bytes=size_bytes, source="manual")
    db.session.add(doc)
    notify_user(current_user, "Document uploaded", f"{original_name} added to {folder}.", "success", "fa-file-circle-plus")
    db.session.commit()
    flash(f"{original_name} uploaded to your Digital Locker.", "success")
    return redirect(url_for("locker.index"))


@locker_bp.route("/<int:doc_id>/download")
@login_required
def download(doc_id):
    doc = Document.query.filter_by(id=doc_id, user_id=current_user.id).first_or_404()
    return send_from_directory(current_app.config["UPLOAD_FOLDER"], doc.stored_name,
                                as_attachment=True, download_name=doc.original_name)


@locker_bp.route("/<int:doc_id>/delete", methods=["POST"])
@login_required
def delete(doc_id):
    doc = Document.query.filter_by(id=doc_id, user_id=current_user.id).first_or_404()
    filepath = os.path.join(current_app.config["UPLOAD_FOLDER"], doc.stored_name)
    if os.path.exists(filepath):
        os.remove(filepath)
    db.session.delete(doc)
    db.session.commit()
    flash("Document deleted.", "info")
    return redirect(url_for("locker.index"))
