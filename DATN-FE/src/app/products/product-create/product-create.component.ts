import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';

import { ProductService, ProductCreateRequest } from '../../services/product.service';
import { Brand, BrandService } from '../../services/brand.service';
import { Category, CategoryService } from '../../services/category.service';
import { Color, ColorService } from '../../services/color.service';
import { Size, SizeService } from '../../services/size.service';
import { SupplierService } from '../../services/supplier.service';
import { SupplierResponse } from '../../model/supllier.model';
import { NgxCurrencyDirective } from 'ngx-currency';

// Define interfaces for better structure
export interface ColorEntry {
  isNewColor: boolean;
  colorName: string | null;
  newColorName: string;
  newColorCode: string;
  sizeCodes: string[];
}

export interface ProductBaseInfo {
  name: string;
  description: string;
  brandId: number | null;
  categoryId: number | null;
  supplierId: number | null;
  price: number;
  importPrice: number;
}

@Component({
  selector: 'app-product-create',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxCurrencyDirective],
  templateUrl: './product-create.component.html',
  styleUrls: ['./product-create.component.scss']
})
export class ProductCreateComponent implements OnInit {
  // Data for dropdowns
  brands: Brand[] = [];
  categories: Category[] = [];
  colors: Color[] = [];
  sizes: Size[] = [];
  suppliers: SupplierResponse[] = [];
  productsExisting: string[] = [];
  filteredProducts: string[] = [];

  // Form data
  product: ProductBaseInfo = {
    name: '',
    description: '',
    brandId: null,
    categoryId: null,
    supplierId: null,
    price: 0,
    importPrice: 0,
  };
  variantData: ColorEntry[] = [];
  selectedMainFile: File | undefined = undefined;

  // UI state
  loading = false;
  message = '';
  isError = false;

  constructor(
    private productService: ProductService,
    private brandService: BrandService,
    private categoryService: CategoryService,
    private colorService: ColorService,
    private sizeService: SizeService,
    private supplierService: SupplierService
  ) {}

  ngOnInit(): void {
    this.loadInitialData();
    this.addColorEntry(); // Start with one variant color entry
  }

  loadInitialData(): void {
    this.brandService.getAll().subscribe(res => {
        this.brands = res.data;
        if (this.brands.length > 0) this.product.brandId = this.brands[0].id;
    });
    this.categoryService.getAll().subscribe(res => {
        this.categories = res.data;
        if (this.categories.length > 0) this.product.categoryId = this.categories[0].id;
    });
    this.colorService.getAll().subscribe(res => this.colors = res.data);
    this.sizeService.getAll().subscribe(res => this.sizes = res.data);
    this.supplierService.getAllSuppliers().subscribe(res => {
        this.suppliers = res.data;
        if (this.suppliers.length > 0) this.product.supplierId = this.suppliers[0].id;
    });
    // this.productService.getAllProductNames().subscribe(res => this.productsExisting = res.data);
  }

  // --- Methods for managing variant UI ---

  addColorEntry(): void {
    this.variantData.push({
      isNewColor: false,
      colorName: null,
      newColorName: '',
      newColorCode: '#000000',
      sizeCodes: []
    });
  }

  removeColorEntry(index: number): void {
    this.variantData.splice(index, 1);
  }

  toggleSizeInColor(sizeCode: string, colorEntry: ColorEntry): void {
    const index = colorEntry.sizeCodes.indexOf(sizeCode);
    if (index > -1) {
      colorEntry.sizeCodes.splice(index, 1);
    } else {
      colorEntry.sizeCodes.push(sizeCode);
    }
  }

  toggleAllSizes(colorEntry: ColorEntry): void {
    if (colorEntry.sizeCodes.length === this.sizes.length) {
      colorEntry.sizeCodes = [];
    } else {
      colorEntry.sizeCodes = this.sizes.map(size => size.code);
    }
  }

  // --- TrackBy functions for ngFor performance ---

  trackByColorEntry(index: number, item: ColorEntry): number {
    return index;
  }

  // --- File and input handlers ---

  onMainFileSelected(event: Event): void {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList && fileList.length > 0) {
      this.selectedMainFile = fileList[0];
    }
  }

  onNameInput(): void {
    if (this.product.name) {
      const input = this.product.name.toLowerCase();
      this.filteredProducts = this.productsExisting
        .filter(p => p.toLowerCase().includes(input));
    } else {
      this.filteredProducts = [];
    }
  }

  selectSuggestion(name: string): void {
    this.product.name = name;
    this.filteredProducts = [];
  }

  // --- Form submission ---

  createProduct(productForm: NgForm): void {
    if (productForm.invalid) {
      this.isError = true;
      this.message = 'Vui lòng điền đầy đủ các trường thông tin bắt buộc.';
      return;
    }

    this.loading = true;
    this.isError = false;
    this.message = '';

    // Flatten color and size data for the backend
    const colorCodes = this.variantData.map(entry => {
      if (entry.isNewColor) {
        // Handle new color creation if backend supports it by name/code,
        // otherwise this needs a separate API call first.
        // For now, let's assume we can send the new name.
        return entry.newColorName;
      }
      return entry.colorName;
    }).filter((name): name is string => name !== null && name !== '');

    const allSizeCodes = this.variantData.flatMap(entry => entry.sizeCodes);
    const uniqueSizeCodes = [...new Set(allSizeCodes)];

    const request: ProductCreateRequest = {
        name: this.product.name,
        description: this.product.description,
        brandId: this.product.brandId!,
        categoryId: this.product.categoryId!,
        supplierId: this.product.supplierId!,
        price: this.product.price,
        importPrice: this.product.importPrice,
        file: this.selectedMainFile,
        colorCodes: colorCodes,
        sizeCodes: uniqueSizeCodes
    };

    this.productService.createProduct(request).subscribe({
      next: (res) => {
        this.loading = false;
        this.isError = false;
        this.message = 'Sản phẩm đã được tạo thành công!';
        this.resetForm(productForm);
      },
      error: (err) => {
        this.loading = false;
        this.isError = true;
        this.message = err.error?.message || 'Đã có lỗi xảy ra khi tạo sản phẩm.';
        console.error(err);
      }
    });
  }

  private resetForm(productForm: NgForm): void {
    productForm.resetForm();
    this.product = {
      name: '',
      description: '',
      brandId: this.brands.length > 0 ? this.brands[0].id : null,
      categoryId: this.categories.length > 0 ? this.categories[0].id : null,
      supplierId: this.suppliers.length > 0 ? this.suppliers[0].id : null,
      price: 0,
      importPrice: 0
    };
    this.variantData = [];
    this.addColorEntry();
    this.selectedMainFile = undefined;
  }
}