import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { Promotion, PromotionRequest } from '../model/promotion.model';

@Injectable({
  providedIn: 'root'
})
export class PromotionService {
  private apiUrl = `${environment.apiBaseUrl}/api/promotions`;

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getById(id: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${id}`);
  }

  create(promotion: PromotionRequest): Observable<any> {
    return this.http.post<any>(this.apiUrl, promotion);
  }

  update(id: number, promotion: PromotionRequest): Observable<any> {
    return this.http.put<any>(`${this.apiUrl}/${id}`, promotion);
  }

  delete(id: number): Observable<any> {
    return this.http.delete<any>(`${this.apiUrl}/${id}`);
  }
  addVariantsToPromotion(promotionId: number, productVariantIds: string[]): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/${promotionId}/variants`, productVariantIds);
  }
}
