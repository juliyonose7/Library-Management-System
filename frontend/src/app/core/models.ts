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

export interface Book {
  id: number;
  title: string;
  isbn: string;
  publicationYear: number;
  stock: number;
  authorId: number;
  authorName: string;
}

export interface Client {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
}
