import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  Author,
  AuthorRequest,
  Book,
  BookRequest,
  Client,
  ClientRequest,
  GoogleBookMetadata,
  PageResponse,
  Sale,
  SaleRequest
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiBase = 'http://localhost:8080/api/v1';

  constructor(private readonly http: HttpClient) {}

  getAuthors(): Observable<PageResponse<Author>> {
    return this.http.get<PageResponse<Author>>(`${this.apiBase}/authors`);
  }

  createAuthor(request: AuthorRequest): Observable<Author> {
    return this.http.post<Author>(`${this.apiBase}/authors`, request);
  }

  updateAuthor(id: number, request: AuthorRequest): Observable<Author> {
    return this.http.put<Author>(`${this.apiBase}/authors/${id}`, request);
  }

  deleteAuthor(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/authors/${id}`);
  }

  getBooks(): Observable<PageResponse<Book>> {
    return this.http.get<PageResponse<Book>>(`${this.apiBase}/books`);
  }

  createBook(request: BookRequest): Observable<Book> {
    return this.http.post<Book>(`${this.apiBase}/books`, request);
  }

  updateBook(id: number, request: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.apiBase}/books/${id}`, request);
  }

  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/books/${id}`);
  }

  getClients(): Observable<PageResponse<Client>> {
    return this.http.get<PageResponse<Client>>(`${this.apiBase}/clients`);
  }

  createClient(request: ClientRequest): Observable<Client> {
    return this.http.post<Client>(`${this.apiBase}/clients`, request);
  }

  updateClient(id: number, request: ClientRequest): Observable<Client> {
    return this.http.put<Client>(`${this.apiBase}/clients/${id}`, request);
  }

  deleteClient(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiBase}/clients/${id}`);
  }

  getSales(): Observable<PageResponse<Sale>> {
    return this.http.get<PageResponse<Sale>>(`${this.apiBase}/sales`);
  }

  getClientSales(clientId: number): Observable<PageResponse<Sale>> {
    return this.http.get<PageResponse<Sale>>(`${this.apiBase}/clients/${clientId}/sales`);
  }

  createSale(request: SaleRequest): Observable<Sale> {
    return this.http.post<Sale>(`${this.apiBase}/sales`, request);
  }

  enrichBookByIsbn(isbn: string): Observable<GoogleBookMetadata> {
    return this.http.get<GoogleBookMetadata>(`${this.apiBase}/books/enrich`, {
      params: { isbn }
    });
  }
}
