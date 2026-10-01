import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideRouter} from '@angular/router';
import {AppComponent} from './app.component';
import { AuthService } from '../core/auth.service';

describe('AppComponent', () => {
    let fixture: ComponentFixture<AppComponent>;
    let authService: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        authService = jasmine.createSpyObj<AuthService>('AuthService', ['isLoggedIn', 'logout']);
        authService.isLoggedIn.and.returnValue(true);

        await TestBed.configureTestingModule({
            imports: [AppComponent],
            providers: [
                provideRouter([]),
                {provide: AuthService, useValue: authService}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(AppComponent);
    });

    it('test_create_whenLoggedIn_rendersNavigation', () => {
        // Arrange
        authService.isLoggedIn.and.returnValue(true);

        // Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(fixture.componentInstance).toBeTruthy();
        expect(compiled.querySelector('.navbar-brand')?.textContent).toContain('ACME Salary');
        expect(compiled.querySelectorAll('.nav-link').length).toBe(2);
    });

    it('test_create_whenLoggedOut_hidesNavigation', () => {
        // Arrange
        authService.isLoggedIn.and.returnValue(false);

        // Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(compiled.querySelector('nav')).toBeNull();
    });

    it('test_logout_click_callsAuthLogout', () => {
        // Arrange
        authService.isLoggedIn.and.returnValue(true);
        fixture.detectChanges();

        // Act
        const button = (fixture.nativeElement as HTMLElement).querySelector('button') as HTMLButtonElement;
        button.click();

        // Assert
        expect(authService.logout).toHaveBeenCalled();
    });
});
 