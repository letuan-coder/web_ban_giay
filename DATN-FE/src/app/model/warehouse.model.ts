export interface Warehouse {
  warehouseCode:string;
  name: string;
  location?: string;
  capacity?: number | null;
  addressDetail?: string; // For building the location string
  provinceCode?:number;
  districtCode?:number;
  wardCode?:number;
}
