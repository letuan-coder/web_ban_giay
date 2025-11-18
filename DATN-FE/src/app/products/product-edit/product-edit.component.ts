import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ProductService, updateProductRequest } from '../../services/product.service';
import { CommonModule } from '@angular/common';
import { Product } from "../../model/product.model"
import { FormsModule } from '@angular/forms';
import { Brand, BrandService } from '../../services/brand.service';
import { Category, CategoryService } from '../../services/category.service';

@Component({
  selector: 'app-product-edit',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './product-edit.component.html',
  styleUrl: './product-edit.component.scss'
})

export class ProductEditComponent implements OnInit {
  product: Product | undefined;
  brands: Brand[] = [];
  categories: Category[] = [];
  loading = false;
  message = '';

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private brandService: BrandService,
    private categoryService: CategoryService,
    private router: Router
  ) { }
  Updateproduct: updateProductRequest = {
    weight: 0,  
    name: '',
    description: '',
    brandId: 0,
    categoryId: 0,
  };
  ngOnInit(): void {
    console.log('ProductEditComponent ngOnInit called.'); // Added log
    this.loadBrands();
    this.loadCategories();
    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      console.log('productId before check:', productId); // Added log
      if (productId) {
        console.log('Product ID from route:', productId); // Debug log
        this.productService.getById(productId).subscribe({
          next: (response: any) => {
            this.product = response.data;
            if (this.product) {
              this.Updateproduct = {
                weight: this.product.weight || 0,
                name: this.product?.name,
                description: this.product.description|| '',
                brandId: this.product.brandId ||1,
                categoryId: this.product.categoryId||1
              };
            }
          },
          error: (err) => {
            console.error('Error fetching product:', err);
            this.message = `Failed to load product details: ${err.message || err.error?.message || 'Unknown error'}`;
          }

        });
      }
    });
  }

  loadBrands() {
    this.brandService.getAll().subscribe((response: any) => {
      this.brands = response.data;
    });
  }

  loadCategories() {
    this.categoryService.getAll().subscribe((response: any) => {
      this.categories = response.data;
      
    });
  }

  updateProduct() {
    if (!this.product || !this.product.id) {
      this.message = 'Product data is missing.';
      return;
    }
    this.loading = true;
    this.message = '';

    this.productService.updateProduct(this.Updateproduct, this.product.id).subscribe({
      next: (res) => {
        this.loading = false;
        this.message = 'Update product successfully';
        this.router.navigate(['/products']);
      },
      error: (err) => {
        this.loading = false;
        console.error(err);
        this.message = 'Failed to update product.';
      }
    });
  }
}