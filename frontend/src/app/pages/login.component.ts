import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <main class="container">
      <section class="card">
        <h1>SGI LIB</h1>
        <p class="subtitle">Gestión de librería moderna</p>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>Usuario</label>
          <input formControlName="username" type="text" placeholder="admin" />

          <label>Contraseña</label>
          <input formControlName="password" type="password" placeholder="Ingresa tu contraseña" />

          <button type="submit" [disabled]="form.invalid || loading">Entrar</button>
          <p *ngIf="error" class="error">Credenciales inválidas</p>
        </form>
      </section>
    </main>
  `,
  styles: [
    '.container{min-height:100vh;display:grid;place-items:center;padding:24px}',
    '.card{width:100%;max-width:420px;background:rgba(255,255,255,.95);backdrop-filter:blur(8px);border:1px solid var(--border);border-radius:18px;padding:28px;box-shadow:0 18px 45px rgba(15,23,42,.12)}',
    'h1{margin:0;font-size:32px;letter-spacing:-.5px}',
    '.subtitle{margin:8px 0 20px;color:var(--text-soft)}',
    'form{display:flex;flex-direction:column;gap:10px}',
    'label{font-size:13px;color:var(--text-soft);font-weight:600}',
    'input{padding:11px;border:1px solid #cbd5e1;border-radius:10px;outline:none;transition:border-color .2s, box-shadow .2s}',
    'input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.15)}',
    'button{margin-top:8px;padding:11px;border:0;background:linear-gradient(135deg,var(--primary),var(--primary-strong));color:#fff;border-radius:10px;font-weight:700;cursor:pointer;transition:transform .2s, opacity .2s}',
    'button:hover{transform:translateY(-1px)}',
    'button:disabled{opacity:.6;cursor:not-allowed;transform:none}',
    '.error{color:#b91c1c;font-size:13px;margin:4px 0 0;font-weight:600}'
  ]
})
export class LoginComponent {
  loading = false;
  error = false;
  form!: ReturnType<FormBuilder['group']>;

  constructor(
    private readonly fb: FormBuilder,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  submit(): void {
    if (this.form.invalid || this.loading) {
      return;
    }
    this.loading = true;
    this.error = false;

    this.authService.login(this.form.getRawValue() as { username: string; password: string }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigateByUrl('/books');
      },
      error: () => {
        this.loading = false;
        this.error = true;
      }
    });
  }
}
