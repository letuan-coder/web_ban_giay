import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Stock } from '../model/stock.model';
import { ApiResponse } from './sale.service'; // Assuming ApiResponse is shared

@Injectable({
  providedIn: 'root'
})
export class StockService {
  private apiUrl = '/api/stocks'; // Adjust API URL as needed

  constructor(private http: HttpClient) { }

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
