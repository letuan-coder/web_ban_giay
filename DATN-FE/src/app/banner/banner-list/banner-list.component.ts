import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';

import { CommonModule } from '@angular/common';
import { Banner, BannerType } from '../../model/banner.model';
import { BannerService } from '../../services/banner.service';

@Component({
  selector: 'app-banner-list',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './banner-list.component.html',
  styleUrls: ['./banner-list.component.scss']
})
export class BannerListComponent implements OnInit {
  banners: Banner[] = [];
  bannerForm: FormGroup;
  isEditing = false;
  selectedBannerId: string | null = null;
  showForm = false;
  selectedFile: File | null = null;
  bannerTypes = Object.values(BannerType);

  constructor(
    private bannerService: BannerService,
    private fb: FormBuilder
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
  }

  loadBanners(): void {
    this.bannerService.getBanners().subscribe(response => {
      this.banners = response.data;
    });
  }

  onFileSelected(event: any): void {
    this.selectedFile = event.target.files[0] ?? null;
  }

  onSubmit(): void {
    if (this.bannerForm.invalid) {
      return;
    }

    const formData = new FormData();
    const bannerData = this.bannerForm.value;

    const request = {
      id: this.isEditing ? this.selectedBannerId : undefined,
      ...bannerData
    };

    if (this.isEditing) {
      // Update logic
      this.bannerService.updateBanner(this.selectedBannerId!, bannerData).subscribe(() => {
        this.resetForm();
        this.loadBanners();
      });
    } else {
      // Create logic
      if (!this.selectedFile) {
        // Handle error: file is required for creation
        console.error('File is required to create a banner.');
        return;
      }
      formData.append('data', new Blob([JSON.stringify(request)], { type: 'application/json' }));
      formData.append('file', this.selectedFile);

      this.bannerService.createBanner(formData).subscribe(() => {
        this.resetForm();
        this.loadBanners();
      });
    }
  }

  editBanner(banner: Banner): void {
    this.isEditing = true;
    this.showForm = true;
    this.selectedBannerId = banner.id;
    this.bannerForm.patchValue({
      ...banner,
      startAt: this.formatDateTime(banner.startAt),
      endAt: this.formatDateTime(banner.endAt)
    });
  }

  deleteBanner(id: string): void {
    if (confirm('Are you sure you want to delete this banner?')) {
      this.bannerService.deleteBanner(id).subscribe(() => {
        this.loadBanners();
      });
    }
  }

  openForm(): void {
    this.showForm = true;
    this.isEditing = false;
  }

  resetForm(): void {
    this.bannerForm.reset({
        sortOrder: 0,
        active: true,
    });
    this.selectedFile = null;
    this.isEditing = false;
    this.selectedBannerId = null;
    this.showForm = false;
    // Reset file input
    const fileInput = document.getElementById('fileInput') as HTMLInputElement;
    if (fileInput) {
        fileInput.value = '';
    }
  }

  private formatDateTime(dateTime: string | null): string {
    if (!dateTime) {
      return '';
    }
    const date = new Date(dateTime);
    // This will format to 'YYYY-MM-DDTHH:mm', which is what datetime-local input expects
    return date.toISOString().substring(0, 16);
  }
}
