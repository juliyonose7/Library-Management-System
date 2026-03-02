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
        <h1>SGI LIB Login</h1>
        <form [formGroup]="form" (ngSubmit)="submit()">
          <label>Usuario</label>
          <input formControlName="username" type="text" placeholder="admin" />

          <label>Contraseña</label>
          <input formControlName="password" type="password" placeholder="Admin123!" />

          <button type="submit" [disabled]="form.invalid || loading">Entrar</button>
          <p *ngIf="error" class="error">Credenciales inválidas</p>
        </form>
      </section>
    </main>
  `,
  styles: [
    '.container{min-height:100vh;display:grid;place-items:center;background:#f4f6f8;padding:16px}',
    '.card{width:100%;max-width:380px;background:#fff;border:1px solid #ddd;border-radius:8px;padding:24px}',
    'h1{margin:0 0 16px;font-size:22px}',
    'form{display:flex;flex-direction:column;gap:10px}',
    'label{font-size:13px;color:#333}',
    'input{padding:10px;border:1px solid #ccc;border-radius:6px}',
    'button{margin-top:6px;padding:10px;border:0;background:#1f2937;color:#fff;border-radius:6px;cursor:pointer}',
    '.error{color:#b91c1c;font-size:13px;margin:4px 0 0}'
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
