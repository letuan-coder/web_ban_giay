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
  weight: number;
}
export interface updateProductRequest {
  name: string;
  description: string;
  weight: number;
  brandId: number;
  categoryId: number;
}
@Injectable({
  providedIn: 'root'
})
export class ProductService {
  private baseUrl = environment.apiBaseUrl+'/api/products';

  constructor(private http: HttpClient) { }


  getAll(page: number, size: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}?page=${page}&size=${size}`);
  }
  createProduct(product: ProductCreateRequest): Observable<any> {
    return this.http.post(this.baseUrl, product);
  }
  // bạn có thể thêm create/update/delete nếu muốn
  deleteProduct(id: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
  updateProduct(product:updateProductRequest,id:string): Observable<any> {
    return this.http.patch(`${this.baseUrl}/${id}`, product);
  }
  getById(id: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/${id}`);
  }

  search(name: string): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/search?name=${name}`);
  }
}
