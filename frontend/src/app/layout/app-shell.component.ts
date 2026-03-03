import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { Subscription } from 'rxjs';
import { ConfirmDialogState, ToastMessage, UiFeedbackService } from '../core/ui-feedback.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="shell">
      <aside class="sidebar">
        <div class="brand">SGI LIB</div>
        <nav>
          <a routerLink="/books" routerLinkActive="active-link" [routerLinkActiveOptions]="{ exact: true }">Libros</a>
          <a routerLink="/authors" routerLinkActive="active-link">Autores</a>
          <a routerLink="/clients" routerLinkActive="active-link">Clientes</a>
          <a routerLink="/sales" routerLinkActive="active-link">Ventas</a>
        </nav>
        <button class="logout" (click)="logout()">Salir</button>
      </aside>

      <div class="content">
        <header class="topbar">
          <div>
            <h1>Panel de administración</h1>
            <p>Gestiona catálogo, clientes y ventas desde un solo lugar</p>
          </div>
          <button class="theme-toggle" (click)="toggleTheme()">
            {{ isDarkTheme ? '☀️ Modo claro' : '🌙 Modo oscuro' }}
          </button>
        </header>
        <section class="view">
          <router-outlet></router-outlet>
        </section>
      </div>

      <div class="toast-stack" *ngIf="toasts.length">
        <article
          *ngFor="let toast of toasts"
          class="toast"
          [class.toast-success]="toast.type === 'success'"
          [class.toast-error]="toast.type === 'error'"
          [class.toast-info]="toast.type === 'info'"
        >
          {{ toast.text }}
        </article>
      </div>

      <div class="confirm-overlay" *ngIf="confirmDialog">
        <div class="confirm-dialog">
          <h3>{{ confirmDialog.title }}</h3>
          <p>{{ confirmDialog.message }}</p>
          <div class="confirm-actions">
            <button type="button" (click)="cancelConfirm()">{{ confirmDialog.cancelText }}</button>
            <button
              type="button"
              class="confirm-btn"
              [class.confirm-danger]="confirmDialog.danger"
              (click)="acceptConfirm()"
            >
              {{ confirmDialog.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [
    '.shell{min-height:100vh;display:grid;grid-template-columns:240px 1fr}',
    '.sidebar{background:var(--sidebar-bg);backdrop-filter:blur(8px);color:var(--sidebar-text);padding:20px 14px;display:flex;flex-direction:column;gap:16px;border-right:1px solid rgba(148,163,184,.25)}',
    '.brand{font-size:20px;font-weight:800;letter-spacing:.4px;padding:8px 10px}',
    'nav{display:flex;flex-direction:column;gap:8px}',
    'nav a{padding:10px 12px;border-radius:10px;color:var(--sidebar-link);text-decoration:none;font-weight:600;transition:all .2s}',
    'nav a:hover{background:rgba(148,163,184,.15);color:#fff}',
    'nav a.active-link{background:linear-gradient(135deg,#2563eb,#1d4ed8);color:#fff;box-shadow:0 10px 20px rgba(37,99,235,.35)}',
    '.logout{margin-top:auto;padding:10px 12px;border:1px solid rgba(148,163,184,.35);border-radius:10px;background:transparent;color:#f8fafc;cursor:pointer;font-weight:700}',
    '.logout:hover{background:rgba(148,163,184,.15)}',
    '.content{display:flex;flex-direction:column;min-width:0}',
    '.topbar{padding:20px 28px 8px;display:flex;align-items:center;justify-content:space-between;gap:12px;flex-wrap:wrap}',
    '.topbar h1{margin:0;font-size:24px;letter-spacing:-.3px}',
    '.topbar p{margin:4px 0 0;color:var(--topbar-text-soft)}',
    '.theme-toggle{padding:9px 12px;border:1px solid var(--border);border-radius:10px;background:var(--surface);color:var(--text);cursor:pointer;font-weight:700;transition:all .2s;box-shadow:var(--shadow-soft)}',
    '.theme-toggle:hover{transform:translateY(-1px)}',
    '.view{padding:0 8px 16px}',
    '.toast-stack{position:fixed;top:20px;right:20px;display:flex;flex-direction:column;gap:10px;z-index:1200;max-width:360px}',
    '.toast{padding:12px 14px;border-radius:12px;background:var(--surface);border:1px solid var(--border);box-shadow:var(--shadow-soft);font-weight:600}',
    '.toast-success{border-color:#86efac;background:#f0fdf4;color:#166534}',
    '.toast-error{border-color:#fecaca;background:#fef2f2;color:#991b1b}',
    '.toast-info{border-color:#bfdbfe;background:#eff6ff;color:#1e40af}',
    'body.theme-dark .toast-success{background:#052e16;border-color:#166534;color:#86efac}',
    'body.theme-dark .toast-error{background:#3b1217;border-color:#7f1d1d;color:#fca5a5}',
    'body.theme-dark .toast-info{background:#172554;border-color:#1d4ed8;color:#bfdbfe}',
    '.confirm-overlay{position:fixed;inset:0;background:rgba(2,6,23,.55);display:grid;place-items:center;z-index:1300;padding:16px}',
    '.confirm-dialog{width:100%;max-width:440px;background:var(--surface);border:1px solid var(--border);border-radius:14px;padding:18px;box-shadow:0 20px 35px rgba(2,6,23,.35)}',
    '.confirm-dialog h3{margin:0 0 8px}',
    '.confirm-dialog p{margin:0 0 14px;color:var(--text-soft)}',
    '.confirm-actions{display:flex;justify-content:flex-end;gap:10px}',
    '.confirm-actions button{padding:9px 12px;border-radius:10px;border:1px solid var(--border);background:var(--surface-muted);color:var(--text);font-weight:700;cursor:pointer}',
    '.confirm-btn{background:linear-gradient(135deg,var(--primary),var(--primary-strong)) !important;border-color:transparent !important;color:#fff !important}',
    '.confirm-btn.confirm-danger{background:linear-gradient(135deg,#ef4444,#dc2626) !important}',
    '@media (max-width: 980px){.shell{grid-template-columns:1fr}.sidebar{position:sticky;top:0;z-index:10;flex-direction:row;align-items:center;gap:10px;padding:10px}.brand{padding:0 6px 0 2px;font-size:18px} nav{flex-direction:row;flex:1;overflow:auto} nav a{white-space:nowrap}.logout{margin-top:0}.topbar{padding:14px 14px 8px}}'
  ]
})
export class AppShellComponent {
  isDarkTheme = false;
  confirmDialog: ConfirmDialogState | null = null;
  toasts: ToastMessage[] = [];
  private readonly subscriptions = new Subscription();

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly uiFeedback: UiFeedbackService
  ) {
    const hasDocument = typeof document !== 'undefined';
    this.isDarkTheme = hasDocument ? document.body.classList.contains('theme-dark') : false;

    this.subscriptions.add(
      this.uiFeedback.confirmState$.subscribe((state) => {
        this.confirmDialog = state;
      })
    );

    this.subscriptions.add(
      this.uiFeedback.toast$.subscribe((toast) => {
        this.toasts = [...this.toasts, toast];
        if (typeof window !== 'undefined') {
          window.setTimeout(() => this.dismissToast(toast.id), toast.durationMs);
        }
      })
    );
  }

  toggleTheme(): void {
    const hasWindow = typeof window !== 'undefined';
    const hasDocument = typeof document !== 'undefined';
    if (!hasWindow || !hasDocument) {
      return;
    }

    this.isDarkTheme = !this.isDarkTheme;
    if (this.isDarkTheme) {
      document.body.classList.add('theme-dark');
      window.localStorage.setItem('sgi-lib-theme', 'dark');
      return;
    }

    document.body.classList.remove('theme-dark');
    window.localStorage.setItem('sgi-lib-theme', 'light');
  }

  acceptConfirm(): void {
    this.uiFeedback.acceptConfirm();
  }

  cancelConfirm(): void {
    this.uiFeedback.cancelConfirm();
  }

  dismissToast(id: number): void {
    this.toasts = this.toasts.filter((toast) => toast.id !== id);
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }

  ngOnDestroy(): void {
    this.subscriptions.unsubscribe();
  }
}
