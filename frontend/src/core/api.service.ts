import {Injectable, inject} from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';

export interface PageResponse<T> {
    content: T[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface Salary {
    id: string;
    amountMinor: number;
    currencyCode: string;
    effectiveFrom: string;
    changeReason: string;
}

export interface Employee {
    id: string;
    employeeNumber: string;
    firstName: string;
    lastName: string;
    email: string;
    department: string;
    countryCode: string;
    currencyCode: string;
    status: string;
    currentSalary?: Salary | null;
}

@Injectable({providedIn: 'root'})
export class ApiService {
    private readonly http = inject(HttpClient);

    listEmployees(filters: Record<string, string | number>) {
        let params = new HttpParams();
        Object.entries(filters).forEach(([k, v]) => {
            if (v !== '' && v !== null && v !== undefined) {
                params = params.set(k, String(v));
            }
        });
        return this.http.get<PageResponse<Employee>>('/api/v1/employees', {params});
    }

    getEmployee(id: string) {
        return this.http.get<{ employee: Employee; salaryHistory: Salary[] }>(`/api/v1/employees/${id}`);
    }

    createEmployee(body: Record<string, unknown>) {
        return this.http.post<{ employee: Employee; salaryHistory: Salary[] }>('/api/v1/employees', body);
    }

    updateEmployee(id: string, body: Record<string, unknown>) {
        return this.http.put<{ employee: Employee; salaryHistory: Salary[] }>(`/api/v1/employees/${id}`, body);
    }

    addSalary(id: string, body: Record<string, unknown>) {
        return this.http.post<{
            employee: Employee;
            salaryHistory: Salary[]
        }>(`/api/v1/employees/${id}/salaries`, body);
    }

    analyticsSummary() {
        return this.http.get<{
            totalEmployees: number;
            byCurrency: Array<{ currencyCode: string; headcount: number; totalMinor: number; averageMinor: number }>;
            byCountry: Array<{ key: string; headcount: number; averageMinor: number }>;
            byDepartment: Array<{ key: string; headcount: number; averageMinor: number }>;
        }>('/api/v1/analytics/summary');
    }

    analyticsDistribution() {
        return this.http.get<{
            currencyCode: string | null;
            bands: Array<{ band: string; headcount: number }>;
        }>('/api/v1/analytics/distribution');
    }
}
 