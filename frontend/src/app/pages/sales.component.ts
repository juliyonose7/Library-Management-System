import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Book, Client, Sale, SaleRequest } from '../core/models';
import { UiFeedbackService } from '../core/ui-feedback.service';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="page">
      <h1>Ventas</h1>

      <section class="panel">
        <h2>Registrar venta</h2>
        <form class="grid" (ngSubmit)="createSale()">
          <select [(ngModel)]="saleForm.clientId" name="clientId" required>
            <option [ngValue]="0">Selecciona cliente</option>
            <option *ngFor="let client of clients" [ngValue]="client.id">
              {{ client.firstName }} {{ client.lastName }}
            </option>
          </select>

          <select [(ngModel)]="saleForm.bookId" name="bookId" required>
            <option [ngValue]="0">Selecciona libro</option>
            <option *ngFor="let book of books" [ngValue]="book.id">
              {{ book.title }} (stock: {{ book.stock }})
            </option>
          </select>

          <input
            type="number"
            min="1"
            placeholder="Cantidad"
            [(ngModel)]="saleForm.quantity"
            name="quantity"
            required
          />

          <div class="actions">
            <button type="submit">Registrar</button>
          </div>
        </form>
        <p *ngIf="error" class="error">{{ error }}</p>
      </section>

      <section class="panel">
        <h2>Historial</h2>
        <div class="filters">
          <select [(ngModel)]="historyClientId" name="historyClientId" (change)="loadSales()">
            <option [ngValue]="0">Todos los clientes</option>
            <option *ngFor="let client of clients" [ngValue]="client.id">
              {{ client.firstName }} {{ client.lastName }}
            </option>
          </select>
          <button type="button" (click)="loadSales()">Actualizar</button>
        </div>
      </section>

      <table *ngIf="sortedSales.length && !isLoadingSales; else empty">
        <thead>
          <tr>
            <th><button type="button" class="sort-btn" (click)="setSort('soldAt')">Fecha {{ sortMark('soldAt') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('clientName')">Cliente {{ sortMark('clientName') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('bookTitle')">Libro {{ sortMark('bookTitle') }}</button></th>
            <th>ISBN</th>
            <th><button type="button" class="sort-btn" (click)="setSort('quantity')">Cantidad {{ sortMark('quantity') }}</button></th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let sale of pagedSales">
            <td>{{ sale.soldAt | date:'yyyy-MM-dd HH:mm' }}</td>
            <td>{{ sale.clientName }}</td>
            <td>{{ sale.bookTitle }}</td>
            <td>{{ sale.bookIsbn }}</td>
            <td>
              <span class="badge" [ngClass]="sale.quantity > 1 ? 'badge-info' : 'badge-success'">
                {{ sale.quantity > 1 ? ('x' + sale.quantity) : '1 unidad' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>

      <section class="pagination" *ngIf="totalPages > 1 && !isLoadingSales">
        <button type="button" (click)="changePage(-1)" [disabled]="page === 1">← Anterior</button>
        <span>Página {{ page }} de {{ totalPages }}</span>
        <button type="button" (click)="changePage(1)" [disabled]="page === totalPages">Siguiente →</button>
      </section>

      <ng-template #empty>
        <section class="skeleton-card" *ngIf="isLoadingSales">
          <div class="skeleton-line w-35"></div>
          <div class="skeleton-line w-85"></div>
          <div class="skeleton-line w-70"></div>
        </section>
        <section class="empty-state" *ngIf="!isLoadingSales">
          <h3>No hay ventas registradas</h3>
          <p>Registra una venta para comenzar a construir el historial comercial.</p>
        </section>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:8px 20px 20px;max-width:1200px;margin:0 auto}',
    'h1{margin:0 0 14px;font-size:30px;letter-spacing:-.3px}',
    '.panel{background:rgba(255,255,255,.92);backdrop-filter:blur(6px);border:1px solid var(--border);border-radius:14px;padding:16px;margin-bottom:16px;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    '.panel h2{margin:0 0 12px;font-size:18px}',
    '.grid{display:grid;grid-template-columns:repeat(3,minmax(140px,1fr));gap:10px;align-items:center}',
    '.grid .actions{grid-column:1 / -1}',
    '.filters{display:flex;gap:10px;align-items:center}',
    'input,select{padding:10px;border:1px solid #cbd5e1;border-radius:10px;outline:none;transition:border-color .2s, box-shadow .2s}',
    'input:focus,select:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.15)}',
    'button{padding:9px 12px;border:1px solid var(--border);border-radius:10px;background:#fff;color:var(--text);font-weight:600;transition:all .2s;cursor:pointer}',
    'button[type="submit"]{background:linear-gradient(135deg,var(--primary),var(--primary-strong));color:#fff;border-color:transparent}',
    'button:hover{border-color:#94a3b8;transform:translateY(-1px)}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.badge{display:inline-flex;align-items:center;padding:4px 9px;border-radius:999px;font-size:12px;font-weight:700;border:1px solid transparent}',
    '.badge-info{background:#eff6ff;color:#1d4ed8;border-color:#bfdbfe}',
    '.badge-success{background:#ecfdf3;color:#166534;border-color:#86efac}',
    'body.theme-dark .badge-info{background:#172554;color:#bfdbfe;border-color:#1d4ed8}',
    'body.theme-dark .badge-success{background:#052e16;color:#86efac;border-color:#166534}',
    '.error{color:#b91c1c;margin-top:8px;font-weight:600}',
    'table{width:100%;border-collapse:separate;border-spacing:0;background:var(--surface);border:1px solid var(--border);border-radius:14px;overflow:hidden;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    'th,td{padding:12px;border-bottom:1px solid var(--border);text-align:left}',
    'th{background:var(--surface-muted);font-size:12px;text-transform:uppercase;letter-spacing:.04em;color:var(--text-soft)}',
    '.sort-btn{all:unset;cursor:pointer;font-weight:700;color:var(--text-soft)}',
    'tbody tr:hover{background:#f8fbff}',
    'tbody tr:last-child td{border-bottom:none}',
    '.pagination{display:flex;justify-content:flex-end;align-items:center;gap:10px;margin-top:10px}',
    '.pagination span{color:var(--text-soft);font-weight:600}',
    '@media (max-width: 820px){.grid{grid-template-columns:1fr 1fr}.page{padding:8px 12px 14px}}',
    '@media (max-width: 620px){.grid{grid-template-columns:1fr}.filters{flex-direction:column;align-items:stretch}}'
  ]
})
export class SalesComponent implements OnInit {
  books: Book[] = [];
  clients: Client[] = [];
  sales: Sale[] = [];
  isLoadingSales = false;
  page = 1;
  readonly pageSize = 8;
  sortField: 'soldAt' | 'clientName' | 'bookTitle' | 'quantity' = 'soldAt';
  sortDirection: 'asc' | 'desc' = 'desc';
  historyClientId = 0;
  error: string | null = null;

