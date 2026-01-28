import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Brand, BrandService } from '../../services/brand.service';
import { ApiResponse } from '../../services/sale.service';

@Component({
  selector: 'app-brands',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './brands.component.html',
  styleUrls: ['./brands.component.scss']
})
export class BrandsComponent implements OnInit {
  brands: Brand[] = [];
  selectedBrand: Partial<Brand> = {};
  showForm = false;

  constructor(private brandService: BrandService) { }

  ngOnInit(): void {
    this.loadBrands();
  }

  loadBrands(): void {
    this.brandService.getAll().subscribe((response: ApiResponse<Brand[]>) => {
      this.brands = response.data;
    });
  }

  showAddForm(): void {
    this.selectedBrand = {};
    this.showForm = true;
  }

  selectBrand(brand: Brand): void {
    this.selectedBrand = { ...brand };
    this.showForm = true;
  }

  resetForm(): void {
    this.selectedBrand = {};
    this.showForm = false;
  }

  saveBrand(): void {
    if (this.selectedBrand.name) {
      if (this.selectedBrand.id) {
        // Update existing brand
        this.brandService.update(this.selectedBrand.id, { name: this.selectedBrand.name })
          .subscribe(() => {
            this.loadBrands();
            this.resetForm();
          });
      } else {
        // Create new brand
        this.brandService.create({ name: this.selectedBrand.name })
          .subscribe(() => {
            this.loadBrands();
            this.resetForm();
          });
      }
    }
  }

  deleteBrand(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa thương hiệu này không?')) {
      this.brandService.delete(id).subscribe(() => {
        this.loadBrands();
      });
    }
  }
}