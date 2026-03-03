import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Book, Client, Sale, SaleRequest } from '../core/models';

@Component({
  selector: 'app-sales',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <main class="page">
      <header>
        <h1>Ventas</h1>
        <nav>
          <a routerLink="/books">Libros</a>
          <a routerLink="/authors">Autores</a>
          <a routerLink="/clients">Clientes</a>
          <a routerLink="/sales">Ventas</a>
          <button (click)="logout()">Salir</button>
        </nav>
      </header>

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

      <table *ngIf="sales.length; else empty">
        <thead>
          <tr>
            <th>Fecha</th>
            <th>Cliente</th>
            <th>Libro</th>
            <th>ISBN</th>
            <th>Cantidad</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let sale of sales">
            <td>{{ sale.soldAt | date:'yyyy-MM-dd HH:mm' }}</td>
            <td>{{ sale.clientName }}</td>
            <td>{{ sale.bookTitle }}</td>
            <td>{{ sale.bookIsbn }}</td>
            <td>{{ sale.quantity }}</td>
          </tr>
        </tbody>
      </table>

      <ng-template #empty>
        <p>No hay ventas registradas.</p>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:20px;max-width:1100px;margin:0 auto}',
    'header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}',
    'nav{display:flex;gap:10px;align-items:center}',
    '.panel{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:12px;margin-bottom:16px}',
    '.panel h2{margin:0 0 10px;font-size:16px}',
    '.grid{display:grid;grid-template-columns:repeat(3,minmax(140px,1fr));gap:10px;align-items:center}',
    '.grid .actions{grid-column:1 / -1}',
    '.filters{display:flex;gap:10px;align-items:center}',
    'input,select{padding:8px;border:1px solid #cbd5e1;border-radius:6px}',
    'a,button{padding:8px 10px;border:1px solid #ccc;border-radius:6px;text-decoration:none;background:#fff;color:#111}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.error{color:#b91c1c;margin-top:8px}',
    'table{width:100%;border-collapse:collapse;background:#fff}',
    'th,td{padding:10px;border:1px solid #e5e7eb;text-align:left}',
    'th{background:#f8fafc}'
  ]
})
export class SalesComponent implements OnInit {
  books: Book[] = [];
  clients: Client[] = [];
  sales: Sale[] = [];
  historyClientId = 0;
  error: string | null = null;

  saleForm: SaleRequest = {
    clientId: 0,
    bookId: 0,
    quantity: 1
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.reloadReferences();
    this.loadSales();
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

    source.subscribe((response) => {
      this.sales = response.content ?? [];
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
        this.saleForm = { clientId: 0, bookId: 0, quantity: 1 };
        this.reloadReferences();
        this.loadSales();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo registrar la venta.';
      }
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
