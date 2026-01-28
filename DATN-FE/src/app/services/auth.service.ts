import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth/login';
  private tokenKey = 'jwt';  // tên key lưu trong localStorage

  constructor(private http: HttpClient) { }

  login(credentials: any): Observable<any> {
    return this.http.post<any>(this.apiUrl, credentials).pipe(
      tap(response => {
        if (response && response.data && response.data.token) {
          this.setToken(response.data.token);
        }
      })
    );
  }

  setToken(token: string) {
    localStorage.setItem(this.tokenKey, token);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  clearToken() {
    localStorage.removeItem(this.tokenKey);
  }

  logout(): Observable<any> {
    const token = this.getToken();
    if (!token) {
      return new Observable(observer => {
        this.clearToken();
        observer.next(null);
        observer.complete();
      });
    }
    return this.http.post<any>(environment.apiBaseUrl + 'api/auth/logout', { token }).pipe(
      tap(() => {
        this.clearToken();
      })
    );
  }

  forgotPassword(email: string): Observable<any> {
    return this.http.post<any>(environment.apiBaseUrl + '/api/auth/forgot-password', { email });
  }

  resetPassword(token: string, newPassword: string,confirmNewPassword:string): Observable<any> {
    return this.http.post<any>(environment.apiBaseUrl + '/api/auth/reset-password', { token, newPassword,confirmNewPassword});
  }
}
