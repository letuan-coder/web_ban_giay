import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApiResponse } from '../../services/sale.service';
import { Order, ORDER_STATUSES, OrderStatus, SHIPPING_STATUS, ShippingStatus, PAYMENT_STATUS, PaymentStatus } from '../../model/order.model';
import { OrderService } from '../../services/order.service';

// Helper to map status to a display text and CSS class
const STATUS_MAP: { [key in OrderStatus]: { text: string; class: string } } = {
  PENDING: { text: 'Đang chờ xác nhận', class: 'bg-secondary' },
  CONFIRMED: { text: 'Đã xác nhận', class: 'bg-primary' },
  CANCELLED: { text: 'Đã hủy', class: 'bg-danger' },
  COMPLETED: { text: 'Hoàn thành', class: 'bg-success' }
};

const SHIPPING_STATUS_MAP: { [key in ShippingStatus]: { text: string; class: string } } = {
  PICKING: { text: 'Đang lấy hàng', class: 'bg-info' },
  DELEVERING: { text: 'Đang giao hàng', class: 'bg-primary' },
  DELEVERED: { text: 'Đã giao', class: 'bg-success' },
  RETURED: { text: 'Đã hoàn trả', class: 'bg-warning text-dark' }
};

const PAYMENT_STATUS_MAP: { [key in PaymentStatus]: { text: string; class: string } } = {
  UNPAID: { text: 'Chưa thanh toán', class: 'bg-warning text-dark' },
  PAID: { text: 'Đã thanh toán', class: 'bg-success' }
};


// Create a specific type for display purposes where created_At is a Date object
interface DisplayOrder extends Omit<Order, 'created_At'> {
  created_At: Date;
}

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.scss']
})
export class OrdersComponent implements OnInit {
  orders: DisplayOrder[] = [];
  loading = false;
  errorMessage = '';
  public selectedOrderForDetail: DisplayOrder | null = null;

  // Expose constants to the template
  public orderStatuses = ORDER_STATUSES;
  public shippingStatuses = SHIPPING_STATUS;
  public paymentStatuses = PAYMENT_STATUS;

  public statusMap = STATUS_MAP;
  public shippingStatusMap = SHIPPING_STATUS_MAP;
  public paymentStatusMap = PAYMENT_STATUS_MAP;
  
  constructor(private orderService: OrderService) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    this.loading = true;
    this.errorMessage = '';
    this.orderService.getAllOrders().subscribe({
      next: (response: ApiResponse<Order[]>) => {
        this.orders = response.data.map(order => ({
          ...order,
          items: order.items || [],
          created_At: this.parseDate(order.created_At)
        }))
          .sort((a, b) => b.created_At.getTime() - a.created_At.getTime());

        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Tải danh sách đơn hàng thất bại. Vui lòng thử lại.';
        console.error(err);
        this.loading = false;
      }
    });
  }

  parseDate(dateStr: string): Date {
    if (!dateStr || !dateStr.includes('/')) {
      return new Date(); // Return current date or some default for invalid input
    }
    const [day, month, year] = dateStr.split('/').map(Number);
    return new Date(year, month - 1, day);
  }

  showOrderDetails(order: DisplayOrder): void {
    this.selectedOrderForDetail = order;
  }

  closeDetailsModal(): void {
    this.selectedOrderForDetail = null;
  }

  onStatusChange(orderId: number, event: Event): void {
    const newStatus = (event.target as HTMLSelectElement).value as OrderStatus;
    this.updateOrderStatus(orderId, newStatus);
  }

  // onShippingStatusChange(orderId: number, event: Event): void {
  //   const newStatus = (event.target as HTMLSelectElement).value as ShippingStatus;
  //   this.updateShippingStatus(orderId, newStatus);
  // }

  updateOrderStatus(orderId: number, newStatus: OrderStatus): void {
    const order = this.orders.find(o => o.id === orderId);
    if (order) {
      const oldStatus = order.orderStatus;
      order.orderStatus = newStatus; // Optimistic update

      this.orderService.updateOrderStatus(orderId, newStatus).subscribe({
        error: (err) => {
          order.orderStatus = oldStatus; // Revert on error
          this.errorMessage = `Cập nhật trạng thái cho đơn hàng #${orderId} thất bại.`;
          console.error(err);
        }
      });
    }
  }

  
  // Utility functions to be used in the template
  getStatusText(status: OrderStatus): string {
    return this.statusMap[status]?.text || status;
  }

  getStatusClass(status: OrderStatus): string {
    return this.statusMap[status]?.class || 'bg-light';
  }

  getShippingStatusText(status: ShippingStatus): string {
    return this.shippingStatusMap[status]?.text || status;
  }

  getShippingStatusClass(status: ShippingStatus): string {
    return this.shippingStatusMap[status]?.class || 'bg-light';
  }

  getPaymentStatusText(status: PaymentStatus): string {
    return this.paymentStatusMap[status]?.text || status;
  }

  getPaymentStatusClass(status: PaymentStatus): string {
    return this.paymentStatusMap[status]?.class || 'bg-light';
  }
}