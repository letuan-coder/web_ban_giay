import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Stock } from '../model/stock.model';
import { ApiResponse } from './sale.service'; // Assuming ApiResponse is shared
import { environment } from '../../enviroment/enviroment';

// Interfaces for stock receipt based on backend DTOs
export interface StockTransactionItemReceived {
  stockTransactionId: string; // This is the VARIANT ID (as UUID string)
  receivedQuantity: number;
}

export interface StockReceiptRequest {
  transactionCode: string;
  stockTransactionItemId: StockTransactionItemReceived[];
  stockTransactionId?: string; // Transaction ID as UUID string, if available
}


@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = environment.apiBaseUrl + '/api/stocks';

  constructor(private http: HttpClient) { }

  // New method for receiving stock based on a transaction
  receiveStock(request: StockReceiptRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}`, request);
  }

  getAll(): Observable<ApiResponse<Stock[]>> {
    return this.http.get<ApiResponse<Stock[]>>(this.apiUrl);
  }

  getById(id: number): Observable<ApiResponse<Stock>> {
    return this.http.get<ApiResponse<Stock>>(`${this.apiUrl}/${id}`);
  }

  create(stock: Partial<Stock>): Observable<ApiResponse<Stock>> {
    return this.http.post<ApiResponse<Stock>>(this.apiUrl, stock);
  }

  update(id: number, stock: Partial<Stock>): Observable<ApiResponse<Stock>> {
    return this.http.put<ApiResponse<Stock>>(`${this.apiUrl}/${id}`, stock);
  }

  delete(id: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`);
  }
}
