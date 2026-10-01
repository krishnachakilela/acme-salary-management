import {ComponentFixture, TestBed} from '@angular/core/testing';
import {Router} from '@angular/router';
import {of, throwError} from 'rxjs';
import {LoginComponent} from './login.component';
import { AuthService } from '../../../core/auth.service';

describe('LoginComponent', () => {
    let fixture: ComponentFixture<LoginComponent>;
    let component: LoginComponent;
    let authService: jasmine.SpyObj<AuthService>;
    let router: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        authService = jasmine.createSpyObj<AuthService>('AuthService', ['login']);
        router = jasmine.createSpyObj<Router>('Router', ['navigateByUrl']);

        await TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                {provide: AuthService, useValue: authService},
                {provide: Router, useValue: router}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('test_create_defaultState_rendersForm', () => {
        // Arrange + Act
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(component).toBeTruthy();
        expect(compiled.querySelector('h1')?.textContent).toContain('ACME HR Login');
        expect(compiled.querySelector('#login-email')).toBeTruthy();
        expect(compiled.querySelector('#login-password')).toBeTruthy();
    });

    it('test_submit_validCredentials_navigatesToEmployees', () => {
        // Arrange
        authService.login.and.returnValue(of({accessToken: 'token'}));
        component.email = 'hr.manager@acme.example';
        component.password = 'ChangeMe!Acme2026';

        // Act
        component.submit();

        // Assert
        expect(authService.login).toHaveBeenCalledWith('hr.manager@acme.example', 'ChangeMe!Acme2026');
        expect(router.navigateByUrl).toHaveBeenCalledWith('/employees');
        expect(component.loading).toBeFalse();
        expect(component.error).toBe('');
    });

    it('test_submit_authFailure_setsErrorMessage', () => {
        // Arrange
        authService.login.and.returnValue(throwError(() => new Error('unauthorized')));
        component.password = 'wrong-password';

        // Act
        component.submit();
        fixture.detectChanges();

        // Assert
        expect(component.loading).toBeFalse();
        expect(component.error).toBe('Login failed');
        expect(router.navigateByUrl).not.toHaveBeenCalled();
        expect((fixture.nativeElement as HTMLElement).textContent).toContain('Login failed');
    });
});
 