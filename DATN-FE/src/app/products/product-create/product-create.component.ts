import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { ProductService, ProductCreateRequest } from '../../services/product.service';
import { Brand, BrandService } from '../../services/brand.service';
import { Category, CategoryService } from '../../services/category.service';
import { Color, ColorService } from '../../services/color.service';
import { Size, SizeService } from '../../services/size.service';
import { NGX_CURRENCY_CONFIG, NgxCurrencyDirective } from 'ngx-currency';

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule,NgxCurrencyDirective],
  templateUrl: './product-create.component.html',
  styleUrls: ['./product-create.component.scss']
})
export class ProductCreateComponent implements OnInit {
  brands: Brand[] = [];
  categories: Category[] = [];
  colors: Color[] = [];
  sizes: Size[] = [];
  productsExisting: string[] = [];
  filteredProducts: string[] = [];

  product: ProductCreateRequest = {
    name: '',
    description: '',
    weight: 0,
    brandId: 0,
    categoryId: 0,
    price: 0,
    file: undefined,
    colorCodes: [],
    sizeCodes: []
  };

  loading = false;
  message = '';

  constructor(
    private productService: ProductService,
    private brandService: BrandService,
    private categoryService: CategoryService,
    private colorService: ColorService,
    private sizeService: SizeService
  ) { }

  ngOnInit(): void {
    this.loadBrands();
    this.loadCategories();
    this.loadColors();
    this.loadSizes();
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
        this.product.categoryId = this.categories[0].id; 
      }
    });
  }

  loadColors() {
    this.colorService.getAll().subscribe((response: any) => {
      this.colors = response.data;
    });
  }

  loadSizes() {
    this.sizeService.getAll().subscribe((response: any) => {
      this.sizes = response.data;
    });
  }

  onFileSelected(event: any) {
    if (event.target.files.length > 0) {
      this.product.file = event.target.files[0];
    }
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

  onSizeChange(event: any, sizeCode: string) {
    if (!this.product.sizeCodes) {
      this.product.sizeCodes = [];
    }
    if (event.target.checked) {
      this.product.sizeCodes.push(sizeCode);
    } else {
      const index = this.product.sizeCodes.indexOf(sizeCode);
      if (index > -1) {
        this.product.sizeCodes.splice(index, 1);
      }
    }
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
          categoryId: this.categories.length > 0 ? this.categories[0].id : 0,
          price: 0,
          file: undefined,
          colorCodes: [],
          sizeCodes: []
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