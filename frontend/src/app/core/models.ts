export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface PageResponse<T> {
  content: T[];
}

export interface Author {
  id: number;
  name: string;
  nationality?: string;
}

export interface AuthorRequest {
  name: string;
  nationality?: string;
}

export interface Book {
  id: number;
  title: string;
  isbn: string;
  publicationYear: number;
  stock: number;
  authorId: number;
  authorName: string;
  subtitle?: string;
  description?: string;
  publisher?: string;
  category?: string;
  coverUrl?: string;
  pageCount?: number;
}

export interface BookRequest {
  title: string;
  isbn: string;
  publicationYear: number;
  stock: number;
  authorId: number;
}

export interface GoogleBookMetadata {
  title: string;
  subtitle?: string;
  description?: string;
  publisher?: string;
  category?: string;
  coverUrl?: string;
  publicationYear?: number;
  pageCount?: number;
  authorName?: string;
}

export interface Client {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface ClientRequest {
  firstName: string;
  lastName: string;
  email: string;
}

export interface Sale {
  id: number;
  clientId: number;
  clientName: string;
  bookId: number;
  bookTitle: string;
  bookIsbn: string;
  quantity: number;
  soldAt: string;
}

export interface SaleRequest {
  clientId: number;
  bookId: number;
  quantity: number;
}
