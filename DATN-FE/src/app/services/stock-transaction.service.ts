import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface StockTransactionItemRequest {
  variantId: string;
  quantity: number;
}

export interface StockTransactionRequest {
  type: string;
  supplierId?: string;
  fromStoreId?: string;
  fromWarehouseId?: string;
  toStoreId?: string;
  toWarehouseId?: string;
  expectedReceivedDate?: string;
  items: StockTransactionItemRequest[];
}

export interface MissingItem {
  productVariantId: number;
  quantity: number;
}

export interface CreateMissingItemsInvoiceRequest {
  originalTransactionId: number;
  missingItems: MissingItem[];
}

// Define the StockTransactionItemResponse interface based on backend DTO
export interface StockTransactionItemResponse {
  id: string;
  variantId: string;
  name: string;
  colorName: string;
  sizeName: number;
  variantSku: string;
  quantity: number;
}

// Define the StockTransactionResponse interface based on backend DTO
export interface StockTransactionResponse {
  id: string;
  code: string;
  type: string;
  transactionStatus: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'PENDING_COMPLETION';
  supplierId?: string;
  supplierName?: string;
  fromWarehouseId?: string;
  fromWarehouseName?: string;
  fromStoreId?: string;
  fromStoreName?: string;
  toWarehouseId?: string;
  toWarehouseName?: string;
  toStoreId?: string;
  toStoreName?: string;
  createdDate: string;
  items: StockTransactionItemResponse[];
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

getTransactionById(id: string): Observable<any> {
  // id là string, không convert sang Number
  return this.http.get(`${this.apiUrl}/code/${id}`);
}

  createMissingItemsInvoice(request: CreateMissingItemsInvoiceRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-missing-items-invoice`, request);
  }

  getAllTransactions(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
}