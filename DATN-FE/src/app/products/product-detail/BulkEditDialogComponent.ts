import { Component, Inject, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../enviroment/enviroment';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import { CommonModule } from '@angular/common';
import { from, concatMap, toArray } from 'rxjs';
import { NgxCurrencyDirective } from 'ngx-currency';
import { ProductVariantService } from '../../services/product-variant.service';

@Component({
  selector: 'app-bulk-edit',
  standalone: true,
  imports: [FormsModule, CommonModule, NgxCurrencyDirective],
  templateUrl: './bulk.edit.component.html',
})
export class BulkEditComponent implements OnInit {

  variants: any[] = [];

  constructor(
    private productVariantService: ProductVariantService,
    private http: HttpClient,
    private toastr: ToastrService,
    @Inject(MAT_DIALOG_DATA) public data: string[]
  ) {}

  ngOnInit() {
    this.loadVariants();
  }

  loadVariants() {
    this.variants = [];
    const requests = this.data.map(id =>
      this.http.get(`${environment.apiBaseUrl}/api/product-variants/${id}`)
    );

    from(requests).pipe(
      concatMap(req => req)
    ).subscribe({
      next: (result: any) => {
        const v = result.data;
        this.variants.push({
          ...v,
          price: v.price ?? 0,
          stock: v.stock ?? 0,
          sku: v.sku ?? '',
          isAvailable: v.isAvailable ?? 'AVAILABLE',
          discountPrice: v.discountPrice ?? 0,
          size: v.size ?? { name: '' },
          colors: v.colors ?? null,
          isEdited: false
        });
      },
      error: (err) => {
        console.error('Lỗi khi load variant', err);
        this.toastr.error('Không thể tải dữ liệu variant');
      }
    });
  }

  // Gọi khi input/select thay đổi để đánh dấu variant đã sửa
  onFieldChange(variant: any) {
    variant.isEdited = true;
  }

  saveAllChanges() {
    const variantsToUpdate = this.variants.filter(v => v.isEdited);

    if (variantsToUpdate.length === 0) {
      this.toastr.info('Không có thay đổi nào để lưu.');
      return;
    }

    from(variantsToUpdate).pipe(
      concatMap(variant => {
        const payload: any = {};
        if (variant.price !== undefined) payload.price = variant.price;
        if (variant.stock !== undefined) payload.stock = variant.stock;
        if (variant.isAvailable !== undefined) payload.isAvailable = variant.isAvailable;
        if (variant.discountPrice !== undefined) payload.discountPrice = variant.discountPrice;
        if (variant.size !== undefined) payload.sizeName = variant.size.name;

        // Gọi API để cập nhật từng biến thể một
        return this.http.patch(`${environment.apiBaseUrl}/api/product-variants/${variant.id}`, payload);
      }),
      toArray() // Chờ tất cả các request hoàn thành và gom kết quả lại
    ).subscribe({
      next: (responses) => {
        this.toastr.success(`Đã cập nhật thành công ${responses.length} biến thể.`);
        this.loadVariants(); // Tải lại dữ liệu để hiển thị trạng thái mới nhất
      },
      error: (err) => {
        console.error('Lỗi khi cập nhật tuần tự', err);
        this.toastr.error('Có lỗi xảy ra trong quá trình cập nhật. Một số thay đổi có thể chưa được lưu.');
        this.loadVariants(); // Tải lại dữ liệu để thấy được những gì đã hoặc chưa được cập nhật
      }
    });
  }

}
