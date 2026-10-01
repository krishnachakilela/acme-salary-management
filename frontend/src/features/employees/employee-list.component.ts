import {Component, OnInit, inject} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {RouterLink} from '@angular/router';
import {ApiService, Employee} from '../../core/api.service';

@Component({
    selector: 'app-employee-list',
    standalone: true,
    imports: [FormsModule, NgFor, NgIf, RouterLink, DecimalPipe],
    templateUrl: './employee-list.component.html',
    styleUrl: './employee-list.component.css'
})
export class EmployeeListComponent implements OnInit {
    private readonly api = inject(ApiService);
    private debounceHandle: ReturnType<typeof setTimeout> | undefined;

    employees: Employee[] = [];
    name = '';
    department = '';
    countryCode = '';
    status = '';
    page = 0;
    size = 20;
    totalPages = 0;
    totalElements = 0;

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
        }).subscribe((res) => {
            this.employees = res.content;
            this.totalPages = res.totalPages;
            this.totalElements = res.totalElements;
        });
    }
}
 
 