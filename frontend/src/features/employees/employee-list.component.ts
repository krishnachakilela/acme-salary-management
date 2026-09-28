import {Component, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {ApiService, Employee} from '../../core/api.service';

@Component({
    selector: 'app-employee-list',
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, RouterLink, DecimalPipe],
    template: `
        <div class="d-flex justify-content-between align-items-center mb-3">
            <h1 class="h3 mb-0">Employees</h1>
            <a class="btn btn-acme" routerLink="/employees/new">Add employee</a>
        </div>
        <div class="card-panel mb-3">
            <div class="row g-2">
                <div class="col-md-3"><input class="form-control" placeholder="Name" [(ngModel)]="name"
                                             (ngModelChange)="reload()"/></div>
                <div class="col-md-3"><input class="form-control" placeholder="Department" [(ngModel)]="department"
                                             (ngModelChange)="reload()"/></div>
                <div class="col-md-2"><input class="form-control" placeholder="Country" [(ngModel)]="countryCode"
                                             (ngModelChange)="reload()"/></div>
                <div class="col-md-2">
                    <select class="form-select" [(ngModel)]="status" (ngModelChange)="reload()">
                        <option value="">All statuses</option>
                        <option value="ACTIVE">ACTIVE</option>
                        <option value="INACTIVE">INACTIVE</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="card-panel">
            <table class="table table-sm align-middle">
                <thead>
                <tr>
                    <th>Number</th>
                    <th>Name</th>
                    <th>Dept</th>
                    <th>Country</th>
                    <th>Salary</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr *ngFor="let e of employees">
                    <td>{{ e.employeeNumber }}</td>
                    <td>{{ e.lastName }}, {{ e.firstName }}</td>
                    <td>{{ e.department }}</td>
                    <td>{{ e.countryCode }}</td>
                    <td *ngIf="e.currentSalary">{{ e.currentSalary.amountMinor / 100 | number:'1.0-0' }} {{ e.currencyCode }}</td>
                    <td *ngIf="!e.currentSalary">-</td>
                    <td><a [routerLink]="['/employees', e.id]">Open</a></td>
                </tr>
                </tbody>
            </table>
            <div class="d-flex gap-2 align-items-center">
                <button class="btn btn-outline-secondary btn-sm" [disabled]="page===0" (click)="prev()">Prev</button>
                <span>Page {{ page + 1 }} / {{ totalPages || 1 }} ({{ totalElements }} total)</span>
                <button class="btn btn-outline-secondary btn-sm" [disabled]="page + 1 >= totalPages" (click)="next()">
                    Next
                </button>
            </div>
        </div>
    `
})
export class EmployeeListComponent implements OnInit {
    private readonly api = inject(ApiService);
    employees: Employee[] = [];
    name = '';
    department = '';
    countryCode = '';
    status = '';
    page = 0;
    size = 20;
    totalPages = 0;
    totalElements = 0;
    private debounceHandle: any;

    ngOnInit(): void {
        this.fetch();
    }

    reload(): void {
        clearTimeout(this.debounceHandle);
        this.debounceHandle = setTimeout(() => {
            this.page = 0;
            this.fetch();
        }, 250);
    }

    prev(): void {
        if (this.page > 0) {
            this.page--;
            this.fetch();
        }
    }

    next(): void {
        if (this.page + 1 < this.totalPages) {
            this.page++;
            this.fetch();
        }
    }

    private fetch(): void {
        this.api.listEmployees({
            name: this.name,
            department: this.department,
            countryCode: this.countryCode,
            status: this.status,
            page: this.page,
            size: this.size
        })
            .subscribe((res) => {
                this.employees = res.content;
                this.totalPages = res.totalPages;
                this.totalElements = res.totalElements;
            });
    }
}
 