import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Store } from '../../model/store.model';
import { StoreService } from '../../services/store.service';
import { ApiResponse } from '../../services/sale.service';
import { SearchableDropdownComponent } from '../../shared/searchable-dropdown/searchable-dropdown.component';

@Component({
  selector: 'app-stores',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule, SearchableDropdownComponent],
  templateUrl: './stores.component.html',
  styleUrls: ['./stores.component.scss']
})
export class StoresComponent implements OnInit {
  stores: Store[] = [];
  selectedStore: Partial<Store> = {};
  showForm = false;

  // Properties for GHN Integration (used by both forms now)
  showGhnForm = false;
  ghnProvinces: any[] = [];
  ghnDistricts: any[] = [];
  ghnWards: any[] = [];
  ghnStoresResult: any[] = [];
  ghnSelectedProvinceId: number | null = null;
  ghnSelectedDistrictId: number | null = null;
  ghnSelectedWardCode: string | null = null;
  
  // Store selected names for building location string in manual form
  selectedGhnProvinceName: string | null = null;
  selectedGhnDistrictName: string | null = null;
  selectedGhnWardName: string | null = null;

  searchedGhn = false;
  provinceSearchFields = ['ProvinceName', 'NameExtension'];
  districtSearchFields = ['DistrictName', 'NameExtension'];
  wardSearchFields = ['WardName', 'NameExtension'];

  private ghnApiBase = 'https://dev-online-gateway.ghn.vn/shiip/public-api';
  private ghnToken = '51851251-ccca-11f0-b989-ea7e29c7fb39';

  constructor(
    private storeService: StoreService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.loadStores();
  }

  loadStores(): void {
    this.storeService.getAll().subscribe((response: ApiResponse<Store[]>) => {
      this.stores = response.data;
    });
  }

  showAddForm(): void {
    this.resetForm();
    this.showForm = true;
    this.loadGhnProvinces(); // Load provinces for the manual form as well
  }

  selectStore(store: Store): void {
    this.resetForm();
    this.selectedStore = { ...store };
    this.showForm = true;
    this.loadGhnProvinces();
    // Note: Pre-filling location dropdowns for editing requires more complex logic
    // to map the stored location string back to province/district/commune codes.
    // This is not implemented here for brevity.
  }

  resetForm(): void {
    this.selectedStore = { active: true };
    this.showForm = false;
    this.showGhnForm = false;

    // Reset GHN-related state for both forms
    this.ghnProvinces = [];
    this.ghnDistricts = [];
    this.ghnWards = [];
    this.ghnSelectedProvinceId = null;
    this.ghnSelectedDistrictId = null;
    this.ghnSelectedWardCode = null;
    this.selectedGhnProvinceName = null;
    this.selectedGhnDistrictName = null;
    this.selectedGhnWardName = null;
    
    // Reset GHN search specific state
    this.ghnStoresResult = [];
    this.searchedGhn = false;
  }

  saveStore(): void {
    // Check for required location fields first
    if (!this.selectedStore.name || !this.ghnSelectedProvinceId || !this.ghnSelectedDistrictId || !this.ghnSelectedWardCode) {
      alert('Vui lòng điền đầy đủ Tên cửa hàng, Tỉnh/Thành phố, Quận/Huyện và Phường/Xã.');
      return; // Stop the save process
    }

    // Construct location string from selected GHN names
    const locationParts = [];
    if (this.selectedStore.addressDetail) {
      locationParts.push(this.selectedStore.addressDetail);
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

    const storeDataToSend = {
        name: this.selectedStore.name,
        phoneNumber: this.selectedStore.phoneNumber,
        active: this.selectedStore.active,
        location: locationString,
        provinceCode: this.ghnSelectedProvinceId,
        districtCode: this.ghnSelectedDistrictId,
        wardCode: wardCodeInt,
    };


    if (this.selectedStore.id) {
        this.storeService.update(this.selectedStore.id, storeDataToSend)
          .subscribe({
            next: () => {
              this.loadStores();
              this.resetForm();
            },
            error: (err) => console.error('Lỗi khi cập nhật cửa hàng:', err)
          });
      } else {
        this.storeService.create(storeDataToSend)
          .subscribe({
            next: () => {
              this.loadStores();
              this.resetForm();
            },
            error: (err) => console.error('Lỗi khi tạo cửa hàng:', err)
          });
      }
  }

  deleteStore(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa cửa hàng này không?')) {
      this.storeService.delete(id).subscribe(() => {
        this.loadStores();
      });
    }
  }

