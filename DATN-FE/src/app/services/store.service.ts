import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Store } from '../model/store.model';
import { ApiResponse } from './sale.service';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class StoreService {
  private apiUrl = environment.apiBaseUrl+'/api/stores';

  constructor(private http: HttpClient) { }

  getAll(): Observable<ApiResponse<Store[]>> {
    return this.http.get<ApiResponse<Store[]>>(this.apiUrl);
  }

  getById(id: number): Observable<ApiResponse<Store>> {
    return this.http.get<ApiResponse<Store>>(`${this.apiUrl}/${id}`);
  }

  create(store: Partial<Store>): Observable<ApiResponse<Store>> {
    return this.http.post<ApiResponse<Store>>(this.apiUrl, store);
  }

  update(id: number, store: Partial<Store>): Observable<ApiResponse<Store>> {
    return this.http.put<ApiResponse<Store>>(`${this.apiUrl}/${id}`, store);
  }

  delete(id: number): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`);
  }
}
