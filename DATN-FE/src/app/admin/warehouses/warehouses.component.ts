import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { WarehouseService } from '../../services/warehouse.service';
import { ApiResponse } from '../../services/sale.service';
import { Warehouse } from '../../model/warehouse.model';
import { SearchableDropdownComponent } from '../../shared/searchable-dropdown/searchable-dropdown.component';
import { NgxCurrencyDirective } from 'ngx-currency';

@Component({
  selector: 'app-warehouses',
  standalone: true,
  imports: [CommonModule, FormsModule,
    HttpClientModule, SearchableDropdownComponent, NgxCurrencyDirective],
  templateUrl: './warehouses.component.html',
  styleUrls: ['./warehouses.component.scss']
})
export class WarehousesComponent implements OnInit {
  warehouses: Warehouse[] = [];
  selectedWarehouse: Partial<Warehouse> = {};
  showForm = false;
  warehouseCapacityRaw: string = '';
  // Properties for GHN Integration
  ghnProvinces: any[] = [];
  ghnDistricts: any[] = [];
  ghnWards: any[] = [];

  ghnSelectedProvinceId: number | null = null;
  ghnSelectedDistrictId: number | null = null;
  ghnSelectedWardCode: string | null = null;

  selectedGhnProvinceName: string | null = null;
  selectedGhnDistrictName: string | null = null;
  selectedGhnWardName: string | null = null;

  provinceSearchFields = ['ProvinceName', 'NameExtension'];
  districtSearchFields = ['DistrictName', 'NameExtension'];
  wardSearchFields = ['WardName', 'NameExtension'];

  private ghnApiBase = 'https://dev-online-gateway.ghn.vn/shiip/public-api';
  private ghnToken = '51851251-ccca-11f0-b989-ea7e29c7fb39'; // Replace with your actual GHN Token

  constructor(
    private warehouseService: WarehouseService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.loadWarehouses();
  }

  loadWarehouses(): void {
    this.warehouseService.getAll().subscribe((response: ApiResponse<Warehouse[]>) => {
      this.warehouses = response.data;
    });
  }

  showAddForm(): void {
    this.resetForm();
    this.showForm = true;
    this.loadGhnProvinces();
  }

  selectWarehouse(warehouse: Warehouse): void {
    if (!warehouse.warehouseCode) return;
    this.resetForm();

    this.warehouseService.getByCode(warehouse.warehouseCode).subscribe(response => {
      const detailedWarehouse = response.data;
      if (!detailedWarehouse) {
        alert("Không tìm thấy thông tin chi tiết cho kho.");
        return;
      }

      this.ghnSelectedProvinceId = detailedWarehouse.provinceCode;
      this.ghnSelectedDistrictId = detailedWarehouse.districtCode;
      this.ghnSelectedWardCode = detailedWarehouse.wardCode?.toString();

      this.showForm = true;
      this.loadGhnProvinces().then(() => {
        if (this.ghnSelectedProvinceId) {
          this.loadGhnDistricts().then(() => {
            if (this.ghnSelectedDistrictId) {
              this.loadGhnWards();
            }
          });
        }
      });
    });
  }

  resetForm(): void {
    this.selectedWarehouse = {};
    this.showForm = false;
    this.ghnProvinces = [];
    this.ghnDistricts = [];
    this.ghnWards = [];
    this.ghnSelectedProvinceId = null;
    this.ghnSelectedDistrictId = null;
    this.ghnSelectedWardCode = null;
    this.selectedGhnProvinceName = null;
    this.selectedGhnDistrictName = null;
    this.selectedGhnWardName = null;
    this.warehouseCapacityRaw='';
  }
  onCapacityChange(value: string | number) {
    if (value != null) {
      // Nếu value là string với dấu ngăn cách hàng nghìn
      if (typeof value === 'string') {
        this.selectedWarehouse.capacity = Number(value.replace(/\./g, ''));
      } else {
        // Nếu value đã là number
        this.selectedWarehouse.capacity = value;
      }
    } else {
      this.selectedWarehouse.capacity = null;
    }
  }

  saveWarehouse(): void {
    if (!this.selectedWarehouse.name || !this.ghnSelectedProvinceId || !this.ghnSelectedDistrictId || !this.ghnSelectedWardCode) {
      alert('Vui lòng điền đầy đủ Tên kho và chọn Tỉnh/Thành phố, Quận/Huyện, Phường/Xã.');
      return;
    }

    const locationParts = [];
    if (this.selectedWarehouse.addressDetail) {
      locationParts.push(this.selectedWarehouse.addressDetail);
    }
    if (this.selectedGhnWardName) {
      locationParts.push(this.selectedGhnWardName);
    }
    if (this.selectedGhnDistrictName) {
      locationParts.push(this.selectedGhnDistrictName);
    }
    if (this.selectedGhnProvinceName) {
      locationParts.push(this.selectedGhnProvinceName);
    }
    const locationString = locationParts.join(', ');

    const wardCodeInt = parseInt(this.ghnSelectedWardCode, 10);
    if (isNaN(wardCodeInt)) {
      alert('Mã Phường/Xã không hợp lệ.');
      return;
    }

    const requestPayload = {
      name: this.selectedWarehouse.name,
      capacity: this.selectedWarehouse.capacity,
      isDefault: this.selectedWarehouse.isCentral || false,
      location: locationString,
      addressDetail: this.selectedWarehouse.addressDetail,
      provinceCode: this.ghnSelectedProvinceId,
      districtCode: this.ghnSelectedDistrictId,
      wardCode: wardCodeInt,
    };

    if (this.selectedWarehouse.warehouseCode) {
      // Update existing warehouse
      this.warehouseService.update(this.selectedWarehouse.warehouseCode, requestPayload)
        .subscribe({
          next: () => {
            this.loadWarehouses();
            this.resetForm();
          },
          error: (err) => console.error('Lỗi khi cập nhật kho:', err)
        });
    } else {
      // Create new warehouse
      this.warehouseService.create(requestPayload)
        .subscribe({
          next: () => {
            this.loadWarehouses();
            this.resetForm();
          },
          error: (err) => console.error('Lỗi khi tạo kho:', err)
        });
    }
  }

