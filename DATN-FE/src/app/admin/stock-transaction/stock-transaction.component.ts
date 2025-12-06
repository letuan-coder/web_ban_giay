import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { StockTransactionService, StockTransactionRequest, StockTransactionResponse } from '../../services/stock-transaction.service';
import { ProductVariantService } from '../../services/product-variant.service';
import { StoreService } from '../../services/store.service';
import { SupplierService, SupplierResponse } from '../../services/supplier.service';
import { WarehouseService } from '../../services/warehouse.service';
import { ProductService } from '../../services/product.service';
import { Store } from '../../model/store.model';
import { Warehouse } from '../../model/warehouse.model';
import { VariantResponse } from '../../model/variant.response.model';
import { Subject, Subscription, forkJoin } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, take } from 'rxjs/operators';
import { ActivatedRoute } from '@angular/router';

interface VariantDisplay {
  id: string;
  sku: string;
  productName: string;
  colorName: string;
  size: string | number; // Assuming size can be string or number based on usage
  price: number; // For displaying price
}

@Component({
  selector: 'app-stock-transaction',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './stock-transaction.component.html',
  styleUrls: ['./stock-transaction.component.scss']
})
export class StockTransactionComponent implements OnInit, OnDestroy {
  transactionForm: FormGroup;
  transactionTypes =
   ['IMPORT_TO_WAREHOUSE', 'EXPORT', 
    'TRANSFER', 'RETURN_SUPPLIER',
     'RETURN_WAREHOUSE', 'ADJUST'];

  suppliers: SupplierResponse[] = []; 
  stores: Store[] = [];
  warehouses: Warehouse[] = [];
  transactions: StockTransactionResponse[] = [];
  
  private searchTerms = new Subject<string>();
  searchResults: any[] = [];
  searchLoading = false;
  activeItemIndex: number | null = null;

  private searchSubscription: Subscription;

  loading = false;
  message = '';

  // New properties for product code search
  productCodeSearchResults: any[] = [];
  productCodeSearchLoading = false;
  productCodeSearchError: string | null = null;

