import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute, RouterLinkActive } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-login',
  standalone: true,
   imports: [CommonModule, FormsModule, RouterLinkActive],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  credentials = {
    username: '',
    password: ''
  };
  error = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
    ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      const token = params['token'];
      const error = params['error'];

      if (token) {
        this.authService.setToken(token);
        try {
          const payload = JSON.parse(atob(token.split('.')[1]));
          const scopes = payload.scope || '';

          if (scopes.includes('ROLE_ADMIN')) {
            this.router.navigate(['/products']);
          } else {
            this.error = 'Chỉ có quản trị viên mới được phép đăng nhập.';
            this.authService.clearToken();
          }
        } catch (e) {
          this.error = 'Đã xảy ra lỗi khi xác thực. Token không hợp lệ.';
          this.authService.clearToken();
          console.error('Error decoding token', e);
        }
      } 
    });
  }

  login() {
    this.loading = true;
    this.error = '';
    this.authService.login(this.credentials).subscribe({
      next: () => {
        const token = this.authService.getToken();
        if (token) {
          try {
            const payload = JSON.parse(atob(token.split('.')[1]));
            const scopes = payload.scope || '';

            if (scopes.includes('ROLE_ADMIN')) {
              // Admin user, proceed to dashboard
              this.router.navigate(['/products']);
            } else {
              this.loading = false;
              this.error = 'Chỉ có quản trị viên mới được phép đăng nhập.';
              this.authService.clearToken(); 
            }
          } catch (e) {
            // Error decoding token
            this.loading = false;
            this.error = 'Đã xảy ra lỗi khi xác thực. Token không hợp lệ.';
            this.authService.clearToken();
            console.error('Error decoding token', e);
          }
        } else {
          // This case should ideally not be reached if the backend successfully returns a token.
          this.loading = false;
          this.error = 'Không nhận được token sau khi đăng nhập thành công.';
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Đăng nhập thất bại. Vui lòng kiểm tra lại tên đăng nhập và mật khẩu.';
        console.error(err);
      }
    });
  }
}
