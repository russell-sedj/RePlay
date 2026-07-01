import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CartItemDTO {
  id: number;
  productId: number;
  productName: string;
  productSlug: string;
  imageUrl: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
}

export interface CartDTO {
  id: number;
  items: CartItemDTO[];
  total: number;
  isEmpty: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class CartService {

  constructor(private http: HttpClient) {}

  getCart(): Observable<CartDTO> {
    return this.http.get<CartDTO>('/api/cart');
  }

  addItem(productId: number, quantity: number): Observable<CartDTO> {
    return this.http.post<CartDTO>('/api/cart/items', { productId, quantity });
  }

  updateItem(itemId: number, quantity: number): Observable<CartDTO> {
    return this.http.put<CartDTO>(`/api/cart/items/${itemId}`, { quantity });
  }

  removeItem(itemId: number): Observable<CartDTO> {
    return this.http.delete<CartDTO>(`/api/cart/items/${itemId}`);
  }

  clearCart(): Observable<CartDTO> {
    return this.http.delete<CartDTO>('/api/cart');
  }
}
