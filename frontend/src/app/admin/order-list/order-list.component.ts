import { Component, OnInit } from '@angular/core';
import { AdminService, OrderAdminDTO } from '../admin.service';

@Component({
  selector: 'app-order-list',
  templateUrl: './order-list.component.html'
})
export class OrderListComponent implements OnInit {
  orders: OrderAdminDTO[] = [];
  loading = true;
  currentPage = 0;
  totalPages = 0;
  statusFilter = '';

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.adminService.getOrders(this.statusFilter || undefined, this.currentPage).subscribe(res => {
      this.orders = res.content;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadOrders();
  }

  updateStatus(orderId: number, event: any): void {
    const newStatus = event.target.value;
    this.adminService.updateOrderStatus(orderId, newStatus).subscribe(() => this.loadOrders());
  }
}