  constructor(
    private fb: FormBuilder,
    private stockTransactionService: StockTransactionService,
    private productVariantService: ProductVariantService,
    private storeService: StoreService,
    private supplierService: SupplierService,
    private warehouseService: WarehouseService,
    private productService: ProductService,
    private route: ActivatedRoute,
  ) {
    this.transactionForm = this.fb.group({
      type: ['', Validators.required],
      expectedReceivedDate: [null],
      supplierId: [null],
      fromStoreId: [null],
      toStoreId: [null],
      fromWarehouseId: [null],
      toWarehouseId: [null],
      items: this.fb.array([], [Validators.required, Validators.minLength(1)])
    });

    this.searchSubscription = this.searchTerms.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      switchMap((term: string) => {
        if (term.length < 2) {
          this.searchResults = [];
          return [];
        }
        this.searchLoading = true;
        return this.productVariantService.search(term);
      })
    ).subscribe({
      next: (response: any[]) => {
        this.searchResults = response.map(item => ({
          ...item,
          productName: item.name,
          colorName: item.color,
        }));
        this.searchLoading = false;
      },
      error: () => {
        this.searchResults = [];
        this.searchLoading = false;
      }
    });
  }

  ngOnInit(): void {
    this.loadAllTransactions();
    this.onTypeChange();

    const initialData$ = {
      stores: this.storeService.getAll(),
      suppliers: this.supplierService.getAllSuppliers(),
      warehouses: this.warehouseService.getAll()
    };

    forkJoin(initialData$).subscribe({
      next: ({ stores, suppliers, warehouses }) => {
        this.stores = stores.data;
        this.suppliers = suppliers.data;
        this.warehouses = warehouses.data;

        // Process query params after initial data is loaded
        this.route.queryParams.pipe(take(1)).subscribe(params => {
          const type = params['type'];
          const supplierIdParam = params['supplierId'];

          if (type && this.transactionTypes.includes(type)) {
            this.transactionForm.patchValue({ type: type });
          }

          if (supplierIdParam) {
            const numericSupplierId = Number(supplierIdParam);
            const supplier = this.suppliers.find(s => s.id === numericSupplierId);
            if (supplier) {
              this.transactionForm.patchValue({ supplierId: supplier.taxCode });
            }
          }
        });
      },
      error: (err) => {
        this.message = 'Không thể tải dữ liệu cần thiết. Vui lòng tải lại trang.';
        console.error(err);
      }
    });
  }

  ngOnDestroy(): void {
    this.searchSubscription.unsubscribe();
  }

  loadAllTransactions(): void {
    this.stockTransactionService.getAllTransactions().subscribe({
      next: (response: any) => {
        this.transactions = response.data;
      },
      error: (err) => {
        console.error('Error fetching all transactions:', err);
      }
    });
  }
  
  onTypeChange(): void {
    this.transactionForm.get('type')?.valueChanges.subscribe(type => {
      const fieldsToReset = ['supplierId', 'fromStoreId', 'toStoreId', 'fromWarehouseId', 'toWarehouseId'];
      fieldsToReset.forEach(field => {
        this.transactionForm.get(field)?.clearValidators();
        this.transactionForm.get(field)?.setValue(null);
      });
      
      // Set new validators based on type
      if (type === 'IMPORT_TO_WAREHOUSE') {
        this.transactionForm.get('supplierId')?.setValidators(Validators.required);
        this.transactionForm.get('toWarehouseId')?.setValidators(Validators.required);
      } else if (type === 'EXPORT') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
      } else if (type === 'TRANSFER') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
      } else if (type === 'RETURN_SUPPLIER') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('supplierId')?.setValidators(Validators.required);
      } else if (type === 'RETURN_WAREHOUSE') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('toWarehouseId')?.setValidators(Validators.required);
      }

      fieldsToReset.forEach(field => {
        this.transactionForm.get(field)?.updateValueAndValidity();
      });
    });
  }

  get items(): FormArray {
    return this.transactionForm.get('items') as FormArray;
  }

  createItem(variant?: VariantDisplay | VariantResponse): FormGroup {
    return this.fb.group({
      variantId: [variant ? variant.id : '', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      variantDetails: [variant || null]
    });
  }
  addItem(): void {
    this.items.push(this.createItem());
  }

  removeItem(index: number): void {
    this.items.removeAt(index);
  }

  search(event: any, index: number): void {
    this.activeItemIndex = index;
    this.searchTerms.next(event.target.value);
  }

  selectVariant(variant: VariantResponse): void {
    if (this.activeItemIndex !== null) {
      const item = this.items.at(this.activeItemIndex);
      const existingItem = this.items.controls.find(
        (control, i) => i !== this.activeItemIndex && control.get('variantId')?.value === variant.id
      );
      if(existingItem) {
        alert('Sản phẩm này đã được thêm vào phiếu.');
        return;
      }

      item.patchValue({ variantId: variant.id, variantDetails: variant });
      this.clearSearch();
    }
  }

  clearSearch(): void {
    this.searchResults = [];
    this.activeItemIndex = null;
  }

  searchByProductCode(code: string): void {
    if (!code || code.trim() === '') {
        return;
    }
    this.productCodeSearchLoading = true;
    this.productCodeSearchError = null;
    this.productCodeSearchResults = [];

    this.productService.getProductByCode(code.trim()).subscribe({
        next: (response: any) => {
            this.productCodeSearchResults = response.data; // Store raw hierarchical data
            this.productCodeSearchLoading = false;
            if (!this.productCodeSearchResults || this.productCodeSearchResults.length === 0) {
                this.productCodeSearchError = "Không tìm thấy sản phẩm với mã này.";
            }
        },
        error: (err) => {
            this.productCodeSearchLoading = false;
            this.productCodeSearchError = "Lỗi khi tìm kiếm sản phẩm.";
            console.error(err);
        }
    });
  }

  addVariantToItems(variant: any, productName: string): void {
      const existingItem = this.items.controls.find(
          control => control.get('variantId')?.value === variant.id
      );

      if (existingItem) {
          alert('Sản phẩm này đã được thêm vào phiếu.');
          return;
      }

      // Construct the object that the UI expects for `variantDetails`
      const variantDetails: VariantDisplay = {
          id: variant.id,
          sku: variant.sku,
          productName: productName,
          colorName: variant.colorName,
          size: variant.size, // Direct usage of variant.size
          price: variant.price // Include price
      };

      const newItem = this.createItem(variantDetails);
      this.items.push(newItem);
  }


  onSubmit(): void {
    if (this.transactionForm.invalid) {
      this.message = 'Vui lòng điền đầy đủ thông tin và thêm ít nhất một sản phẩm.';
      return;
    }

    this.loading = true;
    this.message = '';

    const formValue = this.transactionForm.getRawValue();
    const request: StockTransactionRequest = {
      ...formValue,
      items: formValue.items.map((item: any) => ({
        variantId: item.variantId,
        quantity: item.quantity
      }))
    };

    this.stockTransactionService.createTransaction(request).subscribe({
      next: () => {
        this.loading = false;
        this.message = 'Tạo phiếu kho thành công!';
        this.transactionForm.reset({ type: '' });
        this.items.clear();
        this.productCodeSearchResults = [];
        this.loadAllTransactions();
      },
      error: (err) => {
        this.loading = false;
        this.message = err.error?.message || 'Tạo phiếu kho thất bại. Vui lòng thử lại.';
        console.error(err);
      }
    });
  }
}
