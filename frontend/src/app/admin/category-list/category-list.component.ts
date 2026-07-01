import { Component, OnInit } from '@angular/core';
import { AdminService, AdminCategoryRequest } from '../admin.service';

@Component({
  selector: 'app-category-list',
  templateUrl: './category-list.component.html'
})
export class CategoryListComponent implements OnInit {
  categories: any[] = [];
  loading = true;
  saving = false;
  error = '';

  showModal = false;
  editId: number | null = null;
  form: AdminCategoryRequest = { name: '', slug: '', description: '', imageUrl: '' };

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.loading = true;
    this.adminService.getCategories().subscribe(c => {
      this.categories = c;
      this.loading = false;
    });
  }

  openModal(cat?: any): void {
    if (cat) {
      this.editId = cat.id;
      this.form = { name: cat.name, slug: cat.slug, description: cat.description || '', imageUrl: cat.imageUrl || '' };
    } else {
      this.editId = null;
      this.form = { name: '', slug: '', description: '', imageUrl: '' };
    }
    this.error = '';
    this.showModal = true;
  }

  save(): void {
    this.saving = true;
    this.error = '';
    const obs = this.editId
      ? this.adminService.updateCategory(this.editId, this.form)
      : this.adminService.createCategory(this.form);

    obs.subscribe({
      next: () => {
        this.showModal = false;
        this.saving = false;
        this.loadCategories();
      },
      error: (err) => {
        this.error = err.error?.error || 'Erreur lors de la sauvegarde';
        this.saving = false;
      }
    });
  }

  delete(id: number): void {
    if (!confirm('Supprimer cette categorie ?')) return;
    this.adminService.deleteCategory(id).subscribe(() => this.loadCategories());
  }
}
