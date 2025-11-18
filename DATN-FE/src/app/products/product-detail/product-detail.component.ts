import { MatDialog } from '@angular/material/dialog';
import { BulkEditComponent } from './BulkEditDialogComponent'; // Import BulkEditComponent
import { ApplyPromotionDialogComponent } from './apply-promotion-dialog/apply-promotion-dialog.component'; // Import the new dialog
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgxCurrencyDirective } from 'ngx-currency';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Product } from '../../model/product.model';
import { Component, OnInit } from '@angular/core';
import { ProductService } from '../../services/product.service';
import { ImageProductService } from '../../services/image-product.service';
import { Color, ColorService } from '../../services/color.service';
import { Size, SizeService } from '../../services/size.service';
import { ProductColorService } from '../../services/product-color.service';
import { ProductVariantService } from '../../services/product-variant.service';
import { ColorVariantResponse, VariantResponse } from '../../model/variant.response.model';
import { forkJoin } from 'rxjs';
@Component({
  selector: 'app-product-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, NgxCurrencyDirective, RouterLink],
  templateUrl: './product-detail.component.html',
  styleUrls: ['./product-detail.component.scss']
})

export class ProductDetailComponent implements OnInit {

  product: Product | null = null;
  loading = false;
  error = '';
  selectedProductIds = new Set<string>();
  selectedImageIds = new Set<number>();
  showAddVariantForm = false;
  isNewColor: boolean = false;
  availableColors: Color[] = [];
  availableSizes: Size[] = [];

  selectedSizesGlobal: Set<string> = new Set();
  newVariantRequest = {
    selectedColorCode: null as string | null,
    newColorName: '',
    newColorCode: '',
    isAvailable: 'AVAILABLE',
    variantRequests: [{ sizes: [] as string[], price: 0, stock: 1 }],
    files: [] as File[]
  };

