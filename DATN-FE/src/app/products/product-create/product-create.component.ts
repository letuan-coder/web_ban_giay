import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ProductService, ProductCreateRequest } from '../../services/product.service';
import { Brand, BrandService } from '../../services/brand.service';
import { Category, CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-create.component.html',
  styleUrls: ['./product-create.component.scss']
})
export class ProductCreateComponent implements OnInit {
  brands: Brand[] = [];
  categories: Category[] = [];
  productsExisting: string[] = [];
  filteredProducts: string[] = [];

  product: ProductCreateRequest = {
    name: '',
    description: '',
    weight: 0,
    brandId: 0,
    categoryId: 0,
  };

  loading = false;
  message = '';

  constructor(
    private productService: ProductService,
    private brandService: BrandService,
    private categoryService: CategoryService
  ) { }

  ngOnInit(): void {
    this.loadBrands();
    this.loadCategories();
  }

  loadBrands() {
    this.brandService.getAll().subscribe((response: any) => {
      this.brands = response.data;
      if (this.brands.length > 0) {
        this.product.brandId = this.brands[0].id; // Select the first brand
      }
    });
  }

  loadCategories() {
    this.categoryService.getAll().subscribe((response: any) => {
      this.categories = response.data;
      if (this.categories.length > 0) {
        this.product.categoryId = this.categories[0].id; // Select the first category
      }
    });
  }

  onNameInput() {
    const input = this.product.name.toLowerCase();
    this.filteredProducts = this.productsExisting
      .filter(p => p.toLowerCase().includes(input));
  }

  selectSuggestion(name: string) {
    this.product.name = name;
    this.filteredProducts = [];
  }

  formatPrice(value: number | null): string {
    if (!value) return '';
    return value.toLocaleString('vi-VN'); 
  }

  createProduct() {
    if (!this.product.name || !this.product.brandId || !this.product.categoryId) {
      this.message = 'Vui lòng điền đầy đủ thông tin';
      return;
    }
    this.loading = true;
    this.message = '';
    this.productService.createProduct(this.product).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = 'Tạo sản phẩm thành công!';
        this.product = {
          weight: 0,
          name: '',
          description: '',
          brandId: this.brands.length > 0 ? this.brands[0].id : 0,
          categoryId: this.categories.length > 0 ? this.categories[0].id : 0
        };
        this.filteredProducts = [];
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        this.message = 'Tạo sản phẩm thất bại!';
        this.message= err.message;
      }
    });
  }
}