  saleForm: SaleRequest = {
    clientId: 0,
    bookId: 0,
    quantity: 1
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly uiFeedback: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.reloadReferences();
    this.loadSales();
  }

  get sortedSales(): Sale[] {
    const sales = [...this.sales];
    sales.sort((left, right) => {
      let result = 0;
      if (this.sortField === 'quantity') {
        result = left.quantity - right.quantity;
      } else if (this.sortField === 'soldAt') {
        result = new Date(left.soldAt).getTime() - new Date(right.soldAt).getTime();
      } else {
        result = String(left[this.sortField]).localeCompare(String(right[this.sortField]), 'es', { sensitivity: 'base' });
      }
      return this.sortDirection === 'asc' ? result : -result;
    });
    return sales;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sortedSales.length / this.pageSize));
  }

  get pagedSales(): Sale[] {
    const start = (this.page - 1) * this.pageSize;
    return this.sortedSales.slice(start, start + this.pageSize);
  }

  setSort(field: 'soldAt' | 'clientName' | 'bookTitle' | 'quantity'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = field === 'soldAt' ? 'desc' : 'asc';
    }
    this.page = 1;
  }

  sortMark(field: 'soldAt' | 'clientName' | 'bookTitle' | 'quantity'): string {
    if (this.sortField !== field) {
      return '↕';
    }
    return this.sortDirection === 'asc' ? '↑' : '↓';
  }

  changePage(delta: number): void {
    const next = this.page + delta;
    this.page = Math.min(this.totalPages, Math.max(1, next));
  }

  reloadReferences(): void {
    this.apiService.getBooks().subscribe((response) => {
      this.books = response.content ?? [];
    });
    this.apiService.getClients().subscribe((response) => {
      this.clients = response.content ?? [];
    });
  }

  loadSales(): void {
    const source = this.historyClientId > 0
      ? this.apiService.getClientSales(this.historyClientId)
      : this.apiService.getSales();

    this.isLoadingSales = true;
    source
      .pipe(finalize(() => (this.isLoadingSales = false)))
      .subscribe((response) => {
        this.sales = response.content ?? [];
        this.page = 1;
      });
  }

  createSale(): void {
    this.error = null;
    const request: SaleRequest = {
      clientId: Number(this.saleForm.clientId),
      bookId: Number(this.saleForm.bookId),
      quantity: Number(this.saleForm.quantity)
    };

    if (!request.clientId || !request.bookId || request.quantity < 1) {
      this.error = 'Cliente, libro y cantidad válidos son obligatorios.';
      return;
    }

    this.apiService.createSale(request).subscribe({
      next: () => {
        this.uiFeedback.showSuccess('Venta registrada correctamente.');
        this.saleForm = { clientId: 0, bookId: 0, quantity: 1 };
        this.reloadReferences();
        this.loadSales();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo registrar la venta.';
        this.uiFeedback.showError(this.error ?? 'No se pudo registrar la venta.');
      }
    });
  }

}
