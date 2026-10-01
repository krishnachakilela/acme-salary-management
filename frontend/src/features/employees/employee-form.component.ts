import {Component, inject} from '@angular/core';
import {FormsModule, NgForm} from '@angular/forms';
import {NgIf} from '@angular/common';
import {Router, RouterLink} from '@angular/router';
import {ApiService} from '../../core/api.service';

@Component({
    selector: 'app-employee-form',
    standalone: true,
    imports: [FormsModule, NgIf, RouterLink],
    templateUrl: './employee-form.component.html',
    styleUrl: './employee-form.component.css'
})
export class EmployeeFormComponent {
    private static readonly MAX_SALARY_MAJOR = 1_000_000_000;
    private static readonly MIN_SALARY_MAJOR = 0.01;

    private readonly api = inject(ApiService);
    private readonly router = inject(Router);

    readonly employeeNumberPattern = '^EMP[0-9]{4,10}$';
    readonly countryCodePattern = '^[A-Za-z]{2}$';
    readonly currencyCodePattern = '^[A-Za-z]{3}$';
    readonly emailPattern = '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$';
    readonly maxSalaryMajor = EmployeeFormComponent.MAX_SALARY_MAJOR;
    readonly minSalaryMajor = EmployeeFormComponent.MIN_SALARY_MAJOR;

    employeeNumber = 'EMP999001';
    firstName = '';
    lastName = '';
    email = '';
    department = 'Engineering';
    countryCode = 'US';
    currencyCode = 'USD';
    amountMajor: number | null = 90000;
    effectiveFrom = new Date().toISOString().slice(0, 10);
    changeReason = 'Initial hire';
    error = '';
    submitted = false;
    saving = false;

    save(form: NgForm): void {
        this.submitted = true;
        this.error = '';
        if (form.invalid) {
            return;
        }

        this.saving = true;
        this.api.createEmployee({
            employeeNumber: this.employeeNumber.trim(),
            firstName: this.firstName.trim(),
            lastName: this.lastName.trim(),
            email: this.email.trim(),
            department: this.department.trim(),
            countryCode: this.countryCode.trim().toUpperCase(),
            currencyCode: this.currencyCode.trim().toUpperCase(),
            initialSalaryMinor: Math.round((this.amountMajor ?? 0) * 100),
            effectiveFrom: this.effectiveFrom,
            changeReason: this.changeReason.trim()
        }).subscribe({
            next: (res) => {
                this.saving = false;
                this.router.navigate(['/employees', res.employee.id]);
            },
            error: () => {
                this.saving = false;
                this.error = 'Unable to create employee';
            }
        });
    }

    showError(control: { invalid: boolean | null; touched: boolean | null; dirty: boolean | null } | null | undefined): boolean {
        if (!control) {
            return false;
        }
        return !!control.invalid && (!!control.touched || !!control.dirty || this.submitted);
    }
}
 
 