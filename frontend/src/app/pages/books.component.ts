import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Subscription, finalize, firstValueFrom, interval } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { ApiService } from '../core/api.service';
import { Author, AuthorRequest, Book, BookRequest, GoogleBookMetadata } from '../core/models';
import { UiFeedbackService } from '../core/ui-feedback.service';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="page">
      <h1>Libros</h1>

      <section class="panel">
        <h2>{{ editingBookId ? 'Editar libro' : 'Nuevo libro' }}</h2>
        <form class="book-form" (ngSubmit)="saveBook()">
          <div class="form-field">
            <label for="book-title">Título</label>
            <input id="book-title" type="text" placeholder="Ej: Cien años de soledad" [(ngModel)]="bookForm.title" name="title" required maxlength="200" />
          </div>

          <div class="form-field isbn-field">
            <label for="book-isbn">ISBN</label>
            <input id="book-isbn" type="text" placeholder="Ej: 9780307474728" [(ngModel)]="bookForm.isbn" name="isbn" required maxlength="20" />
            <button type="button" (click)="prefillFormByIsbn()" [disabled]="isPrefillingForm">
              {{ isPrefillingForm ? 'Rellenando...' : 'Rellenar metadatos' }}
            </button>
          </div>

          <div class="form-field author-field" *ngIf="!useNewAuthor">
            <label for="book-author">Autor</label>
            <div class="author-existing-row">
              <select id="book-author" [(ngModel)]="bookForm.authorId" name="authorId" required>
                <option [ngValue]="0">Selecciona autor</option>
                <option *ngFor="let author of authors" [ngValue]="author.id">{{ author.name }}</option>
              </select>
              <button type="button" (click)="enableNewAuthorMode()">+ Nuevo autor</button>
            </div>
          </div>

          <div class="form-field author-new-field" *ngIf="useNewAuthor">
            <label>Autor nuevo</label>
            <div class="author-new-grid">
              <input
                type="text"
                placeholder="Nombre del autor"
                [(ngModel)]="newAuthorForm.name"
                name="newAuthorName"
                maxlength="120"
                required
              />
              <input
                type="text"
                placeholder="Nacionalidad (opcional)"
                [(ngModel)]="newAuthorForm.nationality"
                name="newAuthorNationality"
                maxlength="80"
              />
              <button type="button" (click)="disableNewAuthorMode()">Usar autor existente</button>
            </div>
          </div>

          <div class="form-field">
            <label for="book-year">Año</label>
            <input id="book-year" type="number" placeholder="Ej: 2026" [(ngModel)]="bookForm.publicationYear" name="publicationYear" min="1400" max="3000" required />
          </div>

          <div class="form-field">
            <label for="book-stock">Stock inicial</label>
            <input id="book-stock" type="number" placeholder="Ej: 1" [(ngModel)]="bookForm.stock" name="stock" min="0" required />
          </div>

          <div class="actions">
            <button type="submit">{{ editingBookId ? 'Guardar cambios' : 'Crear libro' }}</button>
            <button type="button" (click)="resetBookForm()" *ngIf="editingBookId">Cancelar</button>
          </div>
        </form>
        <p *ngIf="bookError" class="error">{{ bookError }}</p>
      </section>

      <section class="panel">
        <h2>Buscar metadatos y portada (Google Books)</h2>
        <div class="enrichment-form">
          <input type="text" placeholder="Ingresa ISBN (ej: 9780307474728)" [(ngModel)]="isbnToEnrich" name="isbnToEnrich" />
          <button (click)="enrichByIsbn()" [disabled]="isLoadingMetadata">
            {{ isLoadingMetadata ? 'Buscando...' : 'Buscar' }}
          </button>
          <button type="button" (click)="refreshAllCovers()" [disabled]="isBulkUpdatingCovers || isLoadingBooks || !books.length">
            {{ isBulkUpdatingCovers ? 'Actualizando portadas...' : 'Actualizar todas las portadas' }}
          </button>
        </div>
        <p *ngIf="metadataError" class="error">{{ metadataError }}</p>
        <p *ngIf="isBulkUpdatingCovers" class="muted">Procesando actualización masiva, por favor espera...</p>

        <article class="metadata-preview" *ngIf="metadataPreview">
          <img *ngIf="metadataPreview.coverUrl" [src]="metadataPreview.coverUrl" alt="Portada sugerida" />
          <div>
            <h3>{{ metadataPreview.title }}</h3>
            <p *ngIf="metadataPreview.subtitle"><strong>Subtítulo:</strong> {{ metadataPreview.subtitle }}</p>
            <p *ngIf="metadataPreview.authorName"><strong>Autor sugerido:</strong> {{ metadataPreview.authorName }}</p>
            <p *ngIf="metadataPreview.publisher"><strong>Editorial:</strong> {{ metadataPreview.publisher }}</p>
            <p *ngIf="metadataPreview.category"><strong>Categoría:</strong> {{ metadataPreview.category }}</p>
            <p *ngIf="metadataPreview.publicationYear"><strong>Año:</strong> {{ metadataPreview.publicationYear }}</p>
            <p *ngIf="metadataPreview.pageCount"><strong>Páginas:</strong> {{ metadataPreview.pageCount }}</p>
            <p *ngIf="metadataPreview.description">{{ metadataPreview.description }}</p>
          </div>
        </article>
      </section>

      <section class="panel">
        <h2>Catálogo</h2>
        <input
          type="text"
          placeholder="Buscar por título, autor o ISBN"
          [(ngModel)]="searchText"
          (ngModelChange)="onSearchChanged()"
          name="searchText"
          class="search"
        />
      </section>

      <table *ngIf="sortedBooks.length; else empty">
        <thead>
          <tr>
            <th>Portada</th>
            <th><button type="button" class="sort-btn" (click)="setSort('title')">Título {{ sortMark('title') }}</button></th>
            <th>ISBN</th>
            <th><button type="button" class="sort-btn" (click)="setSort('authorName')">Autor {{ sortMark('authorName') }}</button></th>
            <th>Categoría</th>
            <th><button type="button" class="sort-btn" (click)="setSort('publicationYear')">Año {{ sortMark('publicationYear') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('stock')">Stock {{ sortMark('stock') }}</button></th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let book of pagedBooks">
            <td>
              <img *ngIf="book.coverUrl; else noCover" class="cover" [src]="book.coverUrl" [alt]="book.title" />
              <ng-template #noCover>
                <span class="cover-fallback">Sin portada</span>
              </ng-template>
            </td>
            <td>{{ book.title }}</td>
            <td>{{ book.isbn }}</td>
            <td>{{ book.authorName }}</td>
            <td>
              <span class="badge" [ngClass]="book.category ? 'badge-info' : 'badge-muted'">
                {{ book.category || 'Sin categoría' }}
              </span>
            </td>
            <td>{{ book.publicationYear }}</td>
            <td>
              <span class="badge" [ngClass]="book.stock === 0 ? 'badge-danger' : (book.stock <= 3 ? 'badge-warn' : 'badge-success')">
                {{ book.stock === 0 ? 'Sin stock' : (book.stock <= 3 ? ('Stock bajo: ' + book.stock) : ('Disponible: ' + book.stock)) }}
              </span>
            </td>
            <td class="actions">
              <button type="button" class="action-btn" (click)="editBook(book)">✏️ Editar</button>
              <button type="button" class="danger action-btn" (click)="removeBook(book)">🗑️ Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <section class="pagination" *ngIf="totalPages > 1 && !isLoadingBooks">
        <button type="button" (click)="changePage(-1)" [disabled]="page === 1">← Anterior</button>
        <span>Página {{ page }} de {{ totalPages }}</span>
        <button type="button" (click)="changePage(1)" [disabled]="page === totalPages">Siguiente →</button>
      </section>

      <section class="skeleton-card" *ngIf="isLoadingBooks">
        <div class="skeleton-line w-35"></div>
        <div class="skeleton-line w-85"></div>
        <div class="skeleton-line w-70"></div>
        <div class="skeleton-line w-55"></div>
      </section>

      <ng-template #empty>
        <section class="empty-state" *ngIf="!isLoadingBooks">
          <h3>{{ hasSearchTerm ? 'Sin resultados para la búsqueda' : 'No hay libros disponibles' }}</h3>
          <p>
            {{ hasSearchTerm
              ? ('No se encontraron coincidencias para "' + searchText.trim() + '". Intenta con otro título, autor o ISBN.')
              : 'Crea tu primer libro para comenzar a gestionar el catálogo.' }}
          </p>
        </section>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:8px 20px 20px;max-width:1200px;margin:0 auto}',
    'h1{margin:0 0 14px;font-size:30px;letter-spacing:-.3px}',
    '.panel{background:rgba(255,255,255,.92);backdrop-filter:blur(6px);border:1px solid var(--border);border-radius:14px;padding:16px;margin-bottom:16px;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    '.panel h2{margin:0 0 12px;font-size:18px}',
    '.book-form{display:grid;grid-template-columns:repeat(5,minmax(130px,1fr));gap:10px;align-items:end}',
    '.book-form .actions{grid-column:1 / -1}',
    '.form-field{display:flex;flex-direction:column;gap:6px;min-width:0}',
    '.form-field label{font-size:12px;font-weight:700;color:var(--text-soft)}',
    '.isbn-field{grid-column:span 2}',
    '.author-field{grid-column:span 2}',
    '.isbn-field input{width:100%}',
    '.author-existing-row{display:flex;gap:8px;align-items:center}',
    '.author-existing-row select{flex:1;min-width:0}',
    '.author-new-field{grid-column:1 / -1}',
    '.author-new-grid{display:grid;grid-template-columns:2fr 1.2fr auto;gap:8px;align-items:center}',
    '.author-new-grid input{min-width:0}',
    '.enrichment-form{display:flex;gap:10px;align-items:center}',
    '.enrichment-form input{flex:1}',
    '.metadata-preview{margin-top:14px;display:flex;gap:14px;background:var(--surface-muted);padding:12px;border:1px solid var(--border);border-radius:12px}',
    '.metadata-preview img{width:90px;height:130px;object-fit:cover;border-radius:8px;border:1px solid var(--border)}',
    '.metadata-preview p{margin:4px 0}',
    '.search{width:100%}',
    'input,select{padding:10px;border:1px solid #cbd5e1;border-radius:10px;background:#fff;outline:none;transition:border-color .2s, box-shadow .2s}',
    'input:focus,select:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.15)}',
    'button{padding:9px 12px;border:1px solid var(--border);border-radius:10px;background:#fff;color:var(--text);font-weight:600;transition:all .2s;cursor:pointer}',
    'button[type="submit"]{background:linear-gradient(135deg,var(--primary),var(--primary-strong));color:#fff;border-color:transparent}',
    'button:hover{border-color:#94a3b8;transform:translateY(-1px)}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.action-btn{min-width:88px;justify-content:center}',
    '.badge{display:inline-flex;align-items:center;padding:4px 9px;border-radius:999px;font-size:12px;font-weight:700;border:1px solid transparent}',
    '.badge-muted{background:var(--surface-muted);color:var(--text-soft);border-color:var(--border)}',
    '.badge-info{background:#eff6ff;color:#1d4ed8;border-color:#bfdbfe}',
    '.badge-success{background:#ecfdf3;color:#166534;border-color:#86efac}',
    '.badge-warn{background:#fff7ed;color:#9a3412;border-color:#fdba74}',
    '.badge-danger{background:#fef2f2;color:#991b1b;border-color:#fecaca}',
    'body.theme-dark .badge-info{background:#172554;color:#bfdbfe;border-color:#1d4ed8}',
    'body.theme-dark .badge-success{background:#052e16;color:#86efac;border-color:#166534}',
    'body.theme-dark .badge-warn{background:#431407;color:#fdba74;border-color:#9a3412}',
    'body.theme-dark .badge-danger{background:#3b1217;color:#fca5a5;border-color:#7f1d1d}',
    '.danger{border-color:var(--danger-soft);color:#991b1b;background:#fff7f7}',
    '.error{color:#b91c1c;margin-top:8px;font-weight:600}',
    '.muted{color:var(--text-soft);margin-top:8px;font-weight:600}',
    'table{width:100%;border-collapse:separate;border-spacing:0;background:var(--surface);border:1px solid var(--border);border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    'th,td{padding:12px;border-bottom:1px solid var(--border);text-align:left;vertical-align:top}',
    'th{background:var(--surface-muted);font-size:12px;text-transform:uppercase;letter-spacing:.04em;color:var(--text-soft)}',
    '.sort-btn{all:unset;cursor:pointer;font-weight:700;color:var(--text-soft)}',
    'tbody tr:hover{background:#f8fbff}',
    'tbody tr:last-child td{border-bottom:none}',
    '.pagination{display:flex;justify-content:flex-end;align-items:center;gap:10px;margin-top:10px}',
    '.pagination span{color:var(--text-soft);font-weight:600}',
    '.cover{width:52px;height:76px;object-fit:cover;border-radius:6px;border:1px solid var(--border)}',
    '.cover-fallback{font-size:12px;color:#64748b}',
    '@media (max-width: 920px){.book-form{grid-template-columns:1fr 1fr}.isbn-field,.author-field{grid-column:span 2}.author-new-grid{grid-template-columns:1fr}.metadata-preview{flex-direction:column}.page{padding:8px 12px 14px}}',
    '@media (max-width: 620px){.book-form{grid-template-columns:1fr}.isbn-field,.author-field{grid-column:1}.author-new-field{grid-column:1}}'
  ]
})
export class BooksComponent implements OnInit, OnDestroy {
  books: Book[] = [];
  authors: Author[] = [];
  searchText = '';
  isLoadingBooks = false;
  page = 1;
  readonly pageSize = 8;
  sortField: 'title' | 'authorName' | 'publicationYear' | 'stock' = 'title';
  sortDirection: 'asc' | 'desc' = 'asc';

