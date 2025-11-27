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
  supplierId?: number;
  fromStoreId?: number;
  fromWareHouseId?: number;
  toStoreId?: number;
  toWareHouseId?: number;
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
  id: number;
  variantId: number;
  variantSku: string;
  quantity: number;
  variant: {
    id: number;
    sku: string;
    product: { name: string };
    color: { name: string };
    size: { name: string };
  };
}

// Define the StockTransactionResponse interface based on backend DTO
export interface StockTransactionResponse {
  id: number;
  type:['IMPORT', 'EXPORT', 'TRANSFER', 'RETURN_SUPPLIER', 'RETURN_WAREHOUSE', 'ADJUST'];
  transactionStatus: 'PENDING' | 'COMPLETED' | 'CANCELLED' | 'PENDING_COMPLETION';
  supplierId?: number;
  supplierName?: string;
  fromWarehouseId?: number;
  fromWarehouseName?: string;
  fromStoreId?: number;
  fromStoreName?: string;
  toWarehouseId?: number;
  toWarehouseName?: string;
  toStoreId?: number;
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

  getTransactionById(id: number): Observable<any> {
    return this.http.get(`${this.apiUrl}/${id}`);
  }

  createMissingItemsInvoice(request: CreateMissingItemsInvoiceRequest): Observable<any> {
    return this.http.post(`${this.apiUrl}/create-missing-items-invoice`, request);
  }

  getAllTransactions(): Observable<any> {
    return this.http.get(this.apiUrl);
  }
}