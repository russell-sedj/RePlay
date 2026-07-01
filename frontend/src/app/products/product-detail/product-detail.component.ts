import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ProductService, ProductDetail } from '../product.service';
import { AuthService } from '../../core/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-product-detail',
  templateUrl: './product-detail.component.html'
})
export class ProductDetailComponent implements OnInit {
  product?: ProductDetail;
  loading = true;
  quantity = 1;
  addedToCart = false;

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const slug = this.route.snapshot.paramMap.get('slug');
    if (slug) {
      this.productService.getProductBySlug(slug).subscribe({
        next: (p) => {
          this.product = p;
          this.loading = false;
        },
        error: () => this.loading = false
      });
    }
  }

  addToCart(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login'], { queryParams: { returnUrl: this.router.url } });
      return;
    }
    this.addedToCart = true;
    setTimeout(() => this.addedToCart = false, 2000);
  }
}
