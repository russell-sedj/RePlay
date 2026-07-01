import { Component, OnInit } from '@angular/core';
import { AdminService, AdminProductRequest } from '../admin.service';

const CONSOLE_TYPES = ['NES', 'SNES', 'NINTENDO_64', 'GAMECUBE', 'WII', 'WII_U', 'NINTENDO_SWITCH', 'GAME_BOY', 'GAME_BOY_COLOR', 'GAME_BOY_ADVANCE', 'NINTENDO_DS', 'NINTENDO_3DS', 'PLAYSTATION', 'PLAYSTATION_2', 'PLAYSTATION_3', 'PLAYSTATION_4', 'PLAYSTATION_5', 'PSP', 'PS_VITA', 'XBOX', 'XBOX_360', 'XBOX_ONE', 'XBOX_SERIES', 'SEGA_MEGA_DRIVE', 'SEGA_SATURN', 'SEGA_DREAMCAST', 'ATARI_2600', 'ATARI_7800', 'AUTRE'];

@Component({
  selector: 'app-product-list',
  templateUrl: './product-list.component.html'
})
export class ProductListComponent implements OnInit {
  products: any[] = [];
  categories: any[] = [];
  loading = true;
  saving = false;
  error = '';
  currentPage = 0;
  totalPages = 0;
  consoleTypes = CONSOLE_TYPES;

  showModal = false;
  editId: number | null = null;
  form: AdminProductRequest = this.defaultForm();

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadProducts();
    this.adminService.getCategories().subscribe(c => this.categories = c);
  }

  defaultForm(): AdminProductRequest {
    return { name: '', slug: '', description: '', price: 0, condition: 'NEUF', consoleType: 'NES', stockQuantity: 1, imageUrl: '', categoryId: 0, archived: false };
  }

  loadProducts(): void {
    this.loading = true;
    this.adminService.getProducts({ page: this.currentPage, size: 20 }).subscribe(res => {
      this.products = res.content;
      this.totalPages = res.totalPages;
      this.loading = false;
    });
  }

  changePage(page: number): void {
    this.currentPage = page;
    this.loadProducts();
  }

  openModal(product?: any): void {
    if (product) {
      this.editId = product.id;
      this.form = {
        name: product.name, slug: product.slug, description: product.description || '',
        price: product.price, condition: product.condition, consoleType: product.consoleType,
        stockQuantity: product.stockQuantity, imageUrl: product.imageUrl || '',
        categoryId: product.categoryId || 0, archived: product.archived
      };
    } else {
      this.editId = null;
      this.form = this.defaultForm();
    }
    this.error = '';
    this.showModal = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const obs = this.editId
      ? this.adminService.updateProduct(this.editId, this.form)
      : this.adminService.createProduct(this.form);

    obs.subscribe({
      next: () => {
        this.showModal = false;
        this.saving = false;
        this.loadProducts();
      },
      error: (err) => {
        this.error = err.error?.error || 'Erreur lors de la sauvegarde';
        this.saving = false;
      }
    });
  }
}
