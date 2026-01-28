import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.scss']
})
export class ForgotPasswordComponent {
  email: string = '';
  message: string = '';
  error: string = '';
  loading: boolean = false;

  constructor(private authService: AuthService, private router: Router) { }

  requestReset(): void {
    this.loading = true;
    this.message = '';
    this.error = '';

    this.authService.forgotPassword(this.email).subscribe({
      next: (response) => {
        this.loading = false;
        this.message = response.message || 'Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn.';
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Đã xảy ra lỗi khi gửi yêu cầu. Vui lòng thử lại.';
        console.error('Forgot password error:', err);
      }
    });
  }
}
