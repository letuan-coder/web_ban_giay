export interface SizeResponse {
  code: string;
  name: number;
}

export interface VariantResponse {
  id: string;
  size?: SizeResponse; // Made optional as per previous content
  isAvailable: string;
  price: number;
  discountPrice: number | null;
  stock: number;
  sku: string;
  createdAt: string; 
}

export interface ImageResponse {
  id: number;
  imageUrl: string;
  altText: string;
}

export interface ColorInfo {
  code: string;
  name: string;
  hexCode: string;
}

export interface ColorVariantResponse {
  id: string;
  color: ColorInfo;
  isAvailable: string;
  variantResponses: VariantResponse[];
  images: ImageResponse[];
}

export interface ProductDetail {
  id: string;
  name: string;
  slug: string;
  productCode: string;
  description: string;
  available: string;
  colorResponses: ColorVariantResponse[];
  createdAt: string; // Added createdAt to ProductDetail
}