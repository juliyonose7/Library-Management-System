import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { LoginComponent } from './pages/login.component';
import { BooksComponent } from './pages/books.component';
import { ClientsComponent } from './pages/clients.component';
import { AuthorsComponent } from './pages/authors.component';
import { SalesComponent } from './pages/sales.component';
import { AppShellComponent } from './layout/app-shell.component';

export const routes: Routes = [
	{ path: 'login', component: LoginComponent },
	{
		path: '',
		component: AppShellComponent,
		canActivate: [authGuard],
		children: [
			{ path: 'books', component: BooksComponent },
			{ path: 'authors', component: AuthorsComponent },
			{ path: 'clients', component: ClientsComponent },
			{ path: 'sales', component: SalesComponent },
			{ path: '', pathMatch: 'full', redirectTo: 'books' }
		]
	},
	{ path: '', pathMatch: 'full', redirectTo: 'books' },
	{ path: '**', redirectTo: 'books' }
];
