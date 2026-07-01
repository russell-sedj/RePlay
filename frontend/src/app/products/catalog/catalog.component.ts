import { Component, OnInit } from '@angular/core';
import { ProductService, ProductSummary, Category } from '../product.service';

@Component({
  selector: 'app-catalog',
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  products: ProductSummary[] = [];
  categories: Category[] = [];
  consoles = ['NES', 'SNES', 'NINTENDO_64', 'GAMECUBE', 'WII', 'SWITCH', 'GAMEBOY', 'GAMEBOY_ADVANCE', 'PLAYSTATION', 'PS2', 'PS3', 'PS4', 'PS5', 'XBOX', 'SEGA_MEGA_DRIVE', 'AUTRE'];

  currentPage = 0;
  totalPages = 0;
  sort = 'name,asc';
  loading = false;

  filters: any = {};

  constructor(private productService: ProductService) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadProducts();
  }

  loadCategories(): void {
    this.productService.getCategories().subscribe(cats => this.categories = cats);
  }

  loadProducts(): void {
    this.loading = true;
    this.productService.getProducts({
      page: this.currentPage,
      size: 20,
      sort: this.sort,
      ...this.filters
    }).subscribe(res => {
      this.products = res.content;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  onFilterChange(key: string, value: string): void {
    this.filters[key] = value;
    this.currentPage = 0;
    this.loadProducts();
  }

  onSortChange(sort: string): void {
    this.sort = sort;
    this.loadProducts();
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  resetFilters(): void {
    this.filters = {};
    this.currentPage = 0;
    this.loadProducts();
  }
}
