import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login.component';
import { BooksComponent } from './pages/books.component';
import { ClientsComponent } from './pages/clients.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{ path: 'books', component: BooksComponent, canActivate: [authGuard] },
	{ path: 'clients', component: ClientsComponent, canActivate: [authGuard] },
	{ path: '', pathMatch: 'full', redirectTo: 'books' },
	{ path: '**', redirectTo: 'books' }
];
