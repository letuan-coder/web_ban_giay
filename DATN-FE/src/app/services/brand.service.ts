import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { ApiResponse } from './sale.service';

export interface Brand {
  id: number;
  name: string;
}

@Injectable({ providedIn: 'root' })
export class BrandService {
  private apiUrl = environment.apiBaseUrl+'/api/brands';

  constructor(private http: HttpClient) { }

  getAll(): Observable<ApiResponse<Brand[]>> {
    return this.http.get<ApiResponse<Brand[]>>(this.apiUrl);
  }

  create(brand: { name: string }): Observable<ApiResponse<Brand>> {
    return this.http.post<ApiResponse<Brand>>(this.apiUrl, brand);
  }

  update(id: number, brand: { name: string }): Observable<ApiResponse<Brand>> {
    return this.http.put<ApiResponse<Brand>>(`${this.apiUrl}/${id}`, brand);
  }

  delete(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
