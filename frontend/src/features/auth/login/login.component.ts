import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth.service';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, NgIf],
  template: `
    <div class="row justify-content-center mt-5">
      <div class="col-md-5 card-panel">
        <h1 class="h3 mb-3">ACME HR Login</h1>
        <p class="text-muted">Sign in to manage employee compensation records.</p>
        <form (ngSubmit)="submit()">
          <div class="mb-3">
            <label class="form-label">Email</label>
            <input class="form-control" type="email" [(ngModel)]="email" name="email" required maxlength="255" />
          </div>
          <div class="mb-3">
            <label class="form-label">Password</label>
            <input class="form-control" type="password" [(ngModel)]="password" name="password" required minlength="8" />
          </div>
          <div class="text-danger mb-2" *ngIf="error">{{ error }}</div>
          <button class="btn btn-acme" type="submit" [disabled]="loading">Sign in</button>
        </form>
      </div>
    </div>
  `
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  email = 'hr.manager@acme.example';
  password = '';
  error = '';
  loading = false;

  submit(): void {
    this.loading = true;
    this.error = '';
    this.auth.login(this.email, this.password).subscribe({
      next: () => { this.loading = false; this.router.navigateByUrl('/employees'); },
      error: () => { this.loading = false; this.error = 'Login failed'; }
    });
  }
}
 