  // --- GHN Integration Methods (now used by both forms) ---

  showGhnAddForm(): void {
    this.resetForm();
    this.showGhnForm = true;
    this.loadGhnProvinces();
  }

  hideGhnForm(): void {
    this.resetForm();
  }

  loadGhnProvinces(): void {
    const headers = new HttpHeaders({ 'token': this.ghnToken });
    this.http.get<any>(`${this.ghnApiBase}/master-data/province`, { headers }).subscribe(res => {
      if (res.code === 200) {
        this.ghnProvinces = res.data;
      } else {
        console.error('Lỗi khi tải danh sách tỉnh/thành từ GHN:', res.message);
        alert('Không thể tải danh sách tỉnh/thành từ Giao Hàng Nhanh. Vui lòng kiểm tra lại token API.');
      }
    });
  }
  
  onGhnProvinceSelect(province: any): void {
    // Clear downstream selections
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

  loadGhnDistricts(): void {
    if (!this.ghnSelectedProvinceId) return;

    const headers = new HttpHeaders({ 'token': this.ghnToken });
    const params = { province_id: this.ghnSelectedProvinceId.toString() };
    this.http.get<any>(`${this.ghnApiBase}/master-data/district`, { headers, params }).subscribe(res => {
      if (res.code === 200) {
        this.ghnDistricts = res.data;
      } else {
        console.error('Lỗi khi tải danh sách quận/huyện từ GHN:', res.message);
      }
    });
  }

  onGhnDistrictSelect(district: any): void {
    // Clear downstream selections
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

  loadGhnWards(): void {
    if (!this.ghnSelectedDistrictId) return;

    const headers = new HttpHeaders({ 'token': this.ghnToken });
    const params = { district_id: this.ghnSelectedDistrictId.toString() };
    this.http.get<any>(`${this.ghnApiBase}/master-data/ward`, { headers, params }).subscribe(res => {
      if (res.code === 200) {
        this.ghnWards = res.data;
      } else {
        console.error('Lỗi khi tải danh sách phường/xã từ GHN:', res.message);
      }
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

  searchGhnStores(): void {
    if (!this.ghnSelectedDistrictId) {
      alert('Vui lòng chọn Tỉnh/Thành và Quận/Huyện.');
      return;
    }
    this.searchedGhn = true;
    this.ghnStoresResult = [];
    const headers = new HttpHeaders({ 'token': this.ghnToken });
    
    let params: any = { 
      district_id: this.ghnSelectedDistrictId.toString(), 
      limit: 50 
    };

    if (this.ghnSelectedWardCode) {
      params.ward_code = this.ghnSelectedWardCode;
    }

    this.http.get<any>(`${this.ghnApiBase}/v2/station/get`, { headers, params }).subscribe({
      next: res => {
        if (res.code === 200) {
          this.ghnStoresResult = res.data;
        } else {
          console.error('Lỗi khi tìm kiếm bưu cục GHN:', res.message);
          alert('Lỗi khi tìm kiếm bưu cục GHN: ' + res.message);
        }
      },
      error: err => {
        console.error('Lỗi HTTP khi tìm kiếm bưu cục GHN:', err);
        alert('Đã xảy ra lỗi khi kết nối tới API của Giao Hàng Nhanh.');
      }
    });
  }

  addGhnStore(ghnStore: any): void {
    if (!ghnStore) {
      return;
    }

    const newStore: Partial<Store> = {
      name: ghnStore.name,
      location: ghnStore.address,
      phoneNumber: ghnStore.phone,
      active: true,
    };

    if (confirm(`Bạn có chắc muốn thêm bưu cục "${newStore.name}" vào hệ thống không?`)) {
      this.storeService.create(newStore)
        .subscribe({
          next: () => {
            alert('Thêm cửa hàng thành công!');
            this.loadStores();
            this.resetForm();
          },
          error: (err) => {
            console.error('Lỗi khi tạo cửa hàng từ dữ liệu GHN:', err);
            alert('Đã xảy ra lỗi khi thêm cửa hàng. Vui lòng thử lại.');
          }
        });
    }
  }
}
