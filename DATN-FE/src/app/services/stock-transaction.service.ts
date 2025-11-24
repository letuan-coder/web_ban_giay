import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface StockTransactionItemRequest {
  variantId: string;
  quantity: number;
}

export interface StockTransactionRequest {
  type: string; // 'IN', 'OUT', 'TRANSFER'
  supplierId?: number;
  fromStoreId?: number;
  fromWareHouseId?: number;
  toStoreId?: number;
  toWareHouseId?: number;
  items: StockTransactionItemRequest[];
}

@Injectable({
  providedIn: 'root'
})
export class StockTransactionService {
  private apiUrl = environment.apiBaseUrl + '/api/stock-transactions';

  constructor(private http: HttpClient) { }

  createTransaction(request: StockTransactionRequest): Observable<any> {
    return this.http.post(this.apiUrl, request);
  }
}
