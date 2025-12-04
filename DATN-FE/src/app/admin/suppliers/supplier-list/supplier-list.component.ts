import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SupplierService, SupplierResponse } from '../../../services/supplier.service';
import { ProductService } from '../../../services/product.service';
import { Product } from '../../../model/product.model';
import { Router, RouterLink } from '@angular/router';

@Component({
  selector: 'app-supplier-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './supplier-list.component.html',
  styleUrls: ['./supplier-list.component.scss']
})
export class SupplierListComponent implements OnInit {
  suppliers: SupplierResponse[] = [];
  loading = false;
  error: string | null = null;

  selectedSupplierIdForProducts: number | null = null;
  productsForSelectedSupplier: Product[] = [];
  loadingProducts = false;
  productsError: string | null = null;

  constructor(
    private supplierService: SupplierService,
    private productService: ProductService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadSuppliers();
  }

  loadSuppliers(): void {
    this.loading = true;
    this.error = null;
    this.supplierService.getAllSuppliers().subscribe({
      next: (response: any) => { // Use 'any' to handle wrapper object
        this.suppliers = response.data; // Extract the array from the .data property
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Không thể tải danh sách nhà cung cấp.';
        this.loading = false;
        console.error(err);
      }
    });
  }

  toggleProductsForSupplier(supplierId: number): void {
    if (this.selectedSupplierIdForProducts === supplierId) {
      this.selectedSupplierIdForProducts = null; // Hide products if already showing
      this.productsForSelectedSupplier = [];
    } else {
      this.selectedSupplierIdForProducts = supplierId;
      this.loadProductsForSupplier(supplierId);
    }
  }

  loadProductsForSupplier(supplierId: number): void {
    this.loadingProducts = true;
    this.productsError = null;
    this.productService.getProductsBySupplier(supplierId).subscribe({
      next: (response: any) => { // Use 'any' to handle wrapper object
        this.productsForSelectedSupplier = response.data; // Extract the array from the .data property
        this.loadingProducts = false;
      },
      error: (err) => {
        this.productsError = 'Không thể tải sản phẩm cho nhà cung cấp này.';
        this.loadingProducts = false;
        console.error(err);
      }
    });
  }

  importStock(supplierId: number): void {
    this.router.navigate(['/admin/stock-transaction'], {
      queryParams: {
        type: 'IMPORT_TO_WAREHOUSE',
        supplierId: supplierId
      }
    });
  }

  // Placeholder for delete functionality (to be implemented later if needed)
  deleteSupplier(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa nhà cung cấp này?')) {
      this.supplierService.deleteSupplier(id).subscribe({
        next: () => {
          this.loadSuppliers(); // Reload list after deletion
        },
        error: (err) => {
          this.error = 'Lỗi khi xóa nhà cung cấp.';
          console.error(err);
        }
      });
    }
  }
}
