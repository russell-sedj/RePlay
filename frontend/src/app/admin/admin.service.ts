import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface StatsDTO {
  totalUsers: number;
  totalOrders: number;
  totalRevenue: number;
  totalProducts: number;
  topProducts: { name: string; slug: string; totalSold: number }[];
}

export interface OrderAdminDTO {
  id: number;
  userId: number;
  userEmail: string;
  orderDate: string;
  status: string;
  totalAmount: number;
  transactionId: string | null;
  shippingAddress: string;
}

export interface UserAdminDTO {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface AdminProductRequest {
  name: string;
  slug: string;
  description: string;
  price: number;
  condition: string;
  consoleType: string;
  stockQuantity: number;
  imageUrl: string;
  categoryId: number;
  archived: boolean;
}

export interface AdminCategoryRequest {
  name: string;
  slug: string;
  description: string;
  imageUrl: string;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  constructor(private http: HttpClient) {}

  getStats(): Observable<StatsDTO> {
    return this.http.get<StatsDTO>('/api/admin/stats');
  }

  getProducts(params: any): Observable<PageResponse<any>> {
    let p = new HttpParams();
    Object.entries(params).forEach(([k, v]) => { if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v)); });
    return this.http.get<PageResponse<any>>('/api/admin/products', { params: p });
  }

  createProduct(data: AdminProductRequest): Observable<any> {
    return this.http.post('/api/admin/products', data);
  }

  updateProduct(id: number, data: AdminProductRequest): Observable<any> {
    return this.http.put(`/api/admin/products/${id}`, data);
  }

  getCategories(params?: any): Observable<any[]> {
    return this.http.get<any[]>('/api/admin/categories');
  }

  createCategory(data: AdminCategoryRequest): Observable<any> {
    return this.http.post('/api/admin/categories', data);
  }

  updateCategory(id: number, data: AdminCategoryRequest): Observable<any> {
    return this.http.put(`/api/admin/categories/${id}`, data);
  }

  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`/api/admin/categories/${id}`);
  }

  getOrders(status?: string, page: number = 0, size: number = 20): Observable<PageResponse<OrderAdminDTO>> {
    let p = new HttpParams().set('page', page).set('size', size);
    if (status) p = p.set('status', status);
    return this.http.get<PageResponse<OrderAdminDTO>>('/api/admin/orders', { params: p });
  }

  updateOrderStatus(id: number, status: string): Observable<OrderAdminDTO> {
    return this.http.put<OrderAdminDTO>(`/api/admin/orders/${id}/status`, { status });
  }

  getUsers(page: number = 0, size: number = 20): Observable<PageResponse<UserAdminDTO>> {
    const p = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<UserAdminDTO>>('/api/admin/users', { params: p });
  }
}
