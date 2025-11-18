import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

const API_URL = 'http://localhost:8080/api';

export interface SaleResponse {
  total_Amount: number;
  daily: { [key: string]: number };
  monthly: { [key: string]: number };
  yearly: { [key: string]: number };
}

export interface ApiResponse<T> {
  data: T;
}


@Injectable({
  providedIn: 'root'
})
export class SaleService {

  constructor(private http: HttpClient) { }

  getSalesData(): Observable<ApiResponse<SaleResponse>> {
    return this.http.get<ApiResponse<SaleResponse>>(`${API_URL}/sales`);
  }
}