  constructor(
    private route: ActivatedRoute,
    private productService: ProductService,
    private imageProductService: ImageProductService,
    private colorService: ColorService,
    private sizeService: SizeService,
    private productColorService: ProductColorService,
    private router: Router,
    private productVariantService: ProductVariantService, // Inject ProductVariantService
    private dialog: MatDialog // Inject MatDialog
  ) { }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      const productId = params.get('id');
      if (productId) {
        this.loadProductDetails(productId);
      } else {
        this.error = 'Không tìm thấy ID sản phẩm.';
      }
    });
    this.loadInitialData();
  }

  loadInitialData(): void {
    this.colorService.getAll().subscribe(res => this.availableColors = res.data);
    this.sizeService.getAll().subscribe(res => this.availableSizes = res.data);
    this.availableSizes.sort((a, b) => {
      const aNum = parseInt(a.name.toString(), 10);
      const bNum = parseInt(b.name.toString(), 10);
      return (isNaN(bNum) ? 0 : bNum) - (isNaN(aNum) ? 0 : aNum);
    });
  }

  loadProductDetails(id: string): void {
    this.loading = true;
    this.error = '';
    this.productService.getById(id).subscribe({
      next: (res: any) => {
        this.product = res.data;
        this.loading = false;
      },
      error: (err) => {
        this.error = `Không thể tải thông tin chi tiết sản phẩm: ${err.message || err.error?.message || 'Lỗi không xác định'}`;
        this.loading = false;
        console.error(err);
      }
    });
  }
  sortColumn: string = '';
  sortDirection: 'asc' | 'desc' = 'asc';

  sortVariants(column: string) {
    if (!this.product?.colorResponses) return;

    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }

    this.product.colorResponses.forEach((colorVariant: ColorVariantResponse) => {
      colorVariant.variantResponses.sort((a: VariantResponse, b: VariantResponse) => {
        let valA: any;
        let valB: any;

        switch (column) {
          case 'sku': valA = a.sku; valB = b.sku; break;
          case 'size': valA = a.size?.name ?? 0; valB = b.size?.name ?? 0; break;
          case 'price': valA = a.price; valB = b.price; break;
          case 'stock': valA = a.stock; valB = b.stock; break;
          default: return 0;
        }

        if (typeof valA === 'string') {
          return this.sortDirection === 'asc' ? valA.localeCompare(valB) : valB.localeCompare(valA);
        } else {
          return this.sortDirection === 'asc' ? valA - valB : valB - valA;
        }
      });
    });
  }

  onImageSelectionChange(event: any, imageId: number): void {
    if (event.target.checked) {
      this.selectedImageIds.add(imageId);
    } else {
      this.selectedImageIds.delete(imageId);
    }
  }
  openBulkEdit() {
    this.dialog.open(BulkEditComponent, {
      data: Array.from(this.selectedProductIds)
    });
  }
  openApplyPromotionDialog(): void {
    const dialogRef = this.dialog.open(ApplyPromotionDialogComponent, {
      data: { productVariantIds: Array.from(this.selectedProductIds) }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        // If promotion was applied successfully, refresh product details
        const productId = this.route.snapshot.paramMap.get('id');
        if (productId) {
          this.loadProductDetails(productId);
        }
      }
    });
  }
  deleteVariant(variantId: string): void {
    if (confirm('Bạn có chắc chắn muốn xóa phiên bản sản phẩm này không?')) {
      this.loading = true;
      this.productVariantService.deleteVariant(variantId).subscribe({
        next: () => {
          const productId = this.route.snapshot.paramMap.get('id');
          if (productId) {
            this.loadProductDetails(productId);
          }
          this.loading = false;
        },
        error: (err) => {
          this.error = 'Lỗi khi xóa phiên bản sản phẩm.';
          this.loading = false;
          console.error(err);
        }
      });
    }
  }
  onSelectProduct(event: any, variantId: string): void {
    if (event.target.checked) {
      this.selectedProductIds.add(variantId);
    } else {
      this.selectedProductIds.delete(variantId);
    }
  }
  deleteSelectedImages(): void {
    if (this.selectedImageIds.size === 0) {
      return;
    }
    this.loading = true;
    const deleteRequests = Array.from(this.selectedImageIds).map(id => this.imageProductService.deleteImage(id));
    forkJoin(deleteRequests).subscribe({
      next: () => {
        this.selectedImageIds.clear();
        const productId = this.route.snapshot.paramMap.get('id');
        if (productId) {
          this.loadProductDetails(productId);
        }
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Đã xảy ra lỗi khi xóa ảnh.';
        this.error = err.error.message;
        this.loading = false;
        console.error(err);
      }
    });
  }

  deleteProductColor(id: string): void {
    if (confirm('Bạn có chắc chắn muốn xóa màu sắc này và tất cả các phiên bản liên quan không?')) {
      this.loading = true;
      this.productColorService.deleteProductColor(id).subscribe(
        {
          next: () => {
            const productId = this.route.snapshot.paramMap.get('id');
            if (productId) {
              this.loadProductDetails(productId);
            }
            this.loading = false;
          },
          error: (err) => {
            this.error = 'Lỗi khi xóa màu sắc.';
            this.loading = false;
            console.error(err);
          }
        });
    }
  }

  // --- Add Variant Form Methods ---
  toggleAddVariantForm(): void {
    this.showAddVariantForm = !this.showAddVariantForm;
    this.isNewColor = false; // Reset to existing color selection when form is toggled
    this.resetNewVariantForm();
  }

  addVariantGroup(): void {
    this.newVariantRequest.variantRequests.push({ sizes: [], price: 0, stock: 1 });
  }

  removeVariantGroup(index: number): void {
    this.newVariantRequest.variantRequests.splice(index, 1);
  }

  onFileSelected(event: any): void {
    if (event.target.files) {
      this.newVariantRequest.files = Array.from(event.target.files);
    }
  }
  parsePriceToNumber(price: any): number {
    if (!price) return 0;
    const clean = price.toString().replace(/\./g, '');
    return Number(clean);
  }
  saveNewVariant(): void {
    const productId = this.route.snapshot.paramMap.get('id');
    if (!productId) {
      this.error = "Không tìm thấy ID sản phẩm.";
      return;
    }

    if (this.isNewColor && !this.newVariantRequest.newColorName && !this.newVariantRequest.newColorCode) {
      this.error = "Vui lòng nhập tên màu mới.";
      return;
    }
    if (!this.isNewColor && !this.newVariantRequest.selectedColorCode) {

      this.error = "Vui lòng chọn một màu sắc.";
      return;
    }

    this.loading = true;
    const formData = new FormData();
    if (this.isNewColor) {
      formData.append('color.name', this.newVariantRequest.newColorName);
      formData.append('color.hexCode', this.newVariantRequest.newColorCode);
    } else {
      const selectedColor = this.availableColors.find(c => c.name === this.newVariantRequest.selectedColorCode);
      if (selectedColor) {
        formData.append('colorName', selectedColor.name);
      } else {
        this.error = "Màu sắc đã chọn không hợp lệ.";
        this.loading = false;
        return;
      }
    }

    formData.append('isAvailable', this.newVariantRequest.isAvailable);

    // --- Handle Variant Requests ---
    this.newVariantRequest.variantRequests.forEach((variantGroup, i) => {
      // Append price and stock for the current variant group
      formData.append(`variantRequests[${i}].price`, variantGroup.price.toString());
      formData.append(`variantRequests[${i}].stock`, variantGroup.stock.toString());
      variantGroup.sizes
        .sort((a, b) => {
          const nameA = this.availableSizes.find(s => s.code === a)?.name ?? '';
          const nameB = this.availableSizes.find(s => s.code === b)?.name ?? '';
          return nameA.toString().localeCompare(nameB.toString(), undefined, { numeric: true });
        })
      variantGroup.sizes.forEach((sizeCode, j) => {
        const size = this.availableSizes.find(s => s.code === sizeCode);
        if (size) {
          formData.append(`variantRequests[${i}].sizes[${j}].name`, size.name.toString());
        }
      });
    });
    this.newVariantRequest.files.forEach(file => {
      formData.append('files', file);
    });

    this.productColorService.create(productId, formData).subscribe({
      next: () => {
        this.loading = false;
        this.showAddVariantForm = false;
        this.resetNewVariantForm();
        this.loadProductDetails(productId);
      },
      error: (err) => {
        this.loading = false;
        this.error = 'Lỗi khi thêm phiên bản mới.';
        console.error(err);
      }
    });
  }

  toggleSize(sizeCode: string, group: { sizes: string[] }) {
    if (!group.sizes) group.sizes = [];

    const exists = group.sizes.includes(sizeCode);

    if (exists) {
      group.sizes = group.sizes.filter(s => s !== sizeCode);
    } else {
      group.sizes.push(sizeCode);
    }
  }



  isSizeDisabled(sizeCode: string, group: any): boolean {
    const selected = this.getAllSelectedSizes();

    // Nếu size nằm trong group hiện tại, không disable
    if (group.sizes.includes(sizeCode)) return false;

    // Nếu size đã được nhóm khác chọn → disable
    return selected.has(sizeCode);
  }


  toggleSelectAllSizes(group: any, event: any) {
    const selectedGlobal = this.getAllSelectedSizes();

    if (event.target.checked) {
      // size còn trống = size chưa được nhóm nào chọn
      group.sizes = this.availableSizes
        .filter(s => !selectedGlobal.has(s.code) || group.sizes.includes(s.code))
        .map(s => s.code);
    } else {
      // Bỏ hết size của group hiện tại
      group.sizes = [];
    }
  }

  isAllSelected(group: { sizes: string[] }): boolean {
    const selectedGlobal = this.getAllSelectedSizes();

    // size còn khả dụng cho group
    const availableForGroup = this.availableSizes
      .filter(s => !selectedGlobal.has(s.code) || group.sizes.includes(s.code))
      .map(s => s.code);

    return group.sizes.length === availableForGroup.length;
  }


  onPriceChange(index: number): void {
    const rawPrice = this.newVariantRequest.variantRequests[index].price;
    const cleanPrice = rawPrice ? Number(rawPrice.toString().replace(/\./g, '')) : 0;
    this.newVariantRequest.variantRequests[index].price = cleanPrice;
  }
  getAllSelectedSizes(): Set<string> {
    const selected = new Set<string>();
    this.newVariantRequest.variantRequests.forEach(group => {
      group.sizes?.forEach(s => selected.add(s));
    });
    return selected;
  }
  resetNewVariantForm(): void {
    this.newVariantRequest = {
      selectedColorCode: null,
      newColorName: '',
      newColorCode: '',
      isAvailable: 'AVAILABLE',
      variantRequests: [{ sizes: [], price: 0, stock: 1 }],
      files: []
    };
    this.selectedSizesGlobal.clear();
    this.newVariantRequest.variantRequests.forEach(v => v.sizes = []);
    this.isNewColor = false;
  }
}
