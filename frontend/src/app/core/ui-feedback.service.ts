import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';

export type ToastType = 'success' | 'error' | 'info';

export interface ToastMessage {
  id: number;
  type: ToastType;
  text: string;
  durationMs: number;
}

export interface ConfirmDialogOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  danger?: boolean;
}

export interface ConfirmDialogState {
  title: string;
  message: string;
  confirmText: string;
  cancelText: string;
  danger: boolean;
}

@Injectable({ providedIn: 'root' })
export class UiFeedbackService {
  private readonly confirmStateSubject = new BehaviorSubject<ConfirmDialogState | null>(null);
  readonly confirmState$ = this.confirmStateSubject.asObservable();

  private readonly toastSubject = new Subject<ToastMessage>();
  readonly toast$ = this.toastSubject.asObservable();

  private toastId = 0;
  private pendingConfirmResolver: ((result: boolean) => void) | null = null;

  confirm(options: ConfirmDialogOptions): Promise<boolean> {
    if (this.pendingConfirmResolver) {
      this.pendingConfirmResolver(false);
      this.pendingConfirmResolver = null;
    }

    const state: ConfirmDialogState = {
      title: options.title,
      message: options.message,
      confirmText: options.confirmText ?? 'Confirmar',
      cancelText: options.cancelText ?? 'Cancelar',
      danger: options.danger ?? false
    };

    this.confirmStateSubject.next(state);

    return new Promise<boolean>((resolve) => {
      this.pendingConfirmResolver = resolve;
    });
  }

  acceptConfirm(): void {
    if (this.pendingConfirmResolver) {
      this.pendingConfirmResolver(true);
    }
    this.closeConfirm();
  }

  cancelConfirm(): void {
    if (this.pendingConfirmResolver) {
      this.pendingConfirmResolver(false);
    }
    this.closeConfirm();
  }

  showSuccess(text: string, durationMs = 2600): void {
    this.pushToast('success', text, durationMs);
  }

  showError(text: string, durationMs = 3600): void {
    this.pushToast('error', text, durationMs);
  }

  showInfo(text: string, durationMs = 2600): void {
    this.pushToast('info', text, durationMs);
  }

  private pushToast(type: ToastType, text: string, durationMs: number): void {
    this.toastId += 1;
    this.toastSubject.next({
      id: this.toastId,
      type,
      text,
      durationMs
    });
  }

  private closeConfirm(): void {
    this.pendingConfirmResolver = null;
    this.confirmStateSubject.next(null);
  }
}
