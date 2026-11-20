import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface LockerDocument {
  id: number;
  folder: string | null;
  originalName: string;
  fileType: string | null;
  sizeBytes: number;
  source: string;
  uploadedAt: string;
}

@Injectable({ providedIn: 'root' })
export class LockerService {
  private base = `${environment.apiBaseUrl}/locker`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<LockerDocument[]>(this.base);
  }

  upload(file: File, folder?: string) {
    const form = new FormData();
    form.append('file', file);
    if (folder) {
      form.append('folder', folder);
    }
    return this.http.post<LockerDocument>(this.base, form);
  }

  download(id: number) {
    return this.http.get(`${this.base}/${id}/download`, { responseType: 'blob', observe: 'response' });
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
