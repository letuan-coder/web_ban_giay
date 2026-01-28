import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../enviroment/enviroment'

export interface Color {
  code: string;
  name: string;
  hexCode: string;
}

@Injectable({
  providedIn: 'root'
})
export class ColorService {
  private apiUrl = environment.apiBaseUrl+'/api/colors';

  constructor(private http: HttpClient) { }

  getAll(): Observable<any> {
    return this.http.get<any>(this.apiUrl);
  }
}
