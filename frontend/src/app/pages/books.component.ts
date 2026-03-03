import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Author, Book, BookRequest, GoogleBookMetadata } from '../core/models';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-books',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <main class="page">
      <header>
        <h1>Libros</h1>
        <nav>
          <a routerLink="/books">Libros</a>
          <a routerLink="/authors">Autores</a>
          <a routerLink="/clients">Clientes</a>
          <a routerLink="/sales">Ventas</a>
          <button (click)="logout()">Salir</button>
        </nav>
      </header>

      <section class="panel">
        <h2>{{ editingBookId ? 'Editar libro' : 'Nuevo libro' }}</h2>
        <form class="book-form" (ngSubmit)="saveBook()">
          <input type="text" placeholder="Título" [(ngModel)]="bookForm.title" name="title" required maxlength="200" />
          <input type="text" placeholder="ISBN" [(ngModel)]="bookForm.isbn" name="isbn" required maxlength="20" />
          <select [(ngModel)]="bookForm.authorId" name="authorId" required>
            <option [ngValue]="0">Selecciona autor</option>
            <option *ngFor="let author of authors" [ngValue]="author.id">{{ author.name }}</option>
          </select>
          <input type="number" placeholder="Año" [(ngModel)]="bookForm.publicationYear" name="publicationYear" min="1400" max="3000" required />
          <input type="number" placeholder="Stock" [(ngModel)]="bookForm.stock" name="stock" min="0" required />
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
        </div>
        <p *ngIf="metadataError" class="error">{{ metadataError }}</p>

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
          name="searchText"
          class="search"
        />
      </section>

      <table *ngIf="filteredBooks.length; else empty">
        <thead>
          <tr>
            <th>Portada</th>
            <th>Título</th>
            <th>ISBN</th>
            <th>Autor</th>
            <th>Categoría</th>
            <th>Año</th>
            <th>Stock</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let book of filteredBooks">
            <td>
              <img *ngIf="book.coverUrl; else noCover" class="cover" [src]="book.coverUrl" [alt]="book.title" />
              <ng-template #noCover>
                <span class="cover-fallback">Sin portada</span>
              </ng-template>
            </td>
            <td>{{ book.title }}</td>
            <td>{{ book.isbn }}</td>
            <td>{{ book.authorName }}</td>
            <td>{{ book.category || '-' }}</td>
            <td>{{ book.publicationYear }}</td>
            <td>{{ book.stock }}</td>
            <td class="actions">
              <button type="button" (click)="editBook(book)">Editar</button>
              <button type="button" class="danger" (click)="removeBook(book)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <ng-template #empty>
        <p>No hay libros disponibles.</p>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:20px;max-width:1100px;margin:0 auto}',
    'header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}',
    'nav{display:flex;gap:10px;align-items:center}',
    '.panel{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:12px;margin-bottom:16px}',
    '.panel h2{margin:0 0 10px;font-size:16px}',
    '.book-form{display:grid;grid-template-columns:repeat(5,minmax(120px,1fr));gap:10px;align-items:center}',
    '.book-form .actions{grid-column:1 / -1}',
    '.enrichment-form{display:flex;gap:10px;align-items:center}',
    '.enrichment-form input{flex:1}',
    '.metadata-preview{margin-top:12px;display:flex;gap:12px}',
    '.metadata-preview img{width:90px;height:130px;object-fit:cover;border-radius:6px;border:1px solid #e5e7eb}',
    '.metadata-preview p{margin:4px 0}',
    '.search{width:100%}',
    'input,select{padding:8px;border:1px solid #cbd5e1;border-radius:6px}',
    'a,button{padding:8px 10px;border:1px solid #ccc;border-radius:6px;text-decoration:none;background:#fff;color:#111}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.danger{border-color:#fecaca;color:#991b1b}',
    '.error{color:#b91c1c;margin-top:8px}',
    'table{width:100%;border-collapse:collapse;background:#fff}',
    'th,td{padding:10px;border:1px solid #e5e7eb;text-align:left;vertical-align:top}',
    'th{background:#f8fafc}',
    '.cover{width:48px;height:72px;object-fit:cover;border-radius:4px;border:1px solid #e5e7eb}',
    '.cover-fallback{font-size:12px;color:#64748b}'
  ]
})
export class BooksComponent implements OnInit {
  books: Book[] = [];
  authors: Author[] = [];
  searchText = '';

  editingBookId: number | null = null;
  bookError: string | null = null;

  bookForm: BookRequest = {
    title: '',
    isbn: '',
    publicationYear: new Date().getFullYear(),
    stock: 1,
    authorId: 0
  };

  isbnToEnrich = '';
  metadataPreview: GoogleBookMetadata | null = null;
  metadataError: string | null = null;
  isLoadingMetadata = false;

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.reloadData();
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

  reloadData(): void {
    this.apiService.getBooks().subscribe((response) => {
      this.books = response.content ?? [];
    });

    this.apiService.getAuthors().subscribe((response) => {
      this.authors = response.content ?? [];
    });
  }

  saveBook(): void {
    this.bookError = null;

    const request: BookRequest = {
      title: this.bookForm.title.trim(),
      isbn: this.bookForm.isbn.trim(),
      publicationYear: Number(this.bookForm.publicationYear),
      stock: Number(this.bookForm.stock),
      authorId: Number(this.bookForm.authorId)
    };

    if (!request.title || !request.isbn || !request.authorId) {
      this.bookError = 'Título, ISBN y autor son obligatorios.';
      return;
    }

    const operation = this.editingBookId
      ? this.apiService.updateBook(this.editingBookId, request)
      : this.apiService.createBook(request);

    operation.subscribe({
      next: () => {
        this.resetBookForm();
        this.reloadData();
      },
      error: (err) => {
        this.bookError = err?.error?.detail ?? 'No se pudo guardar el libro.';
      }
    });
  }

  editBook(book: Book): void {
    this.editingBookId = book.id;
    this.bookForm = {
      title: book.title,
      isbn: book.isbn,
      publicationYear: book.publicationYear,
      stock: book.stock,
      authorId: book.authorId
    };
  }

  removeBook(book: Book): void {
    if (!confirm(`¿Eliminar libro ${book.title}?`)) {
      return;
    }

    this.apiService.deleteBook(book.id).subscribe({
      next: () => this.reloadData(),
      error: (err) => {
        this.bookError = err?.error?.detail ?? 'No se pudo eliminar el libro.';
      }
    });
  }

  resetBookForm(): void {
    this.editingBookId = null;
    this.bookForm = {
      title: '',
      isbn: '',
      publicationYear: new Date().getFullYear(),
      stock: 1,
      authorId: 0
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
      error: () => {
        this.metadataError = 'No se encontraron metadatos para ese ISBN.';
        this.isLoadingMetadata = false;
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
