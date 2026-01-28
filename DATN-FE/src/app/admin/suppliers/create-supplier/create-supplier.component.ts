import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SupplierService, SupplierRequest, SupplierResponse } from '../../../services/supplier.service';
import { Router } from '@angular/router';
import { HttpClient, HttpClientModule, HttpHeaders } from '@angular/common/http';
import { SearchableDropdownComponent } from '../../../shared/searchable-dropdown/searchable-dropdown.component';

export enum SupplierStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  SUSPENDED = 'SUSPENDED'
}

@Component({
  selector: 'app-create-supplier',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    SearchableDropdownComponent
  ],
  templateUrl: './create-supplier.component.html',
  styleUrls: ['./create-supplier.component.scss']
})
export class CreateSupplierComponent implements OnInit {
  supplierForm!: FormGroup;
  loading = false;
  message: string | null = null;
  isSuccess = false;
  submitted = false;
  supplierStatuses = Object.values(SupplierStatus);

  // GHN Integration Properties
  private ghnApiBase = 'https://dev-online-gateway.ghn.vn/shiip/public-api';
  private ghnToken = '51851251-ccca-11f0-b989-ea7e29c7fb39'; 

  ghnProvinces: any[] = [];
  ghnDistricts: any[] = [];
  ghnCommunes: any[] = [];

  selectedProvince: any = null;
  selectedDistrict: any = null;
  selectedCommune: any = null;

  provinceSearchFields = ['ProvinceName', 'NameExtension'];
  districtSearchFields = ['DistrictName', 'NameExtension'];
  communeSearchFields = ['WardName', 'NameExtension'];

  constructor(
    private fb: FormBuilder,
    private supplierService: SupplierService,
    private http: HttpClient,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.supplierForm = this.fb.group({
      name: ['', Validators.required],
      taxCode: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^(0|\+84)[3-9][0-9]{8}$/)]],
      streetAddress: ['', Validators.required],
      status: [SupplierStatus.ACTIVE, Validators.required]
    });

    this.loadGhnProvinces();
  }

  get f() { return this.supplierForm.controls; }

  loadGhnProvinces(): void {
    const headers = new HttpHeaders({ 'token': this.ghnToken });
    this.http.get<any>(`${this.ghnApiBase}/master-data/province`, { headers }).subscribe(res => {
      if (res.code === 200) {
        this.ghnProvinces = res.data;
      } else {
        this.message = 'Không thể tải danh sách Tỉnh/Thành phố. Vui lòng thử lại sau.';
        this.isSuccess = false;
      }
    });
  }

  onGhnProvinceSelect(province: any): void {
    this.selectedProvince = province;
    this.selectedDistrict = null;
    this.selectedCommune = null;
    
    this.ghnDistricts = [];
    this.ghnCommunes = [];
    
    if (province) {
      this.loadGhnDistricts(province.ProvinceID);
    }
  }

  loadGhnDistricts(provinceId: number): void {
    const headers = new HttpHeaders({ 'token': this.ghnToken });
    const params = { province_id: provinceId.toString() };
    this.http.get<any>(`${this.ghnApiBase}/master-data/district`, { headers, params }).subscribe(res => {
      if (res.code === 200) { this.ghnDistricts = res.data; }
    });
  }

  onGhnDistrictSelect(district: any): void {
    this.selectedDistrict = district;
    this.selectedCommune = null;
    this.ghnCommunes = [];

    if (district) {
      this.loadGhnCommunes(district.DistrictID);
    }
  }

  loadGhnCommunes(districtId: number): void {
    const headers = new HttpHeaders({ 'token': this.ghnToken });
    const params = { district_id: districtId.toString() };
    this.http.get<any>(`${this.ghnApiBase}/master-data/ward`, { headers, params }).subscribe(res => {
      if (res.code === 200) { this.ghnCommunes = res.data; }
    });
  }

  onGhnCommuneSelect(commune: any): void {
    this.selectedCommune = commune;
  }

  onSubmit(): void {
    this.submitted = true;
    if (this.supplierForm.invalid || !this.selectedProvince || !this.selectedDistrict || !this.selectedCommune) {
      this.message = 'Vui lòng điền đầy đủ và chính xác các thông tin.';
      this.isSuccess = false;
      this.supplierForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.message = null;
    const formValues = this.supplierForm.value;
    const addressParts = [
      formValues.streetAddress,
      this.selectedCommune?.WardName,
      this.selectedDistrict?.DistrictName,
      this.selectedProvince?.ProvinceName
    ].filter(Boolean);
    const fullAddress = addressParts.join(', ');

    const supplierRequest: SupplierRequest = {
      name: formValues.name,
      taxCode: formValues.taxCode,
      email: formValues.email,
      phoneNumber: formValues.phoneNumber,
      supplierAddress: fullAddress,
      status: formValues.status
    };

    this.supplierService.createSupplier(supplierRequest).subscribe({
      next: () => {
        this.loading = false;
        this.isSuccess = true;
        this.message = 'Tạo nhà cung cấp thành công!';
        this.supplierForm.reset({ status: SupplierStatus.ACTIVE });
        this.submitted = false;
        this.selectedProvince = null;
        this.selectedDistrict = null;
        this.selectedCommune = null;
        this.ghnDistricts = [];
        this.ghnCommunes = [];
      },
      error: (err) => {
        this.loading = false;
        this.isSuccess = false;
        this.message = err.error?.message || 'Có lỗi xảy ra khi tạo nhà cung cấp.';
      }
    });
  }
}