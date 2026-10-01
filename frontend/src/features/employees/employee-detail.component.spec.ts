import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap, provideRouter} from '@angular/router';
import {of, throwError} from 'rxjs';
import {EmployeeDetailComponent} from './employee-detail.component';
import {ApiService, Employee, Salary} from '../../core/api.service';

describe('EmployeeDetailComponent', () => {
    let fixture: ComponentFixture<EmployeeDetailComponent>;
    let component: EmployeeDetailComponent;
    let apiService: jasmine.SpyObj<ApiService>;

    const employee: Employee = {
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
    };

    const history: Salary[] = [employee.currentSalary!];

    beforeEach(async () => {
        apiService = jasmine.createSpyObj<ApiService>('ApiService', ['getEmployee', 'addSalary', 'updateEmployee']);
        apiService.getEmployee.and.returnValue(of({employee, salaryHistory: history}));
        apiService.addSalary.and.returnValue(of({employee, salaryHistory: history}));
        apiService.updateEmployee.and.returnValue(of({
            employee: {...employee, status: 'INACTIVE'},
            salaryHistory: history
        }));

        await TestBed.configureTestingModule({
            imports: [EmployeeDetailComponent],
            providers: [
                provideRouter([]),
                {provide: ApiService, useValue: apiService},
                {
                    provide: ActivatedRoute,
                    useValue: {snapshot: {paramMap: convertToParamMap({id: 'emp-1'})}}
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(EmployeeDetailComponent);
        component = fixture.componentInstance;
    });

    it('test_ngOnInit_loadsEmployee_rendersDetail', () => {
        // Arrange + Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(apiService.getEmployee).toHaveBeenCalledWith('emp-1');
        expect(component.employee?.employeeNumber).toBe('EMP00000010');
        expect(compiled.textContent).toContain('Alex Smith');
        expect(compiled.textContent).toContain('Mark inactive');
    });

    it('test_requestStatusChange_opensConfirmDialog', () => {
        // Arrange
        fixture.detectChanges();

        // Act
        component.requestStatusChange('INACTIVE');
        fixture.detectChanges();

        // Assert
        expect(component.confirmOpen).toBeTrue();
        expect((fixture.nativeElement as HTMLElement).textContent)
            .toContain('Mark Alex Smith as inactive?');
        expect(apiService.updateEmployee).not.toHaveBeenCalled();
    });

    it('test_confirmStatusChange_inactive_updatesEmployee', () => {
        // Arrange
        fixture.detectChanges();
        component.requestStatusChange('INACTIVE');

        // Act
        component.confirmStatusChange();
        fixture.detectChanges();

        // Assert
        expect(apiService.updateEmployee).toHaveBeenCalledWith('emp-1', jasmine.objectContaining({
            status: 'INACTIVE',
            email: 'alex.smith@acme.example'
        }));
        expect(component.employee?.status).toBe('INACTIVE');
        expect(component.confirmOpen).toBeFalse();
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Reactivate');
    });

    it('test_cancelStatusChange_closesDialogWithoutApiCall', () => {
        // Arrange
        fixture.detectChanges();
        component.requestStatusChange('INACTIVE');

        // Act
        component.cancelStatusChange();
        fixture.detectChanges();

        // Assert
        expect(component.confirmOpen).toBeFalse();
        expect(apiService.updateEmployee).not.toHaveBeenCalled();
    });

    it('test_confirmStatusChange_failure_setsError', () => {
        // Arrange
        fixture.detectChanges();
        apiService.updateEmployee.and.returnValue(throwError(() => new Error('failed')));
        component.requestStatusChange('INACTIVE');

        // Act
        component.confirmStatusChange();
        fixture.detectChanges();

        // Assert
        expect(component.statusError).toBe('Unable to mark employee inactive');
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Unable to mark employee inactive');
    });

    it('test_addSalary_success_reloadsEmployee', () => {
        // Arrange
        fixture.detectChanges();
        apiService.getEmployee.calls.reset();
        component.amountMajor = 60000;
        component.effectiveFrom = '2025-01-01';
        component.changeReason = 'Annual raise';

        // Act
        component.addSalary();

        // Assert
        expect(apiService.addSalary).toHaveBeenCalledWith('emp-1', {
            amountMinor: 6_000_000,
            effectiveFrom: '2025-01-01',
            changeReason: 'Annual raise'
        });
        expect(apiService.getEmployee).toHaveBeenCalledWith('emp-1');
        expect(component.error).toBe('');
    });

    it('test_addSalary_failure_setsError', () => {
        // Arrange
        fixture.detectChanges();
        apiService.addSalary.and.returnValue(throwError(() => new Error('conflict')));

        // Act
        component.addSalary();
        fixture.detectChanges();

        // Assert
        expect(component.error).toBe('Unable to add salary change');
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Unable to add salary change');
    });

    it('test_inactiveEmployee_disablesSalaryForm', () => {
        // Arrange
        apiService.getEmployee.and.returnValue(of({
            employee: {...employee, status: 'INACTIVE'},
            salaryHistory: history
        }));
        fixture = TestBed.createComponent(EmployeeDetailComponent);
        component = fixture.componentInstance;

        // Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;
        const fieldset = compiled.querySelector('fieldset') as HTMLFieldSetElement;
        const saveButton = compiled.querySelector('button[type="submit"]') as HTMLButtonElement;

        // Assert
        expect(component.canEditSalary).toBeFalse();
        expect(compiled.textContent).toContain('Salary adjustments are disabled while this employee is inactive');
        expect(fieldset.hasAttribute('disabled')).toBeTrue();
        expect(saveButton.hasAttribute('disabled')).toBeTrue();
    });

    it('test_addSalary_whenInactive_doesNotCallApi', () => {
        // Arrange
        fixture.detectChanges();
        component.employee = {...employee, status: 'INACTIVE'};

        // Act
        component.addSalary();

        // Assert
        expect(apiService.addSalary).not.toHaveBeenCalled();
        expect(component.error).toBe('Salary changes are allowed only for active employees');
    });
});
 