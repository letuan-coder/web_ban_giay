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

  getAll(): Observable<{ data: Warehouse[] }> {
    return this.http.get<{ data: Warehouse[] }>(this.apiUrl);
  }
}
