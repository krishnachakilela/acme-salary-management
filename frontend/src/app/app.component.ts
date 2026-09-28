import {Component} from '@angular/core';
import {RouterLink, RouterOutlet} from '@angular/router';
import {NgIf} from '@angular/common';
import { AuthService } from '../core/auth.service';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterOutlet, RouterLink, NgIf],
    template: `
        <nav class="navbar navbar-expand navbar-dark navbar-acme px-3 mb-4" *ngIf="auth.isLoggedIn()">
            <a class="navbar-brand" routerLink="/employees">ACME Salary</a>
            <div class="navbar-nav">
                <a class="nav-link" routerLink="/employees">Employees</a>
                <a class="nav-link" routerLink="/analytics">Analytics</a>
            </div>
            <button class="btn btn-sm btn-outline-light ms-auto" (click)="auth.logout()">Logout</button>
        </nav>
        <main class="container pb-5">
            <router-outlet/>
        </main>
    `
})
export class AppComponent {
    constructor(public auth: AuthService) {
    }
}
 