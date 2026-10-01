import {Component, OnInit, inject} from '@angular/core';
import {NgFor, NgIf, DecimalPipe} from '@angular/common';
import {ApiService} from '../../core/api.service';

export interface CurrencyAggregate {
    currencyCode: string;
    headcount: number;
    totalMinor: number;
    averageMinor: number;
}

export interface GroupAggregate {
    key: string;
    headcount: number;
    averageMinor: number;
}

export interface AnalyticsSummary {
    totalEmployees: number;
    byCurrency: CurrencyAggregate[];
    byCountry: GroupAggregate[];
    byDepartment: GroupAggregate[];
}

export interface BandCount {
    band: string;
    headcount: number;
}

export interface AnalyticsDistribution {
    currencyCode: string | null;
    bands: BandCount[];
}

@Component({
    selector: 'app-analytics',
    standalone: true,
    imports: [NgFor, NgIf, DecimalPipe],
    templateUrl: './analytics.component.html',
    styleUrl: './analytics.component.css'
})
export class AnalyticsComponent implements OnInit {
    private static readonly MIN_BAR_PERCENT = 4;
    private static readonly MAX_BAR_PERCENT = 100;

    private readonly api = inject(ApiService);

    summary?: AnalyticsSummary;
    distribution?: AnalyticsDistribution;

    ngOnInit(): void {
        this.api.analyticsSummary().subscribe((s: AnalyticsSummary) => {
            this.summary = s;
        });
        this.api.analyticsDistribution().subscribe((d: AnalyticsDistribution) => {
            this.distribution = d;
        });
    }

    groupWidth(count: number): number {
        const max = this.maxHeadcount([
            ...(this.summary?.byCountry ?? []),
            ...(this.summary?.byDepartment ?? [])
        ].map((row) => row.headcount));
        return this.barPercent(count, max);
    }

    bandWidth(count: number): number {
        const max = this.maxHeadcount((this.distribution?.bands ?? []).map((band) => band.headcount));
        return this.barPercent(count, max);
    }

    private maxHeadcount(values: number[]): number {
        return Math.max(1, ...values);
    }

    private barPercent(count: number, max: number): number {
        const safeCount = Math.max(0, count);
        const percent = Math.round((safeCount / max) * AnalyticsComponent.MAX_BAR_PERCENT);
        return Math.min(
            AnalyticsComponent.MAX_BAR_PERCENT,
            Math.max(AnalyticsComponent.MIN_BAR_PERCENT, percent)
        );
    }
}
 