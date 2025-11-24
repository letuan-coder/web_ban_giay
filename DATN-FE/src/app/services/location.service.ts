import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Province, District, Commune } from '../model/location.model';
import { ApiResponse } from './sale.service';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class LocationService {
  private apiUrl = environment.apiBaseUrl+"/api";

  constructor(private http: HttpClient) { }

  getProvinces(): Observable<ApiResponse<Province[]>> {
    return this.http.get<ApiResponse<Province[]>>(`${this.apiUrl}/provinces`);
  }

  getDistricts(provinceCode: string): Observable<ApiResponse<District[]>> {
    return this.http.get<ApiResponse<District[]>>(`${this.apiUrl}/districts/${provinceCode}`);
  }

  getCommunes(districtCode: string): Observable<ApiResponse<Commune[]>> {
    return this.http.get<ApiResponse<Commune[]>>(`${this.apiUrl}/communes/${districtCode}`);
  }
}
