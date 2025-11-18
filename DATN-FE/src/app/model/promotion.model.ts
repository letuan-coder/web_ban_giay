

export interface Promotion {
    id: number;
    name: string;
    description: string;
    promotionType: PromotionType;
    discountValue: number;
    startDate: string; 
    endDate: string;
    active: boolean;
    productVariantIds?: string[];
  }
  
  export interface PromotionRequest {
    name: string;
    description: string;
    promotionType: PromotionType;
    discountValue: number;
    startDate: string;
    endDate: string;
    active: boolean;
    productVariantIds?: string[];
  }
  
  export enum PromotionType {
    PERCENTAGE = 'PERCENTAGE',
    FIXED_AMOUNT = 'FIXED_AMOUNT'
  }
  