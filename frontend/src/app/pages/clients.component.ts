import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Client, ClientRequest } from '../core/models';
import { UiFeedbackService } from '../core/ui-feedback.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <main class="page">
      <h1>Clientes</h1>

      <section class="panel">
        <h2>{{ editingClientId ? 'Editar cliente' : 'Nuevo cliente' }}</h2>
        <form class="grid" (ngSubmit)="saveClient()">
          <input type="text" placeholder="Nombre" [(ngModel)]="clientForm.firstName" name="firstName" required maxlength="120" />
          <input type="text" placeholder="Apellido" [(ngModel)]="clientForm.lastName" name="lastName" required maxlength="120" />
          <input type="email" placeholder="Email" [(ngModel)]="clientForm.email" name="email" required maxlength="180" />
          <div class="actions">
            <button type="submit">{{ editingClientId ? 'Guardar cambios' : 'Crear cliente' }}</button>
            <button type="button" (click)="resetForm()" *ngIf="editingClientId">Cancelar</button>
          </div>
        </form>
        <p *ngIf="error" class="error">{{ error }}</p>
      </section>

      <section class="panel">
        <input
          type="text"
          placeholder="Buscar por nombre o email"
          [(ngModel)]="searchText"
          (ngModelChange)="onSearchChanged()"
          name="searchText"
          class="search"
        />
      </section>

      <table *ngIf="sortedClients.length && !isLoadingClients; else empty">
        <thead>
          <tr>
            <th><button type="button" class="sort-btn" (click)="setSort('firstName')">Nombre {{ sortMark('firstName') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('lastName')">Apellido {{ sortMark('lastName') }}</button></th>
            <th><button type="button" class="sort-btn" (click)="setSort('email')">Email {{ sortMark('email') }}</button></th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let client of pagedClients">
            <td>{{ client.firstName }}</td>
            <td>{{ client.lastName }}</td>
            <td>
              <span class="badge badge-info">{{ client.email }}</span>
            </td>
            <td class="actions">
              <button type="button" class="action-btn" (click)="editClient(client)">✏️ Editar</button>
              <button type="button" class="danger action-btn" (click)="removeClient(client)">🗑️ Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <section class="pagination" *ngIf="totalPages > 1 && !isLoadingClients">
        <button type="button" (click)="changePage(-1)" [disabled]="page === 1">← Anterior</button>
        <span>Página {{ page }} de {{ totalPages }}</span>
        <button type="button" (click)="changePage(1)" [disabled]="page === totalPages">Siguiente →</button>
      </section>

      <ng-template #empty>
        <section class="skeleton-card" *ngIf="isLoadingClients">
          <div class="skeleton-line w-35"></div>
          <div class="skeleton-line w-85"></div>
          <div class="skeleton-line w-70"></div>
        </section>
        <section class="empty-state" *ngIf="!isLoadingClients">
          <h3>No hay clientes disponibles</h3>
          <p>Agrega clientes para registrar ventas y construir historial de compras.</p>
        </section>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:8px 20px 20px;max-width:1100px;margin:0 auto}',
    'h1{margin:0 0 14px;font-size:30px;letter-spacing:-.3px}',
    '.panel{background:rgba(255,255,255,.92);backdrop-filter:blur(6px);border:1px solid var(--border);border-radius:14px;padding:16px;margin-bottom:16px;box-shadow:0 10px 30px rgba(15,23,42,.06)}',
    '.panel h2{margin:0 0 12px;font-size:18px}',
    '.grid{display:grid;grid-template-columns:repeat(3,minmax(120px,1fr));gap:10px;align-items:center}',
    '.grid .actions{grid-column:1 / -1}',
    '.search{width:100%}',
    'input{padding:10px;border:1px solid #cbd5e1;border-radius:10px;outline:none;transition:border-color .2s, box-shadow .2s}',
    'input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(37,99,235,.15)}',
    'button{padding:9px 12px;border:1px solid var(--border);border-radius:10px;background:#fff;color:var(--text);font-weight:600;transition:all .2s;cursor:pointer}',
    'button[type="submit"]{background:linear-gradient(135deg,var(--primary),var(--primary-strong));color:#fff;border-color:transparent}',
    'button:hover{border-color:#94a3b8;transform:translateY(-1px)}',
    '.actions{display:flex;gap:8px;align-items:center}',
    '.action-btn{min-width:88px;justify-content:center}',
    '.badge{display:inline-flex;align-items:center;padding:4px 9px;border-radius:999px;font-size:12px;font-weight:700;border:1px solid transparent}',
    '.badge-info{background:#eff6ff;color:#1d4ed8;border-color:#bfdbfe;max-width:280px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}',
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
    '@media (max-width: 820px){.grid{grid-template-columns:1fr 1fr}.page{padding:8px 12px 14px}}',
    '@media (max-width: 620px){.grid{grid-template-columns:1fr}}'
  ]
})
export class ClientsComponent implements OnInit {
  clients: Client[] = [];
  isLoadingClients = false;
  searchText = '';
  page = 1;
  readonly pageSize = 8;
  sortField: 'firstName' | 'lastName' | 'email' = 'firstName';
  sortDirection: 'asc' | 'desc' = 'asc';
  editingClientId: number | null = null;
  error: string | null = null;

