import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { Store } from '../../model/store.model';
import { StoreService } from '../../services/store.service';
import { ApiResponse } from '../../services/sale.service';
import { LocationService } from '../../services/location.service';
import { Province, District, Commune } from '../../model/location.model';
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
  provinces: Province[] = [];
  districts: District[] = [];
  communes: Commune[] = [];

  selectedProvinceCode: string | null = null;
  selectedDistrictCode: string | null = null;
  selectedCommuneCode: string | null = null;

  // Properties for GHN Integration
  showGhnForm = false;
  ghnProvinces: any[] = [];
  ghnDistricts: any[] = [];
  ghnWards: any[] = [];
  ghnStoresResult: any[] = [];
  ghnSelectedProvinceId: number | null = null;
  ghnSelectedDistrictId: number | null = null;
  ghnSelectedWardCode: string | null = null;
  searchedGhn = false;
  provinceSearchFields = ['ProvinceName', 'NameExtension'];
  districtSearchFields = ['DistrictName', 'NameExtension'];
  wardSearchFields = ['WardName', 'NameExtension'];

  private ghnApiBase = 'https://dev-online-gateway.ghn.vn/shiip/public-api';
  private ghnToken = '51851251-ccca-11f0-b989-ea7e29c7fb39';

  constructor(
    private storeService: StoreService,
    private locationService: LocationService,
    private http: HttpClient
  ) { }

  ngOnInit(): void {
    this.loadStores();
    this.loadInitialLocations();
  }

  loadInitialLocations(): void {
    this.locationService.getProvinces().subscribe(res => this.provinces = res.data);
  }

  onProvinceChange(): void {
    this.selectedDistrictCode = null;
    this.selectedCommuneCode = null;
    this.districts = [];
    this.communes = [];

    if (this.selectedProvinceCode) {
      this.locationService.getDistricts(this.selectedProvinceCode)
        .subscribe(res => {
          this.districts = res.data;
        });
    }
  }

  onDistrictChange(): void {
    this.selectedCommuneCode = null;
    this.communes = [];

    if (this.selectedDistrictCode) {
      this.locationService.getCommunes(this.selectedDistrictCode)
        .subscribe(res => {
          this.communes = res.data;
        });
    }
  }

  loadStores(): void {
    this.storeService.getAll().subscribe((response: ApiResponse<Store[]>) => {
      this.stores = response.data;
    });
  }

  showAddForm(): void {
    this.resetForm();
    this.showForm = true;
  }

  selectStore(store: Store): void {
    this.resetForm();
    this.selectedStore = { ...store };
    this.showForm = true;
    // Note: Pre-filling location dropdowns for editing requires more complex logic
    // to map the stored location string back to province/district/commune codes.
    // This is not implemented here for brevity.
  }

  resetForm(): void {
    // Reset manual form state
    this.selectedStore = { active: true };
    this.showForm = false;
    this.selectedProvinceCode = null;
    this.selectedDistrictCode = null;
    this.selectedCommuneCode = null;
    this.districts = [];
    this.communes = [];

    // Reset GHN form state
    this.showGhnForm = false;
    this.ghnSelectedProvinceId = null;
    this.ghnSelectedDistrictId = null;
    this.ghnSelectedWardCode = null;
    this.ghnDistricts = [];
    this.ghnWards = [];
    this.ghnStoresResult = [];
    this.searchedGhn = false;
  }

  saveStore(): void {
    // Check for required location fields first
    if (!this.selectedProvinceCode || !this.selectedDistrictCode || !this.selectedCommuneCode) {
      alert('Vui lòng chọn đầy đủ Tỉnh/Thành phố, Quận/Huyện và Phường/Xã.');
      return; // Stop the save process
    }

    // Construct location string
    const province = this.provinces.find(p => p.code === this.selectedProvinceCode);
    const district = this.districts.find(d => d.code === this.selectedDistrictCode);
    const commune = this.communes.find(c => c.code === this.selectedCommuneCode);

    const locationParts = [];
    if (this.selectedStore.addressDetail) {
      locationParts.push(this.selectedStore.addressDetail);
    }
    if (commune) {
      locationParts.push(commune.nameWithType);
    }
    if (district) {
      locationParts.push(district.nameWithType);
    }
    if (province) {
      locationParts.push(province.nameWithType);
    }

    this.selectedStore.location = locationParts.join(', ');

    if (this.selectedStore.name && this.selectedStore.location) {
      const { addressDetail, ...storeDataToSend } = this.selectedStore;

      if (storeDataToSend.id) {
        this.storeService.update(storeDataToSend.id, storeDataToSend)
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
    } else {
      alert('Vui lòng điền đầy đủ thông tin Tên cửa hàng và Địa chỉ.');
    }
  }

  deleteStore(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa cửa hàng này không?')) {
      this.storeService.delete(id).subscribe(() => {
        this.loadStores();
      });
    }
  }

  // --- GHN Integration Methods ---

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
    this.ghnSelectedDistrictId = null;
    this.ghnSelectedWardCode = null;
    this.ghnDistricts = [];
    this.ghnWards = [];
    
    if (province) {
      this.ghnSelectedProvinceId = province.ProvinceID;
      this.loadGhnDistricts();
    } else {
      this.ghnSelectedProvinceId = null;
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
    this.ghnSelectedWardCode = null;
    this.ghnWards = [];

    if (district) {
      this.ghnSelectedDistrictId = district.DistrictID;
      this.loadGhnWards();
    } else {
      this.ghnSelectedDistrictId = null;
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
    } else {
      this.ghnSelectedWardCode = null;
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
