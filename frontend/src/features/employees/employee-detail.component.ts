import {Component, OnInit, inject} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {ApiService, Employee, Salary} from '../../core/api.service';
import { ConfirmDialogComponent } from '../../shared/confirm-dialog/confirm-dialog.component';

@Component({
    selector: 'app-employee-detail',
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, RouterLink, DecimalPipe, ConfirmDialogComponent],
    templateUrl: './employee-detail.component.html',
    styleUrl: './employee-detail.component.css'
})
export class EmployeeDetailComponent implements OnInit {
    private readonly api = inject(ApiService);
    private readonly route = inject(ActivatedRoute);

    employee?: Employee;
    history: Salary[] = [];
    amountMajor = 100000;
    effectiveFrom = new Date().toISOString().slice(0, 10);
    changeReason = 'Annual adjustment';
    error = '';
    statusError = '';
    statusUpdating = false;

    confirmOpen = false;
    confirmTitle = '';
    confirmMessage = '';
    confirmLabel = '';
    confirmTone: 'primary' | 'danger' = 'primary';
    private pendingStatus: 'ACTIVE' | 'INACTIVE' | null = null;

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id')!;
        this.load(id);
    }

    get canEditSalary(): boolean {
        return this.employee?.status === 'ACTIVE';
    }

    requestStatusChange(status: 'ACTIVE' | 'INACTIVE'): void {
        if (!this.employee || this.statusUpdating) {
            return;
        }

        this.pendingStatus = status;
        if (status === 'INACTIVE') {
            this.confirmTitle = 'Mark employee inactive';
            this.confirmMessage =
                `Mark ${this.employee.firstName} ${this.employee.lastName} as inactive? They will remain searchable with status INACTIVE.`;
            this.confirmLabel = 'Mark inactive';
            this.confirmTone = 'danger';
        } else {
            this.confirmTitle = 'Reactivate employee';
            this.confirmMessage =
                `Reactivate ${this.employee.firstName} ${this.employee.lastName}? Their status will change back to ACTIVE.`;
            this.confirmLabel = 'Reactivate';
            this.confirmTone = 'primary';
        }
        this.confirmOpen = true;
    }

    cancelStatusChange(): void {
        this.confirmOpen = false;
        this.pendingStatus = null;
    }

    confirmStatusChange(): void {
        if (!this.employee || !this.pendingStatus || this.statusUpdating) {
            return;
        }

        const status = this.pendingStatus;
        this.confirmOpen = false;
        this.pendingStatus = null;
        this.statusUpdating = true;
        this.statusError = '';

        this.api.updateEmployee(this.employee.id, {
            firstName: this.employee.firstName,
            lastName: this.employee.lastName,
            email: this.employee.email,
            department: this.employee.department,
            countryCode: this.employee.countryCode,
            currencyCode: this.employee.currencyCode,
            status
        }).subscribe({
            next: (res) => {
                this.employee = res.employee;
                this.history = res.salaryHistory;
                this.statusUpdating = false;
            },
            error: () => {
                this.statusUpdating = false;
                this.statusError = status === 'INACTIVE'
                    ? 'Unable to mark employee inactive'
                    : 'Unable to reactivate employee';
            }
        });
    }

    addSalary(): void {
        if (!this.employee || !this.canEditSalary) {
            this.error = 'Salary changes are allowed only for active employees';
            return;
        }

        const id = this.employee.id;
        this.error = '';
        this.api.addSalary(id, {
            amountMinor: Math.round(this.amountMajor * 100),
            effectiveFrom: this.effectiveFrom,
            changeReason: this.changeReason
        }).subscribe({
            next: () => this.load(id),
            error: () => {
                this.error = 'Unable to add salary change';
            }
        });
    }

    private load(id: string): void {
        this.api.getEmployee(id).subscribe((res) => {
            this.employee = res.employee;
            this.history = res.salaryHistory;
        });
    }
}
 
 