  deleteWarehouse(code: string): void {
    if (confirm('Bạn có chắc chắn muốn xóa kho này không?')) {
      this.warehouseService.delete(code).subscribe(() => {
        this.loadWarehouses();
      });
    }
  }

  setCentralWarehouse(id: string): void {
    if (confirm('Bạn có chắc chắn muốn đặt kho này làm kho tổng không? Chỉ có thể có một kho tổng.')) {
      this.warehouseService.setCentral(id, true).subscribe(() => {
        this.loadWarehouses();
      });
    }
  }

  // --- GHN Integration Methods ---

  loadGhnProvinces(): Promise<void> {
    return new Promise((resolve, reject) => {
      const headers = new HttpHeaders({ 'token': this.ghnToken });
      this.http.get<any>(`${this.ghnApiBase}/master-data/province`, { headers }).subscribe({
        next: res => {
          if (res.code === 200) {
            this.ghnProvinces = res.data;
            resolve();
          } else {
            console.error('Lỗi khi tải tỉnh/thành từ GHN:', res.message);
            alert('Không thể tải tỉnh/thành từ GHN.');
            reject(new Error(res.message));
          }
        },
        error: err => {
          console.error('Lỗi HTTP khi tải tỉnh/thành:', err);
          reject(err);
        }
      });
    });
  }

  onGhnProvinceSelect(province: any): void {
    this.ghnDistricts = [];
    this.ghnWards = [];
    this.ghnSelectedDistrictId = null;
    this.ghnSelectedWardCode = null;
    this.selectedGhnDistrictName = null;
    this.selectedGhnWardName = null;

    if (province) {
      this.ghnSelectedProvinceId = province.ProvinceID;
      this.selectedGhnProvinceName = province.ProvinceName;
      this.loadGhnDistricts();
    } else {
      this.ghnSelectedProvinceId = null;
      this.selectedGhnProvinceName = null;
    }
  }

  loadGhnDistricts(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.ghnSelectedProvinceId) {
        return resolve();
      }
      const headers = new HttpHeaders({ 'token': this.ghnToken });
      const params = { province_id: this.ghnSelectedProvinceId.toString() };
      this.http.get<any>(`${this.ghnApiBase}/master-data/district`, { headers, params }).subscribe({
        next: res => {
          if (res.code === 200) {
            this.ghnDistricts = res.data;
            resolve();
          } else {
            console.error('Lỗi khi tải quận/huyện từ GHN:', res.message);
            reject(new Error(res.message));
          }
        },
        error: err => {
          console.error('Lỗi HTTP khi tải quận/huyện:', err);
          reject(err);
        }
      });
    });
  }

  onGhnDistrictSelect(district: any): void {
    this.ghnWards = [];
    this.ghnSelectedWardCode = null;
    this.selectedGhnWardName = null;

    if (district) {
      this.ghnSelectedDistrictId = district.DistrictID;
      this.selectedGhnDistrictName = district.DistrictName;
      this.loadGhnWards();
    } else {
      this.ghnSelectedDistrictId = null;
      this.selectedGhnDistrictName = null;
    }
  }

  loadGhnWards(): Promise<void> {
    return new Promise((resolve, reject) => {
      if (!this.ghnSelectedDistrictId) {
        return resolve();
      }
      const headers = new HttpHeaders({ 'token': this.ghnToken });
      const params = { district_id: this.ghnSelectedDistrictId.toString() };
      this.http.get<any>(`${this.ghnApiBase}/master-data/ward`, { headers, params }).subscribe({
        next: res => {
          if (res.code === 200) {
            this.ghnWards = res.data;
            resolve();
          } else {
            console.error('Lỗi khi tải phường/xã từ GHN:', res.message);
            reject(new Error(res.message));
          }
        },
        error: err => {
          console.error('Lỗi HTTP khi tải phường/xã:', err);
          reject(err);
        }
      });
    });
  }

  onGhnWardSelect(ward: any): void {
    if (ward) {
      this.ghnSelectedWardCode = ward.WardCode;
      this.selectedGhnWardName = ward.WardName;
    } else {
      this.ghnSelectedWardCode = null;
      this.selectedGhnWardName = null;
    }
  }
}
