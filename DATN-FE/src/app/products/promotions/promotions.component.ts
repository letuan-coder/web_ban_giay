import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Promotion, PromotionRequest, PromotionType } from '../../model/promotion.model';
import { PromotionService } from '../../services/promotion.service';
import { ProductVariantService } from '../../services/product-variant.service';
import { VariantResponse } from '../../model/variant.response.model';

@Component({
  selector: 'app-promotions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl:'./promotions.component.html',
  styleUrls: ['./promotions.component.scss']
})
export class PromotionsComponent implements OnInit {
  promotions: Promotion[] = [];
  productVariants: VariantResponse[] = [];
  
  showForm = false;
  isEditing = false;
  currentPromotion: PromotionRequest = this.getInitialPromotion();
  currentPromotionId: number | null = null;

  promotionTypes = Object.values(PromotionType);

  constructor(
    private promotionService: PromotionService,
    private productVariantService: ProductVariantService
  ) { }

  ngOnInit(): void {
    this.loadPromotions();
    this.loadProductVariants();
  }

  loadPromotions(): void {
    this.promotionService.getAll().subscribe(response => {
      this.promotions = response.data;
    });
  }

  loadProductVariants(): void {
    // Assuming productVariantService.getAll() exists and returns all variants.
    // This might need adjustment based on actual service implementation.
    this.productVariantService.getAllVariants().subscribe(response => {
      this.productVariants = response.data;
    });
  }

  getInitialPromotion(): PromotionRequest {
    return {
      name: '',
      description: '',
      promotionType: PromotionType.PERCENTAGE,
      discountValue: 0,
      startDate: '',
      endDate: '',
      active: true,
      productVariantIds: []
    };
  }

  openCreateForm(): void {
    this.isEditing = false;
    this.showForm = true;
    this.currentPromotion = this.getInitialPromotion();
    this.currentPromotionId = null;
  }

  openEditForm(promotion: Promotion): void {
    this.isEditing = true;
    this.showForm = true;
    this.currentPromotionId = promotion.id;
    this.currentPromotion = {
      name: promotion.name,
      description: promotion.description,
      promotionType: promotion.promotionType,
      discountValue: promotion.discountValue,
      startDate: new Date(promotion.startDate).toISOString().substring(0, 16),
      endDate: new Date(promotion.endDate).toISOString().substring(0, 16),
      active: promotion.active,
      productVariantIds: promotion.productVariantIds || []
    };
  }

  closeForm(): void {
    this.showForm = false;
  }

  onVariantSelectionChange(variantId: string, event: any): void {
    if (!this.currentPromotion.productVariantIds) {
      this.currentPromotion.productVariantIds = [];
    }
    if (event.target.checked) {
      this.currentPromotion.productVariantIds.push(variantId);
    } else {
      const index = this.currentPromotion.productVariantIds.indexOf(variantId);
      if (index > -1) {
        this.currentPromotion.productVariantIds.splice(index, 1);
      }
    }
  }

  handleSubmit(): void {
    const promotionData = {
      ...this.currentPromotion,
      startDate: new Date(this.currentPromotion.startDate).toISOString(),
      endDate: new Date(this.currentPromotion.endDate).toISOString()
    };

    if (this.isEditing && this.currentPromotionId) {
      this.promotionService.update(this.currentPromotionId, promotionData).subscribe(() => {
        this.loadPromotions();
        this.closeForm();
      });
    } else {
      this.promotionService.create(promotionData).subscribe(() => {
        this.loadPromotions();
        this.closeForm();
      });
    }
  }

  deletePromotion(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa khuyến mãi này không?')) {
      this.promotionService.delete(id).subscribe(() => {
        this.loadPromotions();
      });
    }
  }
}
