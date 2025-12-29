export interface Store {
  id: number;
  code: string;
  name: string;
  location: string;
  addressDetail?: string;
  phoneNumber: string;
  active?: boolean; // Added this field
}
