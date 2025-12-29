import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Product } from '../model/product.model';
import { environment } from '../../enviroment/enviroment';

export interface ProductCreateRequest {
  name: string;
  description: string;
  brandId: number;
  categoryId: number;
  supplierId: number;
  price?: number;
  importPrice?: number;
  file?: File;
  colorCodes?: string[];
  sizeCodes?: string[];
}

export interface updateProductRequest {
  name: string;
  description:string;
  brandId: number;
  categoryId: number;
  price?: number;
}
@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private baseUrl = environment.apiBaseUrl + '/api/products';
  private deleteUrl = environment.apiBaseUrl + '/api/images'
  private adminUrl = environment.apiBaseUrl + '/api/products/admin'
  constructor(private http: HttpClient) { }


  getAll(page: number, size: number, name?: string): Observable<any> {
    let url = `${this.baseUrl}?page=${page}&limit=${size}`;
    if (name) {
      url += `&name=${name}`;
    }
    return this.http.get<any>(url);
  }

  createProductWithVariants(formData: FormData): Observable<any> {
    return this.http.post(this.baseUrl, formData);
  }

  createProduct(product: ProductCreateRequest): Observable<any> {
    const formData = new FormData();
    formData.append('name', product.name);
    const encoded = product.description.replace(/\n/g, '\\n');
    formData.append('description', encoded); formData.append('brandId', product.brandId.toString());
    formData.append('categoryId', product.categoryId.toString());
    formData.append('supplierId', product.supplierId.toString());
    if (product.price) {
      formData.append('price', product.price.toString());
    }
    if (product.file) {
      formData.append('file', product.file);
    }
    if (product.colorCodes) {
      product.colorCodes.forEach(code => formData.append('colorCodes', code));
    }
    if (product.sizeCodes) {
      product.sizeCodes.forEach(code => formData.append('sizeCodes', code));
    }
    if (product.importPrice) {
      formData.append('importPrice', product.importPrice.toString());

    }
    return this.http.post(this.baseUrl, formData);
  }
  // bạn có thể thêm create/update/delete nếu muốn
  deleteProduct(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
  updateProduct(product: updateProductRequest, id: string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/${id}`, product);
  }

  updateProductWithImage(product: updateProductRequest, id: string, file: File): Observable<any> {
    const formData = new FormData();
    const productBlob = new Blob([JSON.stringify(product)], { type: 'application/json' });
    formData.append('data', productBlob);
    formData.append('file', file);
    return this.http.patch(`${this.baseUrl}/${id}/images`, formData);
  }

  deleteProductImage(id: string): Observable<any> {
    return this.http.delete(`${this.deleteUrl}/${id}`);
  }

  getById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  getAdminById(id: string): Observable<any> {
    return this.http.get<any>(`${this.adminUrl}/${id}`);
  }
  search(keyword: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/search?keyword=${keyword}`);
  }
  getProductsBySupplier(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/supplier/admin/${id}`);
  }

  getProductByCode(code: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/code/${code}`);
  }
}
