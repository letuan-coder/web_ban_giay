import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { Warehouse } from '../model/warehouse.model';

@Injectable({
  providedIn: 'root'
})

export class WarehouseService {
  private apiUrl = environment.apiBaseUrl + '/api/warehouses';

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }

  getByCode(code: string): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${code}`);
  }

  create(warehouse: Partial<Warehouse>): Observable<Warehouse> {
    return this.http.post<Warehouse>(this.apiUrl, warehouse);
  }

  update(code:string, warehouse: Partial<Warehouse>): Observable<Warehouse> {
    return this.http.patch<Warehouse>(`${this.apiUrl}/${code}`, warehouse);
  }

  delete(code:string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${code}`);
  }
}
