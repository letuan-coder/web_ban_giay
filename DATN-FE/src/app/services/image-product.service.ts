import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class ImageProductService {
  private baseUrl = environment.apiBaseUrl+'/api/images';

  constructor(private http: HttpClient) { }

  deleteImage(id: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${id}`);
  }
}
