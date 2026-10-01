import {Component, EventEmitter, Input, Output} from '@angular/core';
import {NgIf} from '@angular/common';

@Component({
    selector: 'app-confirm-dialog',
    standalone: true,
    imports: [NgIf],
    templateUrl: './confirm-dialog.component.html',
    styleUrl: './confirm-dialog.component.css'
})
export class ConfirmDialogComponent {
    private static nextId = 0;

    @Input() open = false;
    @Input() title = 'Confirm';
    @Input() message = 'Are you sure?';
    @Input() confirmLabel = 'Confirm';
    @Input() cancelLabel = 'Cancel';
    @Input() confirmTone: 'primary' | 'danger' = 'primary';

    @Output() readonly confirmed = new EventEmitter<void>();
    @Output() readonly cancelled = new EventEmitter<void>();

    readonly titleId = `confirm-dialog-title-${ConfirmDialogComponent.nextId++}`;

    onConfirm(): void {
        this.confirmed.emit();
    }

    onCancel(): void {
        this.cancelled.emit();
    }
}
 