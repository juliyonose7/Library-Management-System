import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { AuthResponse, LoginRequest } from './models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly apiBase = 'http://localhost:8080/api/v1';
  private readonly accessKey = 'sgi.accessToken';
  private readonly refreshKey = 'sgi.refreshToken';

  constructor(private readonly http: HttpClient) {}

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.apiBase}/auth/login`, request)
      .pipe(tap((response) => this.saveTokens(response.accessToken, response.refreshToken)));
  }

  refresh(): Observable<AuthResponse> {
    const refreshToken = this.getRefreshToken();
    return this.http
      .post<AuthResponse>(`${this.apiBase}/auth/refresh`, { refreshToken })
      .pipe(tap((response) => this.saveTokens(response.accessToken, response.refreshToken)));
  }

  logout(): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.removeItem(this.accessKey);
    localStorage.removeItem(this.refreshKey);
  }

  isAuthenticated(): boolean {
    return !!this.getAccessToken();
  }

  getAccessToken(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    return localStorage.getItem(this.accessKey);
  }

  getRefreshToken(): string | null {
    if (typeof localStorage === 'undefined') {
      return null;
    }
    return localStorage.getItem(this.refreshKey);
  }

  private saveTokens(accessToken: string, refreshToken: string): void {
    if (typeof localStorage === 'undefined') {
      return;
    }
    localStorage.setItem(this.accessKey, accessToken);
    localStorage.setItem(this.refreshKey, refreshToken);
  }
}
