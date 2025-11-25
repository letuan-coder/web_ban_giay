import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { CdkDragDrop, moveItemInArray, DragDropModule, transferArrayItem } from '@angular/cdk/drag-drop';
import { Banner, BannerType } from '../../model/banner.model';
import { BannerService } from '../../services/banner.service';
import { CategoryService } from '../../services/category.service';
import { BrandService } from '../../services/brand.service';
import { ProductService } from '../../services/product.service';
import { environment } from '../../../enviroment/enviroment';

@Component({
  selector: 'app-banner-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, DragDropModule, FormsModule],
  templateUrl: './banner-list.component.html',
  styleUrls: ['./banner-list.component.scss']
})
export class BannerListComponent implements OnInit {
  public imageApiUrl = environment.apiBaseUrl + '/api/images/view/';
  groupedBanners: { [key: string]: Banner[] } = {};
  bannerTypeKeys: string[] = [];

  bannerForm: FormGroup;
  isEditing = false;
  selectedBannerId: string | null = null;
  showForm = false;
  selectedFile: File | null = null;
  bannerTypes = Object.values(BannerType);

  // Properties for redirect dropdowns
  categories: any[] = [];
  brands: any[] = [];
  products: any[] = [];
  selectedRedirectType: 'category' | 'brand' | 'product' | null = null; // Corrected type to singular
  selectedRedirectId: string | null = null;
  currentEditingBanner: Banner | null = null;

