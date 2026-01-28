import { ColorVariantResponse } from "./variant.response.model";

export interface Product {
  id: string;
  name: string;
  description?: string;
  brandId :number;
  brandName:String;
  categoryName:String;
  productCode:String;
  categoryId:number;
  supplierId?: number;
  supplierName?: string;
  price?: number;
  thumbnailUrl?:string;
  altText?:string;
  slug:string
  available:string;
  createdAt:string;
  colorResponses: ColorVariantResponse[];
}
