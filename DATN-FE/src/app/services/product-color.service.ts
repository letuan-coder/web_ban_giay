import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class ProductColorService {
  private baseUrl = environment.apiBaseUrl + '/api/product-colors';

  constructor(private http: HttpClient) { }
  deleteProductColor(productColorId: string): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${productColorId}`)
  }
 
  create(productId: string, formData: FormData): Observable<any> {
    return this.http.post(`${this.baseUrl}/${productId}`, formData);
  }
  
}
