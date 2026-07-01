import { Component, OnInit } from '@angular/core';
import { CartService, CartItemDTO } from '../../cart/cart.service';
import { OrderService } from '../../order/order.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-checkout',
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent implements OnInit {
  cartItems: CartItemDTO[] = [];
  cartTotal = 0;
  shippingAddress = '';
  loading = false;
  errorMessage = '';
  step: 'form' | 'confirmation' = 'form';
  orderId = 0;
  transactionId = '';

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.cartService.getCart().subscribe(cart => {
      if (cart.isEmpty) {
        this.router.navigate(['/cart']);
        return;
      }
      this.cartItems = cart.items;
      this.cartTotal = cart.total;
    });
  }

  createAndPay(): void {
    if (!this.shippingAddress) return;
    this.loading = true;
    this.errorMessage = '';

    this.orderService.createOrder(this.shippingAddress).subscribe({
      next: (order) => {
        this.orderService.pay(order.id).subscribe({
          next: (payment) => {
            this.orderId = payment.orderId;
            this.transactionId = payment.transactionId;
            this.step = 'confirmation';
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = err.error?.error || 'Erreur de paiement';
            this.loading = false;
          }
        });
      },
      error: (err) => {
        this.errorMessage = err.error?.error || 'Erreur de creation de commande';
        this.loading = false;
      }
    });
  }
}
