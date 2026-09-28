import {Component, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgIf} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {ApiService} from '../../core/api.service';

@Component({
    selector: 'app-employee-form',
    standalone: true,
    imports: [FormsModule, NgIf, RouterLink],
    template: `
        <a routerLink="/employees">&larr; Back</a>
        <div class="card-panel mt-3">
            <h1 class="h3">Create employee</h1>
            <form class="row g-3" (ngSubmit)="save()">
                <div class="col-md-4"><label class="form-label">Employee number</label><input class="form-control"
                                                                                              [(ngModel)]="employeeNumber"
                                                                                              name="employeeNumber"
                                                                                              required/></div>
                <div class="col-md-4"><label class="form-label">First name</label><input class="form-control"
                                                                                         [(ngModel)]="firstName"
                                                                                         name="firstName" required/>
                </div>
                <div class="col-md-4"><label class="form-label">Last name</label><input class="form-control"
                                                                                        [(ngModel)]="lastName"
                                                                                        name="lastName" required/></div>
                <div class="col-md-6"><label class="form-label">Email</label><input class="form-control" type="email"
                                                                                    [(ngModel)]="email" name="email"
                                                                                    required/></div>
                <div class="col-md-6"><label class="form-label">Department</label><input class="form-control"
                                                                                         [(ngModel)]="department"
                                                                                         name="department" required/>
                </div>
                <div class="col-md-3"><label class="form-label">Country</label><input class="form-control"
                                                                                      [(ngModel)]="countryCode"
                                                                                      name="countryCode" required
                                                                                      maxlength="2"/></div>
                <div class="col-md-3"><label class="form-label">Currency</label><input class="form-control"
                                                                                       [(ngModel)]="currencyCode"
                                                                                       name="currencyCode" required
                                                                                       maxlength="3"/></div>
                <div class="col-md-3"><label class="form-label">Starting salary</label><input class="form-control"
                                                                                              type="number"
                                                                                              [(ngModel)]="amountMajor"
                                                                                              name="amountMajor"
                                                                                              required/></div>
                <div class="col-md-3"><label class="form-label">Effective from</label><input class="form-control"
                                                                                             type="date"
                                                                                             [(ngModel)]="effectiveFrom"
                                                                                             name="effectiveFrom"
                                                                                             required/></div>
                <div class="col-12"><label class="form-label">Reason</label><input class="form-control"
                                                                                   [(ngModel)]="changeReason"
                                                                                   name="changeReason" required/></div>
                <div class="col-12">
                    <button class="btn btn-acme" type="submit">Create</button>
                </div>
                <div class="text-danger" *ngIf="error">{{ error }}</div>
            </form>
        </div>
    `
})
export class EmployeeFormComponent {
    private readonly api = inject(ApiService);
    private readonly router = inject(Router);
    employeeNumber = 'EMP999001';
    firstName = '';
    lastName = '';
    email = '';
    department = 'Engineering';
    countryCode = 'US';
    currencyCode = 'USD';
    amountMajor = 90000;
    effectiveFrom = new Date().toISOString().slice(0, 10);
    changeReason = 'Initial hire';
    error = '';

    save(): void {
        this.api.createEmployee({
            employeeNumber: this.employeeNumber,
            firstName: this.firstName,
            lastName: this.lastName,
            email: this.email,
            department: this.department,
            countryCode: this.countryCode.toUpperCase(),
            currencyCode: this.currencyCode.toUpperCase(),
            initialSalaryMinor: Math.round(this.amountMajor * 100),
            effectiveFrom: this.effectiveFrom,
            changeReason: this.changeReason
        }).subscribe({
            next: (res: any) => this.router.navigate(['/employees', res.employee.id]),
            error: () => this.error = 'Unable to create employee'
        });
    }
}
 