  clientForm: ClientRequest = {
    firstName: '',
    lastName: '',
    email: ''
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly uiFeedback: UiFeedbackService
  ) {}

  ngOnInit(): void {
    this.loadClients();
  }

  get filteredClients(): Client[] {
    const term = this.searchText.trim().toLowerCase();
    if (!term) {
      return this.clients;
    }

    return this.clients.filter((client) =>
      client.firstName.toLowerCase().includes(term) ||
      client.lastName.toLowerCase().includes(term) ||
      client.email.toLowerCase().includes(term)
    );
  }

  get sortedClients(): Client[] {
    const clients = [...this.filteredClients];
    clients.sort((left, right) => {
      const leftValue = left[this.sortField];
      const rightValue = right[this.sortField];
      const result = String(leftValue).localeCompare(String(rightValue), 'es', { sensitivity: 'base' });
      return this.sortDirection === 'asc' ? result : -result;
    });
    return clients;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.sortedClients.length / this.pageSize));
  }

  get pagedClients(): Client[] {
    const start = (this.page - 1) * this.pageSize;
    return this.sortedClients.slice(start, start + this.pageSize);
  }

  setSort(field: 'firstName' | 'lastName' | 'email'): void {
    if (this.sortField === field) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortField = field;
      this.sortDirection = 'asc';
    }
    this.page = 1;
  }

  sortMark(field: 'firstName' | 'lastName' | 'email'): string {
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

  loadClients(): void {
    this.isLoadingClients = true;
    this.apiService.getClients()
      .pipe(finalize(() => (this.isLoadingClients = false)))
      .subscribe((response) => {
        this.clients = response.content ?? [];
        this.page = 1;
      });
  }

  saveClient(): void {
    this.error = null;

    const request: ClientRequest = {
      firstName: this.clientForm.firstName.trim(),
      lastName: this.clientForm.lastName.trim(),
      email: this.clientForm.email.trim()
    };

    if (!request.firstName || !request.lastName || !request.email) {
      this.error = 'Nombre, apellido y email son obligatorios.';
      return;
    }

    const operation = this.editingClientId
      ? this.apiService.updateClient(this.editingClientId, request)
      : this.apiService.createClient(request);

    operation.subscribe({
      next: () => {
        this.uiFeedback.showSuccess(this.editingClientId ? 'Cliente actualizado.' : 'Cliente creado.');
        this.resetForm();
        this.loadClients();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo guardar el cliente.';
        this.uiFeedback.showError(this.error ?? 'No se pudo guardar el cliente.');
      }
    });
  }

  editClient(client: Client): void {
    this.editingClientId = client.id;
    this.clientForm = {
      firstName: client.firstName,
      lastName: client.lastName,
      email: client.email
    };
  }

  async removeClient(client: Client): Promise<void> {
    const confirmed = await this.uiFeedback.confirm({
      title: 'Eliminar cliente',
      message: `¿Eliminar cliente ${client.firstName} ${client.lastName}?`,
      confirmText: 'Eliminar',
      cancelText: 'Cancelar',
      danger: true
    });

    if (!confirmed) {
      return;
    }

    this.apiService.deleteClient(client.id).subscribe({
      next: () => {
        this.uiFeedback.showSuccess('Cliente eliminado.');
        this.loadClients();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo eliminar el cliente.';
        this.uiFeedback.showError(this.error ?? 'No se pudo eliminar el cliente.');
      }
    });
  }

  resetForm(): void {
    this.editingClientId = null;
    this.clientForm = {
      firstName: '',
      lastName: '',
      email: ''
    };
  }

}
