import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class ProductVariantService {
  private apiUrl = environment.apiBaseUrl + '/api/product-variants';

  constructor(private http: HttpClient) { }

  getAllVariants(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
  
  deleteVariant(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
  updateVariant(id: string, data: any): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}`, data);
  }
  deleteProductColor(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}
