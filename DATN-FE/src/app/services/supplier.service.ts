import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface SupplierRequest {
  name: string;
  taxCode: string;
  email: string;
  phoneNumber: string;
  supplierAddress: string;
  status?: string; // Assuming SupplierStatus is a string enum in backend
}

export interface SupplierResponse {
  id: string;
  supplierCode: string;
  name: string;
  taxCode: string;
  email: string;
  phoneNumber: string;
  supplierAddress: string;
  status: string; // Assuming SupplierStatus is a string enum in backend
}

@Injectable({
  providedIn: 'root'
})
export class SupplierService {
  private apiUrl = environment.apiBaseUrl + '/api/suppliers';

  constructor(private http: HttpClient) { }

  createSupplier(request: SupplierRequest): Observable<SupplierResponse> {
    return this.http.post<SupplierResponse>(this.apiUrl, request);
  }

  getAllSuppliers(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getSupplierById(id: string): Observable<SupplierResponse> {
    return this.http.get<SupplierResponse>(`${this.apiUrl}/${id}`);
  }

  updateSupplier(id: String, request: SupplierRequest): Observable<SupplierResponse> {
    return this.http.put<SupplierResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteSupplier(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
