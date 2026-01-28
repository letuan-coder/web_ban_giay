export interface SupplierResponse {
  id: number;
  name: string;
  taxCode: string;
  email: string;
  phoneNumber: string;
  supplierAddress: string;
  status: SupplierStatus;
}

export enum SupplierStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}