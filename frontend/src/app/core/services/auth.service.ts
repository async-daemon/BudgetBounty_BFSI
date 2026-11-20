import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, UserDto } from '../models/user.model';

const TOKEN_KEY = 'bb_token';
const USER_KEY = 'bb_user';

@Injectable({ providedIn: 'root' })
export class AuthService {
  currentUser = signal<UserDto | null>(this.readStoredUser());

  constructor(private http: HttpClient, private router: Router) {}

  private readStoredUser(): UserDto | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? (JSON.parse(raw) as UserDto) : null;
    } catch {
      return null;
    }
  }

  register(payload: {
    name: string; email: string; phone?: string; password: string; confirmPassword: string;
  }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, payload)
      .pipe(tap((res) => this.storeSession(res)));
  }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, { email, password })
      .pipe(tap((res) => this.storeSession(res)));
  }

  fetchCurrentUser(): Observable<UserDto> {
    return this.http.get<UserDto>(`${environment.apiBaseUrl}/auth/me`)
      .pipe(tap((user) => {
        this.currentUser.set(user);
        localStorage.setItem(USER_KEY, JSON.stringify(user));
      }));
  }

  private storeSession(res: AuthResponse) {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.user));
    this.currentUser.set(res.user);
  }

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.currentUser.set(null);
    this.router.navigate(['/login']);
  }
}
