import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface OrderItemDTO {
  productId: number;
  productName: string;
  productSlug: string;
  imageUrl: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
}

export interface OrderSummaryDTO {
  id: number;
  orderDate: string;
  status: string;
  totalAmount: number;
  transactionId: string | null;
  itemCount: number;
}

export interface OrderDetailDTO {
  id: number;
  orderDate: string;
  status: string;
  totalAmount: number;
  transactionId: string | null;
  shippingAddress: string;
  items: OrderItemDTO[];
}

export interface PaymentResponse {
  success: boolean;
  transactionId: string;
  orderId: number;
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
export class OrderService {

  constructor(private http: HttpClient) {}

  createOrder(shippingAddress: string): Observable<OrderDetailDTO> {
    return this.http.post<OrderDetailDTO>('/api/orders', { shippingAddress });
  }

  getOrders(page: number = 0, size: number = 10): Observable<PageResponse<OrderSummaryDTO>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<PageResponse<OrderSummaryDTO>>('/api/orders', { params });
  }

  getOrder(id: number): Observable<OrderDetailDTO> {
    return this.http.get<OrderDetailDTO>(`/api/orders/${id}`);
  }

  pay(orderId: number, method: string = 'CARTE'): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>('/api/payments/pay', { orderId, method });
  }
}
