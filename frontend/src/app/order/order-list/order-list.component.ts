import { Component, OnInit } from '@angular/core';
import { OrderService, OrderSummaryDTO } from '../order.service';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html'
})
export class OrderListComponent implements OnInit {
  orders: OrderSummaryDTO[] = [];
  loading = true;
  currentPage = 0;
  totalPages = 0;

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.orderService.getOrders(this.currentPage).subscribe(res => {
      this.orders = res.content;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }
}
