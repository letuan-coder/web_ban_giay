import { Component } from '@angular/core';
import { Router, RouterOutlet, RouterModule } from '@angular/router'; // Added RouterOutlet, RouterModule
import { CommonModule } from '@angular/common'; // Added CommonModule
import { AuthService } from './services/auth.service';
import { MatDialogModule } from '@angular/material/dialog'; // Added MatDialogModule

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  standalone: true, // Changed to true
  imports: [CommonModule, RouterOutlet, RouterModule, MatDialogModule], // Added imports array
  styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'DATN-FE';
  showSearchBar: boolean = false;
  
  constructor(private authService: AuthService, public router: Router) { }

  get isAuthRoute(): boolean {
    const url = this.router.url;
    return url.startsWith('/login') || url.startsWith('/forgot-password') || url.startsWith('/reset-password');
  }

  toggleSearchBar(): void {
    this.showSearchBar = !this.showSearchBar;
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      },
      error: (err) => {
        console.error('Logout failed:', err);
        // Even if logout fails on the server, clear token locally and redirect
        this.authService.clearToken();
        this.router.navigate(['/login']);
      }
    });
  }
}

