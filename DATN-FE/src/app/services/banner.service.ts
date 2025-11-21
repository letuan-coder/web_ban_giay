import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';
import { ApiResponse } from './sale.service';
import { Banner } from '../model/banner.model';

@Injectable({
  providedIn: 'root'
})
export class BannerService {
  private apiUrl = environment.apiBaseUrl + '/api/banners';

  constructor(private http: HttpClient) { }

  getBanners(): Observable<ApiResponse<Banner[]>> {
    return this.http.get<ApiResponse<Banner[]>>(this.apiUrl);
  }

  createBanner(formData: FormData): Observable<ApiResponse<Banner>> {
    return this.http.post<ApiResponse<Banner>>(`${this.apiUrl}/formdata`, formData);
  }

  updateBanner(id: string, banner: any): Observable<ApiResponse<Banner>> {
    return this.http.put<ApiResponse<Banner>>(`${this.apiUrl}/${id}`, banner);
  }

  deleteBanner(id: string): Observable<ApiResponse<string>> {
    return this.http.delete<ApiResponse<string>>(`${this.apiUrl}/${id}`);
  }
}
