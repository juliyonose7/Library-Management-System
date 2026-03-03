import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Author, AuthorRequest } from '../core/models';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-authors',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="page">
      <header>
        <h1>Autores</h1>
        <nav>
          <a routerLink="/books">Libros</a>
          <a routerLink="/authors">Autores</a>
          <a routerLink="/clients">Clientes</a>
          <a routerLink="/sales">Ventas</a>
          <button (click)="logout()">Salir</button>
        </nav>
      </header>

      <section class="panel">
        <h2>{{ editingAuthorId ? 'Editar autor' : 'Nuevo autor' }}</h2>
        <form class="grid" (ngSubmit)="saveAuthor()">
          <input
            type="text"
            placeholder="Nombre del autor"
            [(ngModel)]="authorForm.name"
            name="name"
            required
            maxlength="150"
          />
          <input
            type="text"
            placeholder="Nacionalidad"
            [(ngModel)]="authorForm.nationality"
            name="nationality"
            maxlength="100"
          />
          <div class="actions">
            <button type="submit">{{ editingAuthorId ? 'Guardar cambios' : 'Crear autor' }}</button>
            <button type="button" (click)="resetForm()" *ngIf="editingAuthorId">Cancelar</button>
          </div>
        </form>
        <p *ngIf="error" class="error">{{ error }}</p>
      </section>

      <table *ngIf="authors.length; else empty">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Nacionalidad</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let author of authors">
            <td>{{ author.name }}</td>
            <td>{{ author.nationality || '-' }}</td>
            <td class="actions">
              <button type="button" (click)="editAuthor(author)">Editar</button>
              <button type="button" class="danger" (click)="removeAuthor(author)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <ng-template #empty>
        <p>No hay autores disponibles.</p>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:20px;max-width:1000px;margin:0 auto}',
    'header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}',
    'nav{display:flex;gap:10px;align-items:center}',
    '.panel{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:12px;margin-bottom:16px}',
    '.panel h2{margin:0 0 10px;font-size:16px}',
    '.grid{display:grid;gap:10px}',
    'input{padding:8px;border:1px solid #cbd5e1;border-radius:6px}',
    'a,button{padding:8px 10px;border:1px solid #ccc;border-radius:6px;text-decoration:none;background:#fff;color:#111}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.danger{border-color:#fecaca;color:#991b1b}',
    '.error{color:#b91c1c;margin-top:8px}',
    'table{width:100%;border-collapse:collapse;background:#fff}',
    'th,td{padding:10px;border:1px solid #e5e7eb;text-align:left}',
    'th{background:#f8fafc}'
  ]
})
export class AuthorsComponent implements OnInit {
  authors: Author[] = [];
  editingAuthorId: number | null = null;
  error: string | null = null;

  authorForm: AuthorRequest = {
    name: '',
    nationality: ''
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
  }

  loadAuthors(): void {
    this.apiService.getAuthors().subscribe((response) => {
      this.authors = response.content ?? [];
    });
  }

  saveAuthor(): void {
    this.error = null;
    const request: AuthorRequest = {
      name: this.authorForm.name?.trim() ?? '',
      nationality: this.authorForm.nationality?.trim() || undefined
    };

    if (!request.name) {
      this.error = 'El nombre es obligatorio.';
      return;
    }

    const operation = this.editingAuthorId
      ? this.apiService.updateAuthor(this.editingAuthorId, request)
      : this.apiService.createAuthor(request);

    operation.subscribe({
      next: () => {
        this.resetForm();
        this.loadAuthors();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo guardar el autor.';
      }
    });
  }

  editAuthor(author: Author): void {
    this.editingAuthorId = author.id;
    this.authorForm = {
      name: author.name,
      nationality: author.nationality ?? ''
    };
  }

  removeAuthor(author: Author): void {
    if (!confirm(`¿Eliminar autor ${author.name}?`)) {
      return;
    }

    this.apiService.deleteAuthor(author.id).subscribe({
      next: () => this.loadAuthors(),
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo eliminar el autor.';
      }
    });
  }

  resetForm(): void {
    this.editingAuthorId = null;
    this.authorForm = {
      name: '',
      nationality: ''
    };
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
