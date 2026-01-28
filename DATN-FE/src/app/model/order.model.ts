export interface OrderItem {
  id: number;
  productName: string;
  sku: string;
  quantity: number;
  price: number;
}

export interface Order {
  id: string;
  orderCode:string;
  userName: string;
  phoneNumber: string;
  userAddress: string;
  receiverName:string;
  totalPrice: number;
  orderStatus: OrderStatus;
  shippingStatus: ShippingStatus;
  created_At: string;
  paymentMethod: PaymentStatus;
  items: OrderItem[];
  note?: string;
}
export type PaymentStatus =

  | 'UNPAID'
  | 'PAID'


export const PAYMENT_STATUS: PaymentStatus[] = [
  'PAID',
  'UNPAID'
];

export type ShippingStatus =
  | 'DELEVERED'
  | 'DELEVERING'
  | 'PICKING'
  | 'RETURED';

export const SHIPPING_STATUS: ShippingStatus[] = [
   'DELEVERED',
   'DELEVERING',
   'PICKING',
  'RETURED'
];


export type OrderStatus =

  | 'PENDING'
  | 'CONFIRMED'
  | 'CANCELLED'
  | 'COMPLETED';

export const ORDER_STATUSES: OrderStatus[] = [

  'PENDING',
  'CONFIRMED',
  'CANCELLED',
  'COMPLETED'
];
