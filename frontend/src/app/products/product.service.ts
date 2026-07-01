import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Category {
  id: number;
  name: string;
  slug: string;
  description: string;
  imageUrl: string;
}

export interface ProductSummary {
  id: number;
  name: string;
  slug: string;
  price: number;
  condition: string;
  consoleType: string;
  imageUrl: string;
  stockQuantity: number;
  categoryName: string;
  categorySlug: string;
}

export interface ProductDetail {
  id: number;
  name: string;
  slug: string;
  description: string;
  price: number;
  condition: string;
  consoleType: string;
  stockQuantity: number;
  imageUrl: string;
  archived: boolean;
  createdAt: string;
  categoryId: number;
  categoryName: string;
  categorySlug: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

@Injectable({
  providedIn: 'root'
})
export class ProductService {

  constructor(private http: HttpClient) {}

  getProducts(params: {
    page?: number;
    size?: number;
    sort?: string;
    category?: string;
    console?: string;
    condition?: string;
    minPrice?: number;
    maxPrice?: number;
  }): Observable<PageResponse<ProductSummary>> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, value.toString());
      }
    });
    return this.http.get<PageResponse<ProductSummary>>('/api/products', { params: httpParams });
  }

  getProductBySlug(slug: string): Observable<ProductDetail> {
    return this.http.get<ProductDetail>(`/api/products/${slug}`);
  }

  getCategories(): Observable<Category[]> {
    return this.http.get<Category[]>('/api/categories');
  }
}
