import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of} from 'rxjs';
import {AnalyticsComponent, AnalyticsDistribution, AnalyticsSummary} from './analytics.component';
import {ApiService} from '../../core/api.service';

describe('AnalyticsComponent', () => {
    let fixture: ComponentFixture<AnalyticsComponent>;
    let component: AnalyticsComponent;
    let apiService: jasmine.SpyObj<ApiService>;

    const summary: AnalyticsSummary = {
        totalEmployees: 2,
        byCurrency: [{currencyCode: 'USD', headcount: 2, totalMinor: 10_000_000, averageMinor: 5_000_000}],
        byCountry: [{key: 'US', headcount: 2, averageMinor: 5_000_000}],
        byDepartment: [{key: 'Engineering', headcount: 1, averageMinor: 5_000_000}]
    };

    const distribution: AnalyticsDistribution = {
        currencyCode: null,
        bands: [
            {band: '30k-50k', headcount: 689},
            {band: '50k-80k', headcount: 1922},
            {band: '80k-120k', headcount: 2616},
            {band: '120k+', headcount: 4773}
        ]
    };

    beforeEach(async () => {
        apiService = jasmine.createSpyObj<ApiService>('ApiService', ['analyticsSummary', 'analyticsDistribution']);
        apiService.analyticsSummary.and.returnValue(of(summary));
        apiService.analyticsDistribution.and.returnValue(of(distribution));

        await TestBed.configureTestingModule({
            imports: [AnalyticsComponent],
            providers: [{provide: ApiService, useValue: apiService}]
        }).compileComponents();

        fixture = TestBed.createComponent(AnalyticsComponent);
        component = fixture.componentInstance;
    });

    it('test_ngOnInit_loadsSummaryAndDistribution_rendersCharts', () => {
        // Arrange + Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(apiService.analyticsSummary).toHaveBeenCalled();
        expect(apiService.analyticsDistribution).toHaveBeenCalled();
        expect(compiled.textContent).toContain('Total employees:');
        expect(compiled.textContent).toContain('2');
        expect(compiled.textContent).toContain('USD');
        expect(compiled.textContent).toContain('120k+');
        expect(compiled.querySelectorAll('.bar-track').length).toBeGreaterThan(0);
    });

    it('test_bandWidth_scalesWithinSection_neverExceedsHundred', () => {
        // Arrange
        fixture.detectChanges();

        // Act
        const full = component.bandWidth(4773);
        const mid = component.bandWidth(2616);
        const zero = component.bandWidth(0);

        // Assert
        expect(full).toBe(100);
        expect(mid).toBeLessThanOrEqual(100);
        expect(mid).toBeGreaterThan(4);
        expect(zero).toBe(4);
    });

    it('test_groupWidth_usesSummaryMax_independentOfBands', () => {
        // Arrange
        fixture.detectChanges();

        // Act
        const full = component.groupWidth(2);
        const half = component.groupWidth(1);

        // Assert
        expect(full).toBe(100);
        expect(half).toBe(50);
    });
});
 