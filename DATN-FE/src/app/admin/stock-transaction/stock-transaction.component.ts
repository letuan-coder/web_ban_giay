import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { StockTransactionService, StockTransactionRequest, StockTransactionResponse } from '../../services/stock-transaction.service';
import { ProductVariantService } from '../../services/product-variant.service';
import { StoreService } from '../../services/store.service';
import { SupplierService, SupplierResponse } from '../../services/supplier.service';
import { WarehouseService } from '../../services/warehouse.service'; // Import WarehouseService
import { Store } from '../../model/store.model';
import { Warehouse } from '../../model/warehouse.model'; // Import Warehouse model
import { VariantResponse } from '../../model/variant.response.model';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
// Removed: import { ConstantsService } from '../../services/constants.service';

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
   ['IMPORT', 'EXPORT', 'TRANSFER', 'RETURN_SUPPLIER', 'RETURN_WAREHOUSE', 'ADJUST']; // Hardcoded again

  suppliers: SupplierResponse[] = []; 
  stores: Store[] = [];
  warehouses: Warehouse[] = []; // Add warehouses array
  transactions: StockTransactionResponse[] = [];
  
  // Search-related properties
  private searchTerms = new Subject<string>();
  searchResults: VariantResponse[] = [];
  searchLoading = false;
  activeItemIndex: number | null = null; // To know which item row is searching

  private searchSubscription: Subscription;

  loading = false;
  message = '';

  constructor(
    private fb: FormBuilder,
    private stockTransactionService: StockTransactionService,
    private productVariantService: ProductVariantService,
    private storeService: StoreService,
    private supplierService: SupplierService,
    private warehouseService: WarehouseService, // Inject WarehouseService
    // Removed: private constantsService: ConstantsService,
  ) {
    this.transactionForm = this.fb.group({
      type: ['', Validators.required],
      supplierId: [null],
      fromStoreId: [null],
      toStoreId: [null],
      fromWarehouseId: [null], // Add fromWarehouseId
      toWarehouseId: [null],   // Add toWarehouseId
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
      next: (response: any) => {
        let results: VariantResponse[] = [];
        
        if (response.data) {
          if (Array.isArray(response.data.content)) {
            results = response.data.content;
          } else if (Array.isArray(response.data)) {
            results = response.data;
          } else if (typeof response.data === 'object' && response.data !== null) {
            results = [response.data];
          }
        }
        this.searchResults = results;
        this.searchLoading = false;
      },
      error: () => {
        this.searchResults = [];
        this.searchLoading = false;
      }
    });
  }

  ngOnInit(): void {
    this.loadInitialData();
    this.onTypeChange();
    this.loadAllTransactions();
    // Removed: this.loadTransactionTypes();
  }

  ngOnDestroy(): void {
    this.searchSubscription.unsubscribe();
  }

  loadInitialData(): void {
    this.storeService.getAll().subscribe((response: any) => this.stores = response.data);
    this.supplierService.getAllSuppliers().subscribe((response: any) => this.suppliers = response.data);
    this.warehouseService.getAll().subscribe((response: any) => this.warehouses = response.data); // Load warehouses
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

  // Removed: loadTransactionTypes() method
  
  onTypeChange(): void {
    this.transactionForm.get('type')?.valueChanges.subscribe(type => {
      const fieldsToReset = ['supplierId', 'fromStoreId', 'toStoreId', 'fromWarehouseId', 'toWarehouseId'];
      fieldsToReset.forEach(field => {
        this.transactionForm.get(field)?.clearValidators();
        this.transactionForm.get(field)?.setValue(null);
      });
      
      // Set new validators based on type
      if (type === 'IMPORT') {
        this.transactionForm.get('supplierId')?.setValidators(Validators.required);
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
        // Also allow import to warehouse
        // this.transactionForm.get('toWarehouseId')?.setValidators(Validators.required);
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
        this.transactionForm.get('toWarehouseId')?.setValidators(Validators.required); // Corrected typo
      } else if (type === 'ADJUST') {
        // No specific validation rules for adjust in this context
      }

      // Update validity for all fields
      fieldsToReset.forEach(field => {
        this.transactionForm.get(field)?.updateValueAndValidity();
      });
    });
  }

  get items(): FormArray {
    return this.transactionForm.get('items') as FormArray;
  }

  createItem(variant?: VariantResponse): FormGroup {
    return this.fb.group({
      variantId: [variant ? variant.id : '', Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      // Add a non-form-control property to store the variant details for display
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
