import {Component, OnInit, inject} from '@angular/core';
import {ActivatedRoute, RouterLink} from '@angular/router';
import {FormsModule} from '@angular/forms';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {ApiService, Employee, Salary} from '../../core/api.service';

@Component({
    selector: 'app-employee-detail',
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, RouterLink, DecimalPipe],
    template: `
        <a routerLink="/employees">&larr; Back</a>
        <div class="card-panel mt-3" *ngIf="employee">
            <h1 class="h3">{{ employee.firstName }} {{ employee.lastName }}</h1>
            <p>{{ employee.employeeNumber }} · {{ employee.department }} · {{ employee.countryCode }}
                / {{ employee.currencyCode }} · {{ employee.status }}</p>
            <p *ngIf="employee.currentSalary">
                <strong>Current:</strong> {{ employee.currentSalary.amountMinor / 100 | number:'1.0-0' }} {{ employee.currencyCode }}
            </p>

            <h2 class="h5 mt-4">Salary history</h2>
            <table class="table table-sm">
                <thead>
                <tr>
                    <th>Effective</th>
                    <th>Amount</th>
                    <th>Reason</th>
                </tr>
                </thead>
                <tbody>
                <tr *ngFor="let s of history">
                    <td>{{ s.effectiveFrom }}</td>
                    <td>{{ s.amountMinor / 100 | number:'1.0-0' }} {{ s.currencyCode }}</td>
                    <td>{{ s.changeReason }}</td>
                </tr>
                </tbody>
            </table>

            <h2 class="h5 mt-4">Add salary change</h2>
            <form class="row g-2" (ngSubmit)="addSalary()">
                <div class="col-md-3"><input class="form-control" type="number" [(ngModel)]="amountMajor" name="amount"
                                             placeholder="Amount (major units)" required/></div>
                <div class="col-md-3"><input class="form-control" type="date" [(ngModel)]="effectiveFrom"
                                             name="effectiveFrom" required/></div>
                <div class="col-md-4"><input class="form-control" [(ngModel)]="changeReason" name="changeReason"
                                             placeholder="Reason" required/></div>
                <div class="col-md-2">
                    <button class="btn btn-acme w-100" type="submit">Save</button>
                </div>
            </form>
            <div class="text-danger mt-2" *ngIf="error">{{ error }}</div>
        </div>
    `
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

    ngOnInit(): void {
        const id = this.route.snapshot.paramMap.get('id')!;
        this.load(id);
    }

    addSalary(): void {
        const id = this.employee!.id;
        this.api.addSalary(id, {
            amountMinor: Math.round(this.amountMajor * 100),
            effectiveFrom: this.effectiveFrom,
            changeReason: this.changeReason
        }).subscribe({
            next: () => this.load(id),
            error: () => this.error = 'Unable to add salary change'
        });
    }

    private load(id: string): void {
        this.api.getEmployee(id).subscribe((res) => {
            this.employee = res.employee;
            this.history = res.salaryHistory;
        });
    }
}
 