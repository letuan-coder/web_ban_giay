import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import {  PromotionService } from '../../../services/promotion.service'; 
import { ProductVariantService } from '../../../services/product-variant.service'; // Assuming ProductVariantService is in services
import { ToastrService } from 'ngx-toastr'; // Assuming ToastrService is used for notifications
import { Promotion } from '../../../model/promotion.model';

@Component({
  selector: 'app-apply-promotion-dialog',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './apply-promotion-dialog.component.html',
  styleUrls: ['./apply-promotion-dialog.component.scss']
})
export class ApplyPromotionDialogComponent implements OnInit {
  promotions: Promotion[] = [];
  selectedPromotionId: number | null = null;
  loading = false;

  constructor(
    public dialogRef: MatDialogRef<ApplyPromotionDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { productVariantIds: string[] },
    private promotionService: PromotionService,
    private toastr: ToastrService
  ) { }

  ngOnInit(): void {
    this.loadPromotions();
  }

  loadPromotions(): void {
    this.promotionService.getAll().subscribe({
      next: (res) => {
        this.promotions = res.data;
      },
      error: (err) => {
        this.toastr.error('Không thể tải danh sách khuyến mãi.', 'Lỗi');
        console.error(err);
      }
    });
  }

  applyPromotion(): void {
    if (!this.selectedPromotionId) {
      this.toastr.warning('Vui lòng chọn một khuyến mãi.', 'Cảnh báo');
      return;
    }

    if (this.data.productVariantIds.length === 0) {
      this.toastr.warning('Không có biến thể nào được chọn.', 'Cảnh báo');
      return;
    }

    this.loading = true;
    this.promotionService.addVariantsToPromotion(this.selectedPromotionId, this.data.productVariantIds).subscribe({
      next: () => {
        this.toastr.success('Áp dụng khuyến mãi thành công!', 'Thành công');
        this.dialogRef.close(true); // Close dialog and indicate success
      },
      error: (err) => {
        this.toastr.error('Áp dụng khuyến mãi thất bại.', 'Lỗi');
        console.error(err);
        this.loading = false;
      }
    });
  }

  onCancel(): void {
    this.dialogRef.close(false); // Close dialog and indicate cancellation
  }
}
