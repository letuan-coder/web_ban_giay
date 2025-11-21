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
  price?: number;
  weight?: number;
  thumbnailUrl?:string;
  altText?:string;
  slug:string
  createdAt:string;
  colorResponses: ColorVariantResponse[];
}