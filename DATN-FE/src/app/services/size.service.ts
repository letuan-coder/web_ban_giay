import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment';

export interface Size {
  code: string;
  name: number;
}

@Injectable({
  providedIn: 'root'
})
export class SizeService {
  private apiUrl = environment.apiBaseUrl+'/api/sizes';

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }
}
