import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LockerDocument, LockerService } from './locker.service';

@Component({
  selector: 'app-locker',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './locker.component.html'
})
export class LockerComponent implements OnInit {
  private lockerService = inject(LockerService);

  documents = signal<LockerDocument[]>([]);
  loading = signal(true);
  uploading = signal(false);
  folder = signal('General');
  selectedFile: File | null = null;
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.lockerService.list().subscribe((data) => {
      this.documents.set(data);
      this.loading.set(false);
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files && input.files.length ? input.files[0] : null;
  }

  upload() {
    if (!this.selectedFile) {
      this.errorMessage.set('Choose a file first.');
      return;
    }
    this.uploading.set(true);
    this.lockerService.upload(this.selectedFile, this.folder()).subscribe({
      next: (doc) => {
        this.uploading.set(false);
        this.selectedFile = null;
        this.successMessage.set(`${doc.originalName} uploaded.`);
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.uploading.set(false);
        this.errorMessage.set(err?.error?.message || 'Upload failed.');
      }
    });
  }

  download(doc: LockerDocument) {
    this.lockerService.download(doc.id).subscribe({
      next: (res) => {
        const blob = res.body as Blob;
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = doc.originalName;
        a.click();
        window.URL.revokeObjectURL(url);
      },
      error: () => this.errorMessage.set('Download failed.')
    });
  }

  delete(id: number) {
    this.lockerService.delete(id).subscribe({
      next: () => this.refresh(),
      error: () => this.errorMessage.set('Could not delete document.')
    });
  }

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }
}
