import { Component, OnInit } from '@angular/core';
import { CartService, CartDTO, CartItemDTO } from '../cart.service';

@Component({
  selector: 'app-cart-page',
  templateUrl: './cart-page.component.html'
})
export class CartPageComponent implements OnInit {
  cart?: CartDTO;
  loading = true;

  constructor(private cartService: CartService) {}

  ngOnInit(): void {
    this.loadCart();
  }

  loadCart(): void {
    this.loading = true;
    this.cartService.getCart().subscribe({
      next: (c) => {
        this.cart = c;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  increaseQuantity(item: CartItemDTO): void {
    this.cartService.updateItem(item.id, item.quantity + 1).subscribe(c => this.cart = c);
  }

  decreaseQuantity(item: CartItemDTO): void {
    if (item.quantity <= 1) return;
    this.cartService.updateItem(item.id, item.quantity - 1).subscribe(c => this.cart = c);
  }

  onQuantityChange(item: CartItemDTO, value: string): void {
    const qty = parseInt(value, 10);
    if (qty >= 1) {
      this.cartService.updateItem(item.id, qty).subscribe(c => this.cart = c);
    }
  }

  removeItem(item: CartItemDTO): void {
    this.cartService.removeItem(item.id).subscribe(c => this.cart = c);
  }

  clearCart(): void {
    this.cartService.clearCart().subscribe(c => this.cart = c);
  }
}
