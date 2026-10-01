import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgIf } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth.service';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [FormsModule, NgIf],
    templateUrl: './login.component.html',
    styleUrl: './login.component.css'
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
            next: () => {
                this.loading = false;
                this.router.navigateByUrl('/employees');
            },
            error: () => {
                this.loading = false;
                this.error = 'Login failed';
            }
        });
    }
}
 