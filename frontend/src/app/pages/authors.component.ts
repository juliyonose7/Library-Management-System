import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Author, AuthorRequest } from '../core/models';
import { UiFeedbackService } from '../core/ui-feedback.service';

@Component({
  selector: 'app-authors',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="page">
      <h1>Autores</h1>

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

      <table *ngIf="sortedAuthors.length && !isLoadingAuthors; else empty">
        <thead>
          <tr>
            <th><button type="button" class="sort-btn" (click)="setSort('name')">Nombre {{ sortMark('name') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('nationality')">Nacionalidad {{ sortMark('nationality') }}</button></th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let author of pagedAuthors">
            <td>{{ author.name }}</td>
            <td>
              <span class="badge" [ngClass]="author.nationality ? 'badge-info' : 'badge-muted'">
                {{ author.nationality || 'Sin dato' }}
              </span>
            </td>
            <td class="actions">
              <button type="button" class="action-btn" (click)="editAuthor(author)">✏️ Editar</button>
              <button type="button" class="danger action-btn" (click)="removeAuthor(author)">🗑️ Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <section class="pagination" *ngIf="totalPages > 1 && !isLoadingAuthors">
        <button type="button" (click)="changePage(-1)" [disabled]="page === 1">← Anterior</button>
        <span>Página {{ page }} de {{ totalPages }}</span>
        <button type="button" (click)="changePage(1)" [disabled]="page === totalPages">Siguiente →</button>
      </section>

      <ng-template #empty>
        <section class="skeleton-card" *ngIf="isLoadingAuthors">
          <div class="skeleton-line w-35"></div>
          <div class="skeleton-line w-85"></div>
          <div class="skeleton-line w-70"></div>
        </section>
        <section class="empty-state" *ngIf="!isLoadingAuthors">
          <h3>No hay autores disponibles</h3>
          <p>Registra autores para vincularlos con libros y enriquecer el catálogo.</p>
        </section>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:8px 20px 20px;max-width:1100px;margin:0 auto}',
    'h1{margin:0 0 14px;font-size:30px;letter-spacing:-.3px}',
    '.panel{background:rgba(255,255,255,.92);backdrop-filter:blur(6px);border:1px solid var(--border);border-radius:14px;padding:16px;margin-bottom:16px;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    '.panel h2{margin:0 0 12px;font-size:18px}',
    '.grid{display:grid;gap:10px}',
    'input{padding:10px;border:1px solid #cbd5e1;border-radius:10px;outline:none;transition:border-color .2s, box-shadow .2s}',
    'input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.15)}',
    'button{padding:9px 12px;border:1px solid var(--border);border-radius:10px;background:#fff;color:var(--text);font-weight:600;transition:all .2s;cursor:pointer}',
    'button[type="submit"]{background:linear-gradient(135deg,var(--primary),var(--primary-strong));color:#fff;border-color:transparent}',
    'button:hover{border-color:#94a3b8;transform:translateY(-1px)}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.action-btn{min-width:88px;justify-content:center}',
    '.badge{display:inline-flex;align-items:center;padding:4px 9px;border-radius:999px;font-size:12px;font-weight:700;border:1px solid transparent}',
    '.badge-muted{background:var(--surface-muted);color:var(--text-soft);border-color:var(--border)}',
    '.badge-info{background:#eff6ff;color:#1d4ed8;border-color:#bfdbfe}',
    'body.theme-dark .badge-info{background:#172554;color:#bfdbfe;border-color:#1d4ed8}',
    '.danger{border-color:var(--danger-soft);color:#991b1b;background:#fff7f7}',
    '.error{color:#b91c1c;margin-top:8px;font-weight:600}',
    'table{width:100%;border-collapse:separate;border-spacing:0;background:var(--surface);border:1px solid var(--border);border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    'th,td{padding:12px;border-bottom:1px solid var(--border);text-align:left}',
    'th{background:var(--surface-muted);font-size:12px;text-transform:uppercase;letter-spacing:.04em;color:var(--text-soft)}',
    '.sort-btn{all:unset;cursor:pointer;font-weight:700;color:var(--text-soft)}',
    'tbody tr:hover{background:#f8fbff}',
    'tbody tr:last-child td{border-bottom:none}',
    '.pagination{display:flex;justify-content:flex-end;align-items:center;gap:10px;margin-top:10px}',
    '.pagination span{color:var(--text-soft);font-weight:600}',
    '@media (max-width: 620px){.page{padding:8px 12px 14px}}'
  ]
})
export class AuthorsComponent implements OnInit {
  authors: Author[] = [];
  isLoadingAuthors = false;
  page = 1;
  readonly pageSize = 8;
  sortField: 'name' | 'nationality' = 'name';
  sortDirection: 'asc' | 'desc' = 'asc';
  editingAuthorId: number | null = null;
  error: string | null = null;

  authorForm: AuthorRequest = {
    name: '',
    nationality: ''
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly uiFeedback: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
  }

  get sortedAuthors(): Author[] {
    const authors = [...this.authors];
    authors.sort((left, right) => {
      const leftValue = String(left[this.sortField] ?? '');
      const rightValue = String(right[this.sortField] ?? '');
      const result = leftValue.localeCompare(rightValue, 'es', { sensitivity: 'base' });
      return this.sortDirection === 'asc' ? result : -result;
    });
    return authors;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sortedAuthors.length / this.pageSize));
  }

  get pagedAuthors(): Author[] {
    const start = (this.page - 1) * this.pageSize;
    return this.sortedAuthors.slice(start, start + this.pageSize);
  }

  setSort(field: 'name' | 'nationality'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.page = 1;
  }

  sortMark(field: 'name' | 'nationality'): string {
    if (this.sortField !== field) {
      return '↕';
    }
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  changePage(delta: number): void {
    const next = this.page + delta;
    this.page = Math.min(this.totalPages, Math.max(1, next));
  }

  loadAuthors(): void {
    this.isLoadingAuthors = true;
    this.apiService.getAuthors()
      .pipe(finalize(() => (this.isLoadingAuthors = false)))
      .subscribe((response) => {
        this.authors = response.content ?? [];
        this.page = 1;
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
        this.uiFeedback.showSuccess(this.editingAuthorId ? 'Autor actualizado.' : 'Autor creado.');
        this.resetForm();
        this.loadAuthors();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo guardar el autor.';
        this.uiFeedback.showError(this.error ?? 'No se pudo guardar el autor.');
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

  async removeAuthor(author: Author): Promise<void> {
    const confirmed = await this.uiFeedback.confirm({
      title: 'Eliminar autor',
      message: `¿Eliminar autor ${author.name}?`,
      confirmText: 'Eliminar',
      cancelText: 'Cancelar',
      danger: true
    });

    if (!confirmed) {
      return;
    }

    this.apiService.deleteAuthor(author.id).subscribe({
      next: () => {
        this.uiFeedback.showSuccess('Autor eliminado.');
        this.loadAuthors();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo eliminar el autor.';
        this.uiFeedback.showError(this.error ?? 'No se pudo eliminar el autor.');
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

}
