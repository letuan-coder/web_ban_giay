import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  newPassword: string = '';
  confirmNewPassword: string = '';
  message: string = '';
  error: string = '';
  loading: boolean = false;
  showNewPassword = false;
  showConfirmPassword = false;

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      this.token = params['token'] || '';
      if (!this.token) {
        this.error = 'Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.';
      }
    });
  }

  resetPassword(): void {
    this.loading = true;
    this.message = '';
    this.error = '';

    if (this.newPassword !== this.confirmNewPassword) {
      this.error = 'Mật khẩu mới và xác nhận mật khẩu không khớp.';
      this.loading = false;
      return;
    }

    if (!this.token) {
      this.error = 'Mã đặt lại mật khẩu không hợp lệ hoặc đã hết hạn.';
      this.loading = false;
      return;
    }

    this.authService.resetPassword(this.token, this.newPassword,this.confirmNewPassword).subscribe({
      next: (response) => {
        this.loading = false;
        this.message = response.message || 'Mật khẩu của bạn đã được đặt lại thành công.';
        // Optionally redirect to login page after a short delay
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.message || 'Đã xảy ra lỗi khi đặt lại mật khẩu. Vui lòng thử lại.';
        console.error('Reset password error:', err);
      }
    });
  }
}
