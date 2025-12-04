import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, FormArray, Validators, ReactiveFormsModule } from '@angular/forms';
import { StockService } from '../../services/stock.service';
import { StockTransactionService, StockTransactionResponse, StockTransactionItemResponse } from '../../services/stock-transaction.service';

@Component({
  selector: 'app-stocks',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './stocks.component.html',
  styleUrls: ['./stocks.component.scss']
})
export class StocksComponent implements OnInit {
  
  // State for the new workflow
  transactionIdToSearch: string | null = null;
  transaction: StockTransactionResponse | null = null;
  receiptForm: FormGroup;
  
  loading = false; // For finding transaction
  submitting = false; // For submitting receipt
  message = '';
  isError = false;

  constructor(
    private fb: FormBuilder,
    private stockService: StockService,
    private stockTransactionService: StockTransactionService
  ) {
    this.receiptForm = this.fb.group({
      items: this.fb.array([])
    });
  }

  ngOnInit(): void {
    // No initial data load needed for this new workflow
  }

  get receiptItems(): FormArray {
    return this.receiptForm.get('items') as FormArray;
  }

  findTransaction(): void {
    if (!this.transactionIdToSearch) {
      return;
    }
    this.loading = true;
    this.message = '';
    this.isError = false;
    this.transaction = null;
    
    this.stockTransactionService.getTransactionById(this.transactionIdToSearch).subscribe({
      next: (response) => {
        this.loading = false;
        if (response.data.transactionStatus !== 'PENDING') {
            this.message = `Phiếu kho #${response.data.id} đã ở trạng thái ${response.data.transactionStatus} và không thể nhập kho.`;
            this.isError = true;
            return;
        }
        this.transaction = response.data;
        this.buildReceiptForm(this.transaction?.items || []);
      },
      error: (err) => {
        this.loading = false;
        this.isError = true;
        this.message = err.error?.message || `Không tìm thấy phiếu kho với ID: ${this.transactionIdToSearch}`;
        console.error(err);
      }
    });
  }

  buildReceiptForm(items: StockTransactionItemResponse[]): void {
    this.receiptItems.clear();
    items.forEach(item => {
      const itemGroup = this.fb.group({
        variantId: [item.variant.id, Validators.required],
        expectedQuantity: [item.quantity],
        actualQuantity: [item.quantity, [Validators.required, Validators.min(0)]],
        // Store variant details for display purposes
        variant: [item.variant]
      });
      this.receiptItems.push(itemGroup);
    });
  }

  confirmReceipt(): void {
    if (this.receiptForm.invalid) {
      this.message = 'Vui lòng nhập đầy đủ số lượng thực nhận cho tất cả sản phẩm.';
      this.isError = true;
      return;
    }
    if (!this.transaction) return;

    this.submitting = true;
    this.message = '';
    this.isError = false;

    const formValue = this.receiptForm.getRawValue();
    const request = {
      stockTransactionId: this.transaction.id,
      items: formValue.items.map((item: any) => ({
        variantId: item.variantId,
        quantity: item.actualQuantity
      }))
    };

    this.stockService.receiveStock(request).subscribe({
      next: () => {
        this.submitting = false;
        this.message = 'Xác nhận nhập kho thành công!';
        this.isError = false;
        // Reset state
        this.transaction = null;
        this.transactionIdToSearch = null;
        this.receiptItems.clear();
      },
      error: (err) => {
        this.submitting = false;
        this.isError = true;
        this.message = err.error?.message || 'Đã có lỗi xảy ra khi xác nhận nhập kho.';
        console.error(err);
      }
    });
  }
}
