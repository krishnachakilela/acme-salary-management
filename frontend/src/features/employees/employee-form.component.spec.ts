import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {NgForm} from '@angular/forms';
import {Router, provideRouter} from '@angular/router';
import {of, throwError} from 'rxjs';
import {EmployeeFormComponent} from './employee-form.component';
import {ApiService} from '../../core/api.service';

describe('EmployeeFormComponent', () => {
    let fixture: ComponentFixture<EmployeeFormComponent>;
    let component: EmployeeFormComponent;
    let apiService: jasmine.SpyObj<ApiService>;
    let router: Router;

    beforeEach(async () => {
        apiService = jasmine.createSpyObj<ApiService>('ApiService', ['createEmployee']);

        await TestBed.configureTestingModule({
            imports: [EmployeeFormComponent],
            providers: [
                provideRouter([]),
                {provide: ApiService, useValue: apiService}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(EmployeeFormComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
        spyOn(router, 'navigate');
        fixture.detectChanges();
    });

    function validForm(): NgForm {
        return {
            invalid: false
        } as NgForm;
    }

    function invalidForm(): NgForm {
        return {
            invalid: true
        } as NgForm;
    }

    it('test_create_defaultState_rendersForm', () => {
        // Arrange + Act
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(compiled.querySelector('h1')?.textContent).toContain('Create employee');
        expect(compiled.querySelector('#employee-number')).toBeTruthy();
        expect(component.employeeNumber).toBe('EMP999001');
    });

    it('test_save_invalidForm_doesNotCallApi', () => {
        // Arrange
        component.email = 'ddd';

        // Act
        component.save(invalidForm());
        fixture.detectChanges();

        // Assert
        expect(component.submitted).toBeTrue();
        expect(apiService.createEmployee).not.toHaveBeenCalled();
        expect(router.navigate).not.toHaveBeenCalled();
    });

    it('test_save_success_navigatesToEmployeeDetail', () => {
        // Arrange
        apiService.createEmployee.and.returnValue(of({
            employee: {
                id: 'emp-42',
                employeeNumber: 'EMP999001',
                firstName: 'Alex',
                lastName: 'Smith',
                email: 'alex.smith@acme.example',
                department: 'Engineering',
                countryCode: 'US',
                currencyCode: 'USD',
                status: 'ACTIVE'
            },
            salaryHistory: []
        }));
        component.firstName = 'Alex';
        component.lastName = 'Smith';
        component.email = 'alex.smith@acme.example';
        component.countryCode = 'us';
        component.currencyCode = 'usd';
        component.amountMajor = 90000;
        component.effectiveFrom = '2024-01-01';

        // Act
        component.save(validForm());

        // Assert
        expect(apiService.createEmployee).toHaveBeenCalledWith(jasmine.objectContaining({
            employeeNumber: 'EMP999001',
            countryCode: 'US',
            currencyCode: 'USD',
            initialSalaryMinor: 9_000_000
        }));
        expect(router.navigate).toHaveBeenCalledWith(['/employees', 'emp-42']);
        expect(component.error).toBe('');
    });

    it('test_save_failure_setsErrorMessage', () => {
        // Arrange
        apiService.createEmployee.and.returnValue(throwError(() => new Error('conflict')));
        component.firstName = 'Alex';
        component.lastName = 'Smith';
        component.email = 'alex.smith@acme.example';

        // Act
        component.save(validForm());
        fixture.detectChanges();

        // Assert
        expect(component.error).toBe('Unable to create employee');
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Unable to create employee');
        expect(router.navigate).not.toHaveBeenCalled();
    });

    it('test_showError_whenSubmittedAndInvalid_returnsTrue', () => {
        // Arrange
        component.submitted = true;

        // Act + Assert
        expect(component.showError({invalid: true, touched: false, dirty: false})).toBeTrue();
        expect(component.showError({invalid: false, touched: true, dirty: true})).toBeFalse();
    });

    it('test_template_invalidEmail_showsValidationMessage', () => {
        // Arrange
        component.firstName = 'Alex';
        component.lastName = 'Smith';
        component.email = 'ddd';
        fixture.detectChanges();
        const emailInput = fixture.nativeElement.querySelector('#email') as HTMLInputElement;
        emailInput.value = 'ddd';
        emailInput.dispatchEvent(new Event('input'));
        emailInput.dispatchEvent(new Event('blur'));
        fixture.detectChanges();

        // Act
        const formDe = fixture.debugElement.query(By.css('form'));
        const form = formDe.references['employeeForm'] as NgForm;
        component.save(form);
        fixture.detectChanges();

        // Assert
        expect(form.controls['email'].invalid).toBeTrue();
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Enter a valid email address.');
        expect(apiService.createEmployee).not.toHaveBeenCalled();
    });
});
 