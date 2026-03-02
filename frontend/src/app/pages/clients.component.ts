import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { Client } from '../core/models';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-clients',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <main class="page">
      <header>
        <h1>Clientes</h1>
        <nav>
          <a routerLink="/books">Libros</a>
          <a routerLink="/clients">Clientes</a>
          <button (click)="logout()">Salir</button>
        </nav>
      </header>

      <table *ngIf="clients.length; else empty">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Apellido</th>
            <th>Email</th>
          </tr>
        </thead>
        <tbody>
          <tr *ngFor="let client of clients">
            <td>{{ client.firstName }}</td>
            <td>{{ client.lastName }}</td>
            <td>{{ client.email }}</td>
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
    'a,button{padding:8px 10px;border:1px solid #ccc;border-radius:6px;text-decoration:none;background:#fff;color:#111}',
    'table{width:100%;border-collapse:collapse;background:#fff}',
    'th,td{padding:10px;border:1px solid #e5e7eb;text-align:left}',
    'th{background:#f8fafc}'
  ]
})
export class ClientsComponent implements OnInit {
  clients: Client[] = [];

  constructor(
    private readonly apiService: ApiService,
    private readonly authService: AuthService,
    private readonly router: Router
  ) {}

  ngOnInit(): void {
    this.apiService.getClients().subscribe((response) => {
      this.clients = response.content ?? [];
    });
  }

  logout(): void {
    this.authService.logout();
    this.router.navigateByUrl('/login');
  }
}
