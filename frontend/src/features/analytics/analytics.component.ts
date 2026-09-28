import {Component, OnInit, inject} from '@angular/core';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {ApiService} from '../../core/api.service';

@Component({
    selector: 'app-analytics',
    standalone: true,
    imports: [NgFor, NgIf, DecimalPipe],
    template: `
        <h1 class="h3 mb-3">Compensation analytics</h1>
        <div class="card-panel mb-3" *ngIf="summary">
            <p class="mb-1"><strong>Total employees:</strong> {{ summary.totalEmployees }}</p>
            <h2 class="h5 mt-3">By currency</h2>
            <table class="table table-sm">
                <thead>
                <tr>
                    <th>Currency</th>
                    <th>Headcount</th>
                    <th>Total</th>
                    <th>Average</th>
                </tr>
                </thead>
                <tbody>
                <tr *ngFor="let row of summary.byCurrency">
                    <td>{{ row.currencyCode }}</td>
                    <td>{{ row.headcount }}</td>
                    <td>{{ row.totalAmountMinor / 100 | number:'1.0-0' }}</td>
                    <td>{{ row.averageAmountMinor / 100 | number:'1.0-0' }}</td>
                </tr>
                </tbody>
            </table>
            <div class="row">
                <div class="col-md-6">
                    <h2 class="h5">By country</h2>
                    <div *ngFor="let row of summary.byCountry" class="mb-2">
                        <div class="d-flex justify-content-between">
                            <span>{{ row.key }}</span><span>{{ row.headcount }}</span></div>
                        <div class="bar" [style.width.%]="width(row.headcount)"></div>
                    </div>
                </div>
                <div class="col-md-6">
                    <h2 class="h5">By department</h2>
                    <div *ngFor="let row of summary.byDepartment" class="mb-2">
                        <div class="d-flex justify-content-between">
                            <span>{{ row.key }}</span><span>{{ row.headcount }}</span></div>
                        <div class="bar" [style.width.%]="width(row.headcount)"></div>
                    </div>
                </div>
            </div>
        </div>
        <div class="card-panel" *ngIf="distribution">
            <h2 class="h5">Salary distribution bands</h2>
            <div *ngFor="let band of distribution.bands" class="mb-2">
                <div class="d-flex justify-content-between">
                    <span>{{ band.band }}</span><span>{{ band.headcount }}</span></div>
                <div class="bar" [style.width.%]="width(band.headcount)"></div>
            </div>
        </div>
    `
})
export class AnalyticsComponent implements OnInit {
    private readonly api = inject(ApiService);
    summary: any;
    distribution: any;
    private max = 1;

    ngOnInit(): void {
        this.api.analyticsSummary().subscribe((s) => {
            this.summary = s;
            this.max = Math.max(1, ...s.byCountry.map((r: any) => r.headcount), ...s.byDepartment.map((r: any) => r.headcount));
        });
        this.api.analyticsDistribution().subscribe((d) => {
            this.distribution = d;
            this.max = Math.max(this.max, ...d.bands.map((b: any) => b.headcount));
        });
    }

    width(count: number): number {
        return Math.max(4, Math.round((count / this.max) * 100));
    }
}
 