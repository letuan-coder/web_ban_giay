import { Component, OnInit } from '@angular/core';
import { Product } from '../../model/product.model';
import { ProductService } from '../../services/product.service';
import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { Router, RouterModule } from '@angular/router';
import { forkJoin } from 'rxjs';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-product-list',
  standalone: true,
  imports: [CommonModule, HttpClientModule, RouterModule, FormsModule],
  templateUrl: './product-list.component.html',
  styleUrls: ['./product-list.component.scss'],
})
export class ProductListComponent implements OnInit {
  products: Product[] = [];
  loading = false;
  error = '';
  selectedProductIds = new Set<string>();

  currentPage = 0;
  pageSize = 15;
  totalPages = 0;

  searchTerm = '';
  isSearch = false;

  constructor(
    private productService: ProductService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadProducts(this.currentPage);
  }

  openCreateProduct() {
    this.router.navigate(['products/create']);
  }

  loadProducts(page: number, name?: string) {
    this.loading = true;
    this.error = '';
    this.isSearch = !!name;
    this.currentPage = page;

    this.productService.getAll(page + 1, this.pageSize, name).subscribe({
      next: (res: any) => {
        this.totalPages = res.data.totalPages;
        this.processProducts(res.data.content);
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không tải được sản phẩm. Vui lòng thử lại.';
        console.error(err);
        this.loading = false;
      }
    });
  }

  searchProducts(): void {
    const trimmedSearchTerm = this.searchTerm.trim();
    if (!trimmedSearchTerm) {
      this.loadProducts(0);
      return;
    }

    this.loading = true;
    this.error = '';
    this.isSearch = true;
    this.currentPage = 0;
    this.totalPages = 0;

    this.productService.search(trimmedSearchTerm).subscribe({
      next: (res: any) => {
        if (res.data && res.data.length > 0) {
          this.processProducts(res.data);
          this.totalPages = 1; 
        } else {
          // If Redis search returns nothing, fall back to DB search
          this.loadProducts(0, trimmedSearchTerm);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không tìm thấy sản phẩm.';
        console.error(err);
        this.products = [];
        this.loading = false;
      }
    });
  }
  processProducts(products: any[]): void {

    const productsWithImages = products.map((product: any) => {
      
      let price = product.price || 0;
      let ThumbnailUrl=product.ThumbnailUrl;
      const allVariants = product.colorResponses?.flatMap((color: any) => color.variantResponses) || [];

      if (allVariants.length > 0) {
        price = Math.min(...allVariants.map((v: any) => Number(v.price)));
      }
      return { ...product,ThumbnailUrl, price };

    });

    this.products = productsWithImages;

  }

  goToPage(page: number): void {
    if (page >= 0 && page < this.totalPages) {
      this.loadProducts(page, this.isSearch ? this.searchTerm : '');
    }
  }

  getPages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  deleteProduct(id: string): void {
    if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) {
      this.loading = true;
      this.productService.deleteProduct(id).subscribe({
        next: () => {
          this.loadProducts(this.currentPage, this.isSearch ? this.searchTerm : '');
        },
        error: (err) => {
          this.error = 'Đã xảy ra lỗi khi xóa sản phẩm.';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }
  onProductSelectionChange(event: any, productId: string): void {
    if (event.target.checked) {
      this.selectedProductIds.add(productId);
    } else {
      this.selectedProductIds.delete(productId);
    }
  }

  deleteSelectedProducts(): void {
    if (this.selectedProductIds.size === 0) {
      return;
    }

    this.loading = true;
    const deleteRequests = Array.from
      (this.selectedProductIds).map(id => this.productService.deleteProduct(id));

    forkJoin(deleteRequests).subscribe({
      next: () => {
        this.selectedProductIds.clear();
        this.loadProducts(this.currentPage, this.isSearch ? this.searchTerm : ''); // Refresh data
      },
      error: (err) => {
        this.error = 'Đã xảy ra lỗi khi xóa sản phẩm.';
        this.loading = false;
        console.error(err);
      }
    });
  }
}

