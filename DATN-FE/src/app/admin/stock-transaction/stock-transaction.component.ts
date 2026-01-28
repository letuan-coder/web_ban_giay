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
import { Subject, Subscription, forkJoin, of } from 'rxjs';
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
  transactionTypes = [
    'IMPORT_TO_WAREHOUSE',
    'IMPORT_TO_STORE',
    'EXPORT_TO_STORE',
    'TRANSFER',
    'RETURN_TO_SUPPLIER',
    'RETURN_TO_WAREHOUSE',
    'ADJUST'
  ];

  suppliers: SupplierResponse[] = []; 
  stores: Store[] = [];
  warehouses: Warehouse[] = [];
  transactions: StockTransactionResponse[] = [];
  
  private searchTerms = new Subject<string>();
  searchResults: any[] = [];
  searchLoading = false;
  activeItemIndex: number | null = null;

  private searchSubscription: Subscription;
  private supplierSubscription!: Subscription;

  loading = false;
  message = '';

  // New properties for product code search
  productCodeSearchResults: any[] = [];
  productCodeSearchLoading = false;
  productCodeSearchError: string | null = null;

  // New properties for supplier products
  supplierProducts: any[] = [];
  supplierProductsLoading = false;
  supplierProductsError: string | null = null;

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
    this.onSupplierChange();

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
            const supplierId = String(supplierIdParam);
            const supplier = this.suppliers.find(s => String(s.id) === supplierId);
            if (supplier) {
              this.transactionForm.patchValue({ supplierId: supplier.id });
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
    if (this.supplierSubscription) {
      this.supplierSubscription.unsubscribe();
    }
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
      } else if (type === 'IMPORT_TO_STORE') {
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
        // supplierId can be optional for IMPORT_TO_STORE if it's an internal transfer from another store/warehouse,
        // but if it's from an external supplier directly to store, it should be required.
        // For now, let's assume it's like IMPORT_TO_WAREHOUSE for supplier product fetching.
        // If no supplier is selected, the product list will be empty, prompting manual search.
      } else if (type === 'EXPORT_TO_STORE') {
        this.transactionForm.get('fromWarehouseId')?.setValidators(Validators.required); // Export from Warehouse to Store
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
      } else if (type === 'TRANSFER') {
        // Transfer logic implies moving between store/warehouse locations.
        // The current implementation allows selecting both 'from' and 'to' for stores/warehouses.
        // This needs careful consideration based on exact business rules for transfers.
        // Assuming general transfer from a source to a destination.
        // This validation might need to be more complex (e.g., at least one 'from' and one 'to' required,
        // and 'from' != 'to' of same type). For simplicity, keeping existing as basic example for store-to-store.
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
      } else if (type === 'RETURN_TO_SUPPLIER') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('supplierId')?.setValidators(Validators.required);
      } else if (type === 'RETURN_TO_WAREHOUSE') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('toWarehouseId')?.setValidators(Validators.required);
      }

      fieldsToReset.forEach(field => {
        this.transactionForm.get(field)?.updateValueAndValidity();
      });
    });
  }

  onSupplierChange(): void {
    this.supplierSubscription = this.transactionForm.get('supplierId')!.valueChanges.pipe(
      distinctUntilChanged()
    ).subscribe(() => {
      // When supplier changes, just clear the old product list
      this.supplierProducts = [];
      this.supplierProductsError = null;
    });
  }

  fetchSupplierProducts(): void {
    const supplierId = this.transactionForm.get('supplierId')?.value;
    if (!supplierId) {
      this.supplierProductsError = "Vui lòng chọn một nhà cung cấp trước.";
      return;
    }

    this.supplierProducts = [];
    this.supplierProductsError = null;
    this.supplierProductsLoading = true;
    
    this.productCodeSearchResults = [];
    this.productCodeSearchError = null;
    
    this.productService.getProductsBySupplier(supplierId).subscribe({
      next: (response: any) => {
        this.supplierProducts = response.data || [];
        this.supplierProducts.forEach(p => {
          p.variantsVisible = false;
          p.variantsFetched = false;
          p.variantsLoading = false;
          p.variantDetailResponses = []; // Initialize as empty
        });
        if (this.supplierProducts.length === 0) {
          this.supplierProductsError = "Nhà cung cấp này không có sản phẩm nào.";
        }
        this.supplierProductsLoading = false;
      },
      error: (err) => {
        this.supplierProductsLoading = false;
        this.supplierProductsError = 'Lỗi khi tải sản phẩm từ nhà cung cấp.';
        console.error(err);
      }
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
      if (this.isVariantInItems(variant.id)) {
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
    this.supplierProducts = []; // Clear supplier products
    this.supplierProductsError = null;


    this.productService.getProductByCode(code.trim()).subscribe({
        next: (response: any) => {
            this.productCodeSearchResults = response.data; // Store raw hierarchical data
            this.productCodeSearchResults.forEach(p => {
              p.variantsVisible = false;
              p.variantsFetched = false;
              p.variantsLoading = false;
              p.variantDetailResponses = []; // Initialize as empty
            });
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
    if (this.isVariantInItems(variant.id)) {
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

  isVariantInItems(variantId: string): boolean {
    return this.items.controls.some(control => control.get('variantId')?.value === variantId);
  }

  toggleVariants(product: any): void {
    // Just hide if already visible
    if (product.variantsVisible) {
      product.variantsVisible = false;
      return;
    }

    // Show the list
    product.variantsVisible = true;

    // Fetch if not already fetched
    if (!product.variantsFetched) {
      product.variantsLoading = true;
      this.productService.getById(product.id).subscribe({
        next: (response: any) => {
          product.variantDetailResponses = response.data?.variantDetailResponses || [];
          product.variantsFetched = true;
          product.variantsLoading = false;
        },
        error: (err) => {
          console.error('Error fetching variants for product ' + product.id, err);
          // Optionally add an error message property to the product
          product.variantsLoading = false;
        }
      });
    }
  }

  addAllVariantsToItems(product: any): void {
    let addedCount = 0;
    for (const variant of product.variantDetailResponses) {
      if (!this.isVariantInItems(variant.id)) {
        const variantDetails: VariantDisplay = {
          id: variant.id,
          sku: variant.sku,
          productName: product.name, // Use product.name as productName
          colorName: variant.colorName,
          size: variant.size,
          price: variant.price
        };
        const newItem = this.createItem(variantDetails);
        this.items.push(newItem);
        addedCount++;
      }
    }
    if (addedCount > 0) {
      alert(`Đã thêm ${addedCount} biến thể của sản phẩm '${product.name}' vào phiếu.`);
    } else {
      alert(`Tất cả biến thể của sản phẩm '${product.name}' đã có trong phiếu.`);
    }
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
        this.supplierProducts = [];
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
