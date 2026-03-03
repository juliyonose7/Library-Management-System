import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Client, ClientRequest } from '../core/models';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  template: `
    <main class="page">
      <header>
        <h1>Clientes</h1>
        <nav>
          <a routerLink="/books">Libros</a>
          <a routerLink="/authors">Autores</a>
          <a routerLink="/clients">Clientes</a>
          <a routerLink="/sales">Ventas</a>
          <button (click)="logout()">Salir</button>
        </nav>
      </header>

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
          name="searchText"
          class="search"
        />
      </section>

      <table *ngIf="filteredClients.length; else empty">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>Email</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let client of filteredClients">
            <td>{{ client.firstName }}</td>
            <td>{{ client.lastName }}</td>
            <td>{{ client.email }}</td>
            <td class="actions">
              <button type="button" (click)="editClient(client)">Editar</button>
              <button type="button" class="danger" (click)="removeClient(client)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>

      <ng-template #empty>
        <p>No hay clientes disponibles.</p>
      </ng-template>
    </main>
  `,
  styles: [
    '.page{padding:20px;max-width:1000px;margin:0 auto}',
    'header{display:flex;justify-content:space-between;align-items:center;margin-bottom:16px}',
    'nav{display:flex;gap:10px;align-items:center}',
    '.panel{background:#fff;border:1px solid #e5e7eb;border-radius:8px;padding:12px;margin-bottom:16px}',
    '.panel h2{margin:0 0 10px;font-size:16px}',
    '.grid{display:grid;grid-template-columns:repeat(3,minmax(120px,1fr));gap:10px;align-items:center}',
    '.grid .actions{grid-column:1 / -1}',
    '.search{width:100%}',
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
export class ClientsComponent implements OnInit {
  clients: Client[] = [];
  searchText = '';
  editingClientId: number | null = null;
  error: string | null = null;

  clientForm: ClientRequest = {
    firstName: '',
    lastName: '',
    email: ''
  };

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router
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

  loadClients(): void {
    this.apiService.getClients().subscribe((response) => {
      this.clients = response.content ?? [];
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
        this.resetForm();
        this.loadClients();
      },
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo guardar el cliente.';
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

  removeClient(client: Client): void {
    if (!confirm(`¿Eliminar cliente ${client.firstName} ${client.lastName}?`)) {
      return;
    }

    this.apiService.deleteClient(client.id).subscribe({
      next: () => this.loadClients(),
      error: (err) => {
        this.error = err?.error?.detail ?? 'No se pudo eliminar el cliente.';
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

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
