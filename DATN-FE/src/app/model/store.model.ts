export interface Store {
  id: number;
  name: string;
  location: string;
  addressDetail?: string;
  phoneNumber: string;
  active?: boolean; // Added this field
}
