import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Category, CategoryService } from '../../services/category.service';
import { ApiResponse } from '../../services/sale.service';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './categories.component.html',
  styleUrls: ['./categories.component.scss']
})
export class CategoriesComponent implements OnInit {
  categories: Category[] = [];
  selectedCategory: Partial<Category> = {};
  showForm = false;

  constructor(private categoryService: CategoryService) { }

  ngOnInit(): void {
    this.loadCategories();
  }

  loadCategories(): void {
    this.categoryService.getAll().subscribe((response: ApiResponse<Category[]>) => {
      this.categories = response.data;
    });
  }

  showAddForm(): void {
    this.selectedCategory = {};
    this.showForm = true;
  }

  selectCategory(category: Category): void {
    this.selectedCategory = { ...category };
    this.showForm = true;
  }

  resetForm(): void {
    this.selectedCategory = {};
    this.showForm = false;
  }

  saveCategory(): void {
    if (this.selectedCategory.name) {
      if (this.selectedCategory.id) {
        // Update existing category
        this.categoryService.update(this.selectedCategory.id, { name: this.selectedCategory.name })
          .subscribe(() => {
            this.loadCategories();
            this.resetForm();
          });
      } else {
        // Create new category
        this.categoryService.create({ name: this.selectedCategory.name })
          .subscribe(() => {
            this.loadCategories();
            this.resetForm();
          });
      }
    }
  }

  deleteCategory(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa danh mục này không?')) {
      this.categoryService.delete(id).subscribe(() => {
        this.loadCategories();
      });
    }
  }
}