import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http'; // Import HttpHeaders
import { Observable } from 'rxjs';
import { Order, OrderStatus, ShippingStatus } from '../model/order.model';
import { ApiResponse } from './sale.service'; // Assuming a common response wrapper
import { environment } from '../../enviroment/enviroment';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private apiUrl = `${environment.apiBaseUrl}/api/orders`; // Adjusted to use environment variable

  constructor(private http: HttpClient) { }

  // Fetch all orders for admin
  getAllOrders(): Observable<ApiResponse<Order[]>> {
    return this.http.get<ApiResponse<Order[]>>(`${this.apiUrl}/all`);
  }

  // Get a single order by ID
  getOrderById(id: number): Observable<ApiResponse<Order>> {
    return this.http.get<ApiResponse<Order>>(`${this.apiUrl}/${id}`);
  }

  // Update order status
  updateOrderStatus(id: string, status: OrderStatus): Observable<ApiResponse<Order>> {
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain' });
    return this.http.patch<ApiResponse<Order>>(`${this.apiUrl}/${id}`, status, { headers });
  }

  updateShippingStatus(id: string, status: ShippingStatus): Observable<ApiResponse<Order>> {
    const headers = new HttpHeaders({ 'Content-Type': 'text/plain' });
    return this.http.patch<ApiResponse<Order>>(`${this.apiUrl}/${id}/shipping`, status, { headers });
  }
}
