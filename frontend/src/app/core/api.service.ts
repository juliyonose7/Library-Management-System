import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, Client, PageResponse } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly apiBase = 'http://localhost:8080/api/v1';

  constructor(private readonly http: HttpClient) {}

  getBooks(): Observable<PageResponse<Book>> {
    return this.http.get<PageResponse<Book>>(`${this.apiBase}/books`);
  }

  getClients(): Observable<PageResponse<Client>> {
    return this.http.get<PageResponse<Client>>(`${this.apiBase}/clients`);
  }
}
