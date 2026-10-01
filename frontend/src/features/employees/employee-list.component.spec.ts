import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {of} from 'rxjs';
import {EmployeeListComponent} from './employee-list.component';
import {ApiService, Employee, PageResponse} from '../../core/api.service';

describe('EmployeeListComponent', () => {
    let fixture: ComponentFixture<EmployeeListComponent>;
    let component: EmployeeListComponent;
    let apiService: jasmine.SpyObj<ApiService>;

    const pageResponse: PageResponse<Employee> = {
        content: [{
            id: 'emp-1',
            employeeNumber: 'EMP00000010',
            firstName: 'Alex',
            lastName: 'Smith',
            email: 'alex.smith@acme.example',
            department: 'Engineering',
            countryCode: 'US',
            currencyCode: 'USD',
            status: 'ACTIVE',
            currentSalary: {
                id: 'sal-1',
                amountMinor: 5_000_000,
                currencyCode: 'USD',
                effectiveFrom: '2024-01-01',
                changeReason: 'Initial hire'
            }
        }],
        page: 0,
        size: 20,
        totalElements: 1,
        totalPages: 2
    };

    beforeEach(async () => {
        apiService = jasmine.createSpyObj<ApiService>('ApiService', ['listEmployees']);
        apiService.listEmployees.and.returnValue(of(pageResponse));

        await TestBed.configureTestingModule({
            imports: [EmployeeListComponent],
            providers: [
                provideRouter([]),
                {provide: ApiService, useValue: apiService}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(EmployeeListComponent);
        component = fixture.componentInstance;
    });

    it('test_ngOnInit_loadsEmployees_rendersRows', () => {
        // Arrange + Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(apiService.listEmployees).toHaveBeenCalled();
        expect(component.employees.length).toBe(1);
        expect(compiled.textContent).toContain('EMP00000010');
        expect(compiled.textContent).toContain('Smith, Alex');
    });

    it('test_reload_debouncesAndResetsPage', fakeAsync(() => {
        // Arrange
        fixture.detectChanges();
        apiService.listEmployees.calls.reset();
        component.page = 3;
        component.name = 'Young';

        // Act
        component.reload();
        tick(249);
        expect(apiService.listEmployees).not.toHaveBeenCalled();
        tick(1);

        // Assert
        expect(component.page).toBe(0);
        expect(apiService.listEmployees).toHaveBeenCalled();
    }));

    it('test_next_whenMorePages_incrementsPage', () => {
        // Arrange
        fixture.detectChanges();
        apiService.listEmployees.calls.reset();

        // Act
        component.next();

        // Assert
        expect(component.page).toBe(1);
        expect(apiService.listEmployees).toHaveBeenCalled();
    });

    it('test_prev_whenOnFirstPage_doesNotFetch', () => {
        // Arrange
        fixture.detectChanges();
        apiService.listEmployees.calls.reset();

        // Act
        component.prev();

        // Assert
        expect(component.page).toBe(0);
        expect(apiService.listEmployees).not.toHaveBeenCalled();
    });
});
 