  editingBookId: number | null = null;
  bookError: string | null = null;

  bookForm: BookRequest = {
    title: '',
    isbn: '',
    publicationYear: new Date().getFullYear(),
    stock: 1,
    authorId: 0
  };

  useNewAuthor = false;
  newAuthorForm: AuthorRequest = {
    name: '',
    nationality: ''
  };

  isbnToEnrich = '';
  metadataPreview: GoogleBookMetadata | null = null;
  metadataError: string | null = null;
  isLoadingMetadata = false;
  isPrefillingForm = false;
  isBulkUpdatingCovers = false;
  private autoRefreshSubscription?: Subscription;

  constructor(
    private readonly apiService: ApiService,
    private readonly uiFeedback: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.reloadData();
    this.startAutoRefresh();
  }

  ngOnDestroy(): void {
    this.autoRefreshSubscription?.unsubscribe();
  }

  get filteredBooks(): Book[] {
    const term = this.searchText.trim().toLowerCase();
    if (!term) {
      return this.books;
    }

    return this.books.filter((book) =>
      book.title.toLowerCase().includes(term) ||
      book.authorName.toLowerCase().includes(term) ||
      book.isbn.toLowerCase().includes(term)
    );
  }

  get hasSearchTerm(): boolean {
    return this.searchText.trim().length > 0;
  }

