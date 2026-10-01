import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ConfirmDialogComponent} from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
    let fixture: ComponentFixture<ConfirmDialogComponent>;
    let component: ConfirmDialogComponent;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [ConfirmDialogComponent]
        }).compileComponents();

        fixture = TestBed.createComponent(ConfirmDialogComponent);
        component = fixture.componentInstance;
    });

    it('test_create_whenClosed_hidesDialog', () => {
        // Arrange
        component.open = false;

        // Act
        fixture.detectChanges();

        // Assert
        expect(fixture.nativeElement.querySelector('.confirm-dialog')).toBeNull();
    });

    it('test_create_whenOpen_rendersTitleAndMessage', () => {
        // Arrange
        component.open = true;
        component.title = 'Reactivate employee';
        component.message = 'Are you sure you want to reactivate this employee?';
        component.confirmLabel = 'Reactivate';

        // Act
        fixture.detectChanges();
        const compiled = fixture.nativeElement as HTMLElement;

        // Assert
        expect(compiled.querySelector('.confirm-title')?.textContent).toContain('Reactivate employee');
        expect(compiled.textContent).toContain('Are you sure you want to reactivate this employee?');
        expect(compiled.textContent).toContain('Reactivate');
    });

    it('test_confirmClick_emitsConfirmed', () => {
        // Arrange
        component.open = true;
        fixture.detectChanges();
        const confirmedSpy = spyOn(component.confirmed, 'emit');

        // Act
        const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
        buttons[1].click();

        // Assert
        expect(confirmedSpy).toHaveBeenCalled();
    });

    it('test_cancelClick_emitsCancelled', () => {
        // Arrange
        component.open = true;
        fixture.detectChanges();
        const cancelledSpy = spyOn(component.cancelled, 'emit');

        // Act
        const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('button');
        buttons[0].click();

        // Assert
        expect(cancelledSpy).toHaveBeenCalled();
    });
});
 