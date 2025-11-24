import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Store } from '../../model/store.model';
import { StoreService } from '../../services/store.service';
import { ApiResponse } from '../../services/sale.service';
import { LocationService } from '../../services/location.service';
import { Province, District, Commune } from '../../model/location.model';

@Component({
  selector: 'app-stores',
  standalone: true,
  imports: [CommonModule, FormsModule],
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

  constructor(
    private storeService: StoreService,
    private locationService: LocationService
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
    this.selectedStore = { active: true, ...store };
    this.showForm = true;
    this.selectedProvinceCode = null;
    this.selectedDistrictCode = null;
    this.selectedCommuneCode = null;
    this.districts = [];
    this.communes = [];
  }

  resetForm(): void {
    this.selectedStore = { active: true }; 
    this.showForm = false;
    this.selectedProvinceCode = null;
    this.selectedDistrictCode = null;
    this.selectedCommuneCode = null;
    this.districts = [];
    this.communes = [];
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
}