  get sortedBooks(): Book[] {
    const books = [...this.filteredBooks];
    books.sort((left, right) => {
      const leftValue = left[this.sortField];
      const rightValue = right[this.sortField];

      let result = 0;
      if (typeof leftValue === 'number' && typeof rightValue === 'number') {
        result = leftValue - rightValue;
      } else {
        result = String(leftValue).localeCompare(String(rightValue), 'es', { sensitivity: 'base' });
      }

      return this.sortDirection === 'asc' ? result : -result;
    });
    return books;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sortedBooks.length / this.pageSize));
  }

  get pagedBooks(): Book[] {
    const start = (this.page - 1) * this.pageSize;
    return this.sortedBooks.slice(start, start + this.pageSize);
  }

  setSort(field: 'title' | 'authorName' | 'publicationYear' | 'stock'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.page = 1;
  }

  sortMark(field: 'title' | 'authorName' | 'publicationYear' | 'stock'): string {
    if (this.sortField !== field) {
      return '↕';
    }
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  changePage(delta: number): void {
    const next = this.page + delta;
    this.page = Math.min(this.totalPages, Math.max(1, next));
  }

  onSearchChanged(): void {
    this.page = 1;
  }

  reloadData(): void {
    this.isLoadingBooks = true;
    this.apiService.getBooks()
      .pipe(finalize(() => (this.isLoadingBooks = false)))
      .subscribe((response) => {
        this.applyBooksSnapshot(response.content ?? [], true);
        this.page = 1;
      });

    this.apiService.getAuthors().subscribe((response) => {
      this.authors = response.content ?? [];
    });
  }

  private startAutoRefresh(): void {
    this.autoRefreshSubscription = interval(8000).subscribe(() => {
      if (this.isLoadingBooks || this.isBulkUpdatingCovers || this.isPrefillingForm || this.isLoadingMetadata) {
        return;
      }

      this.apiService.getBooks().subscribe({
        next: (response) => {
          this.applyBooksSnapshot(response.content ?? [], false);
        },
        error: () => {
          // Ignorar errores temporales en auto-refresh para no interrumpir al usuario.
        }
      });
    });
  }

  private applyBooksSnapshot(nextBooks: Book[], resetPage: boolean): void {
    this.books = nextBooks;

    if (resetPage) {
      this.page = 1;
      return;
    }

    const maxPage = Math.max(1, Math.ceil(this.sortedBooks.length / this.pageSize));
    if (this.page > maxPage) {
      this.page = maxPage;
    }
  }

  async saveBook(): Promise<void> {
    this.bookError = null;

    const title = this.bookForm.title.trim();
    const isbn = this.bookForm.isbn.trim();

    if (!title || !isbn) {
      this.bookError = 'Título, ISBN y autor son obligatorios.';
      return;
    }

    let authorId = Number(this.bookForm.authorId);

    if (this.useNewAuthor) {
      const newAuthorName = this.newAuthorForm.name.trim();
      if (!newAuthorName) {
        this.bookError = 'Ingresa el nombre del autor nuevo.';
        return;
      }

      const existingAuthor = this.authors.find(
        (author) => this.normalizeText(author.name) === this.normalizeText(newAuthorName)
      );

      if (existingAuthor) {
        authorId = existingAuthor.id;
      } else {
        try {
          const createdAuthor = await firstValueFrom(
            this.apiService.createAuthor({
              name: newAuthorName,
              nationality: this.newAuthorForm.nationality?.trim() || undefined
            })
          );
          authorId = createdAuthor.id;
          this.authors = [...this.authors, createdAuthor];
        } catch (error: any) {
          this.bookError = error?.error?.detail ?? 'No se pudo crear el autor nuevo.';
          this.uiFeedback.showError(this.bookError ?? 'No se pudo crear el autor nuevo.');
          return;
        }
      }
    }

    if (!authorId) {
      this.bookError = 'Título, ISBN y autor son obligatorios.';
      return;
    }

    const request: BookRequest = {
      title,
      isbn,
      publicationYear: Number(this.bookForm.publicationYear),
      stock: Number(this.bookForm.stock),
      authorId
    };

    try {
      const operation = this.editingBookId
        ? this.apiService.updateBook(this.editingBookId, request)
        : this.apiService.createBook(request);

      await firstValueFrom(operation);
      this.uiFeedback.showSuccess(this.editingBookId ? 'Libro actualizado.' : 'Libro creado.');
      this.resetBookForm();
      this.reloadData();
    } catch (err: any) {
      this.bookError = err?.error?.detail ?? 'No se pudo guardar el libro.';
      this.uiFeedback.showError(this.bookError ?? 'No se pudo guardar el libro.');
    }
  }

  editBook(book: Book): void {
    this.disableNewAuthorMode();
    this.editingBookId = book.id;
    this.bookForm = {
      title: book.title,
      isbn: book.isbn,
      publicationYear: book.publicationYear,
      stock: book.stock,
      authorId: book.authorId
    };
  }

  async removeBook(book: Book): Promise<void> {
    const confirmed = await this.uiFeedback.confirm({
      title: 'Eliminar libro',
      message: `¿Eliminar libro ${book.title}?`,
      confirmText: 'Eliminar',
      cancelText: 'Cancelar',
      danger: true
    });

    if (!confirmed) {
      return;
    }

    this.apiService.deleteBook(book.id).subscribe({
      next: () => {
        this.uiFeedback.showSuccess('Libro eliminado.');
        this.reloadData();
      },
      error: (err) => {
        this.bookError = err?.error?.detail ?? 'No se pudo eliminar el libro.';
        this.uiFeedback.showError(this.bookError ?? 'No se pudo eliminar el libro.');
      }
    });
  }

  resetBookForm(): void {
    this.editingBookId = null;
    this.disableNewAuthorMode();
    this.bookForm = {
      title: '',
      isbn: '',
      publicationYear: new Date().getFullYear(),
      stock: 1,
      authorId: 0
    };
  }

  enableNewAuthorMode(): void {
    this.useNewAuthor = true;
    this.bookForm.authorId = 0;
  }

  disableNewAuthorMode(): void {
    this.useNewAuthor = false;
    this.newAuthorForm = {
      name: '',
      nationality: ''
    };
  }

  enrichByIsbn(): void {
    const isbn = this.isbnToEnrich.trim();
    this.metadataPreview = null;
    this.metadataError = null;

    if (!isbn) {
      this.metadataError = 'Ingresa un ISBN válido.';
      return;
    }

    this.isLoadingMetadata = true;
    this.apiService.enrichBookByIsbn(isbn).subscribe({
      next: (metadata) => {
        this.metadataPreview = metadata;
        this.isLoadingMetadata = false;
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.metadataError = 'No se encontraron metadatos para ese ISBN. Verifica el código o prueba otro.';
        } else if (error.status === 0) {
          this.metadataError = 'No se pudo conectar al backend. Revisa que el servicio esté activo.';
        } else {
          this.metadataError = 'Error consultando metadatos. Intenta nuevamente en unos segundos.';
        }
        this.uiFeedback.showError(this.metadataError);
        this.isLoadingMetadata = false;
      }
    });
  }

  prefillFormByIsbn(): void {
    const isbn = this.bookForm.isbn.trim();
    this.bookError = null;

    if (!isbn) {
      this.bookError = 'Ingresa un ISBN antes de rellenar metadatos.';
      return;
    }

    this.isPrefillingForm = true;
    this.apiService.enrichBookByIsbn(isbn).subscribe({
      next: (metadata) => {
        if (metadata.title?.trim()) {
          this.bookForm.title = metadata.title.trim();
        }
        if (metadata.publicationYear) {
          this.bookForm.publicationYear = metadata.publicationYear;
        }

        this.metadataPreview = metadata;
        this.metadataError = null;
        this.isbnToEnrich = isbn;

        const suggestedAuthor = this.extractPrimaryAuthor(metadata.authorName);
        if (suggestedAuthor) {
          const normalizedSuggestedAuthor = this.normalizeText(suggestedAuthor);
          const matchingAuthor = this.authors.find(
            (author) => this.normalizeText(author.name) === normalizedSuggestedAuthor
          );

          if (matchingAuthor) {
            this.disableNewAuthorMode();
            this.bookForm.authorId = matchingAuthor.id;
          } else {
            this.enableNewAuthorMode();
            this.newAuthorForm.name = suggestedAuthor;
          }
        }

        this.uiFeedback.showSuccess('Metadatos rellenados desde ISBN.');
        this.isPrefillingForm = false;
      },
      error: (error: HttpErrorResponse) => {
        if (error.status === 404) {
          this.bookError = 'No se encontraron metadatos para ese ISBN.';
        } else if (error.status === 0) {
          this.bookError = 'No se pudo conectar al backend para rellenar metadatos.';
        } else {
          this.bookError = 'No se pudieron rellenar metadatos en este momento.';
        }
        this.uiFeedback.showError(this.bookError);
        this.isPrefillingForm = false;
      }
    });
  }

  private normalizeText(value: string): string {
    return value
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .replace(/\s+/g, ' ')
      .trim()
      .toLowerCase();
  }

  private extractPrimaryAuthor(authorName?: string): string {
    if (!authorName) {
      return '';
    }

    const normalized = authorName.trim();
    if (!normalized) {
      return '';
    }

    return normalized.split(/,|;|\|/)[0].trim();
  }

  async refreshAllCovers(): Promise<void> {
    if (!this.books.length || this.isBulkUpdatingCovers) {
      return;
    }

    const confirmed = await this.uiFeedback.confirm({
      title: 'Actualizar portadas',
      message: `Se actualizarán metadatos y portada de ${this.books.length} libros según su ISBN. ¿Deseas continuar?`,
      confirmText: 'Actualizar',
      cancelText: 'Cancelar'
    });

    if (!confirmed) {
      return;
    }

    this.isBulkUpdatingCovers = true;
    this.bookError = null;

    let updated = 0;
    let failed = 0;

    for (const book of this.books) {
      const request: BookRequest = {
        title: book.title,
        isbn: book.isbn,
        publicationYear: book.publicationYear,
        stock: book.stock,
        authorId: book.authorId
      };

      try {
        await firstValueFrom(this.apiService.updateBook(book.id, request));
        updated += 1;
      } catch {
        failed += 1;
      }
    }

    this.isBulkUpdatingCovers = false;
    this.reloadData();

    if (failed === 0) {
      this.uiFeedback.showSuccess(`Portadas actualizadas para ${updated} libros.`);
      return;
    }

    this.uiFeedback.showInfo(`Actualización completada: ${updated} libros actualizados, ${failed} con error.`);
  }

}
