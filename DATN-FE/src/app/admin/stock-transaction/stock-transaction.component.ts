import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { StockTransactionService, StockTransactionRequest } from '../../services/stock-transaction.service';
import { ProductVariantService } from '../../services/product-variant.service';
import { StoreService } from '../../services/store.service';
import { SupplierService, SupplierResponse } from '../../services/supplier.service';
import { Store } from '../../model/store.model';
import { VariantResponse } from '../../model/variant.response.model';
import { Subject, Subscription } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';

@Component({
  selector: 'app-stock-transaction',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './stock-transaction.component.html',
  styleUrls: ['./stock-transaction.component.scss']
})
export class StockTransactionComponent implements OnInit, OnDestroy {
  transactionForm: FormGroup;
  transactionTypes = ['IN', 'OUT', 'TRANSFER'];

  suppliers: SupplierResponse[] = []; 
  stores: Store[] = [];
  
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
  ) {
    this.transactionForm = this.fb.group({
      type: ['', Validators.required],
      supplierId: [null],
      fromStoreId: [null],
      toStoreId: [null],
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
        this.searchResults = response.data;
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
  }

  ngOnDestroy(): void {
    this.searchSubscription.unsubscribe();
  }

  loadInitialData(): void {
    this.storeService.getAll().subscribe((response: any) => this.stores = response.data);
    this.supplierService.getAllSuppliers().subscribe((response: any) => this.suppliers = response.data);
  }
  
  onTypeChange(): void {
    this.transactionForm.get('type')?.valueChanges.subscribe(type => {
      // Reset all relevant fields and validators
      ['supplierId', 'fromStoreId', 'toStoreId'].forEach(field => {
        this.transactionForm.get(field)?.clearValidators();
        this.transactionForm.get(field)?.setValue(null);
      });
      
      // Set new validators based on type
      if (type === 'IN') {
        this.transactionForm.get('supplierId')?.setValidators(Validators.required);
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
      } else if (type === 'OUT') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
      } else if (type === 'TRANSFER') {
        this.transactionForm.get('fromStoreId')?.setValidators(Validators.required);
        this.transactionForm.get('toStoreId')?.setValidators(Validators.required);
      }

      // Update validity for all fields
      ['supplierId', 'fromStoreId', 'toStoreId'].forEach(field => {
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
      // Check if this variant is already in the list
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

    // We need to build the request payload by removing the 'variantDetails'
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
      },
      error: (err) => {
        this.loading = false;
        this.message = err.error?.message || 'Tạo phiếu kho thất bại. Vui lòng thử lại.';
        console.error(err);
      }
    });
  }
}