  constructor(
    private bannerService: BannerService,
    private fb: FormBuilder,
    private categoryService: CategoryService,
    private brandService: BrandService,
    private productService: ProductService
  ) {
    this.bannerForm = this.fb.group({
      bannerName: ['', Validators.required],
      redirectUrl: [''],
      sortOrder: [0],
      active: [true],
      startAt: [''],
      endAt: [''],
      type: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.loadBanners();
    this.loadDropdownData();
  }

  loadDropdownData(): void {
    this.categoryService.getAll().subscribe(res => {
      if (res.data) this.categories = res.data;
    });
    this.brandService.getAll().subscribe(res => {
      if (res.data) this.brands = res.data;
    });
    this.productService.getAll(1, 1000).subscribe(res => {
      if (res.data && res.data.content) this.products = res.data.content;
    });
  }

  loadBanners(): void {
    this.bannerService.getBanners().subscribe(response => {
      const banners = response.data.sort((a, b) => a.sortOrder - b.sortOrder);
      this.groupBanners(banners);
    });
  }

  groupBanners(banners: Banner[]): void {
    const grouped = banners.reduce((acc, banner) => {
      const { type } = banner;
      if (!acc[type]) {
        acc[type] = [];
      }
      acc[type].push(banner);
      return acc;
    }, {} as { [key: string]: Banner[] });

    this.bannerTypes.forEach(type => {
      if (!grouped[type]) {
        grouped[type] = [];
      }
    });

    this.groupedBanners = grouped;
    this.bannerTypeKeys = Object.keys(this.groupedBanners).sort();
  }

  drop(event: CdkDragDrop<Banner[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    }
    this.updateBannersOrderAndType();
  }

  updateBannersOrderAndType(): void {
    const payload: { id: string; sortOrder: number; type: string }[] = [];
    this.bannerTypeKeys.forEach(type => {
      this.groupedBanners[type].forEach((banner, index) => {
        payload.push({ id: banner.id, sortOrder: index, type: type });
      });
    });

    this.bannerService.updateSortOrder(payload).subscribe(() => this.loadBanners());
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0] ?? null;
  }

  onSubmit(): void {
    if (this.bannerForm.invalid) return;

    const bannerData = this.bannerForm.value;

    if (this.isEditing) {
      const formData = new FormData();
      formData.append('data', new Blob([JSON.stringify(bannerData)], { type: 'application/json' }));
      if (this.selectedFile) {
        formData.append('file', this.selectedFile);
      }

      this.bannerService.updateBanner(this.selectedBannerId!, formData).subscribe(() => {
        this.resetForm();
        this.loadBanners();
      });
    } else {
      if (!this.selectedFile) {
        console.error('File is required to create a banner.');
        return;
      }
      const formData = new FormData();
      formData.append('data', new Blob([JSON.stringify(bannerData)], { type: 'application/json' }));
      formData.append('file', this.selectedFile);

      this.bannerService.createBanner(formData).subscribe(() => {
        this.resetForm();
        this.loadBanners();
      });
    }
  }

  editBanner(currentEditingBanner: Banner): void {
    this.currentEditingBanner = currentEditingBanner;  // ← CẦN THIẾT
    this.isEditing = true;
    this.showForm = true;
    this.selectedBannerId = currentEditingBanner.id;
    this.bannerForm.patchValue({
      ...currentEditingBanner, startAt:
        this.formatDateTime(currentEditingBanner.startAt),
      endAt: this.formatDateTime(currentEditingBanner.endAt)
    });
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
    this.selectedFile = null;

    this.selectedRedirectType = null;
    this.selectedRedirectId = null;
    const url = currentEditingBanner.redirectUrl;
    if (url) {
      const productDetailMatch = url.match(/^\/products\/(\w+)$/);
      if (productDetailMatch) {
        this.selectedRedirectType = 'product';
        this.selectedRedirectId = productDetailMatch[1];
      } else {
        const queryString = url.split('?')[1];
        if (queryString) {
          const urlParams = new URLSearchParams(queryString);
          if (urlParams.has('category_id')) {
            this.selectedRedirectType = 'category';
            this.selectedRedirectId = urlParams.get('category_id');
          } else if (urlParams.has('brand_id')) {
            this.selectedRedirectType = 'brand';
            this.selectedRedirectId = urlParams.get('brand_id');
          }
        }
      }
    }

  }

  deleteBanner(id: string): void {
    if (confirm('Are you sure you want to delete this banner?')) {
      this.bannerService.deleteBanner(id).subscribe(() => this.loadBanners());
    }
  }

  deleteImage(banner: Banner): void {
    if (confirm('Are you sure you want to delete the image for this banner?')) {
      if (!banner.id) { // Check for banner.id instead of banner.imageUrl
        console.error('No banner ID to delete image.');
        return;
      }
      this.bannerService.deleteImage(banner.id).subscribe({
        next: () => {
          const updatedBanner = { ...banner, imageUrl: null };
          this.bannerService.updateBanner(banner.id, updatedBanner).subscribe({
            next: () => {
              this.loadBanners(); // Reload to show the change
            },
            error: (err) => console.error('Error updating banner after image deletion:', err)
          });
        },
        error: (err) => console.error('Error deleting image:', err)
      });
    }
  }

  clearImageForUpload(): void {
    if (this.currentEditingBanner) {
      this.currentEditingBanner.imageUrl = ''; 
      this.deleteImage(this.currentEditingBanner);
    }
    this.selectedFile = null; // Clear any selected file for upload
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
      fileInput.value = ''; // Clear the file input element
    }
    this.bannerForm.markAsDirty(); // Mark form as dirty to enable Save button
  }

  openForm(): void {
    this.resetForm();
    this.showForm = true;
    this.isEditing = false;
  }

  resetForm(): void {
    this.bannerForm.reset({ sortOrder: 0, active: true });
    this.selectedFile = null;
    this.isEditing = false;
    this.selectedBannerId = null;
    this.showForm = false;
    this.selectedRedirectType = null;
    this.selectedRedirectId = null;
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) fileInput.value = '';
  }

  onRedirectTypeChange(): void {
    this.selectedRedirectId = null;
    this.onEntitySelect();
  }

  onEntitySelect(): void {
    let url = '';
    if (this.selectedRedirectType && this.selectedRedirectId) {
      if (this.selectedRedirectType === 'product') {
        url = `/products/${this.selectedRedirectId}`;
      } else if (this.selectedRedirectType === 'category') {
        url = `/products?category_id=${this.selectedRedirectId}`;
      } else if (this.selectedRedirectType === 'brand') {
        url = `/products?brand_id=${this.selectedRedirectId}`;
      }
      this.bannerForm.get('redirectUrl')?.setValue(url);
    } else {
      this.bannerForm.get('redirectUrl')?.setValue('');
    }
  }

  private formatDateTime(dateTime: string | null): string {
    if (!dateTime) return '';
    const date = new Date(dateTime);
    return date.toISOString().substring(0, 10);
  }
}
