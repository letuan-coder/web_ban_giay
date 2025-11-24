import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Stock } from '../../model/stock.model'; // Corrected import path
import { StockService } from '../../services/stock.service'; // Corrected import path
import { ApiResponse } from '../../services/sale.service';

@Component({
  selector: 'app-stocks',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stocks.component.html',
  styleUrls: ['./stocks.component.scss']
})
export class StocksComponent implements OnInit {
  stocks: Stock[] = [];
  selectedStock: Partial<Stock> = {};
  showForm = false;

  constructor(private stockService: StockService) { }

  ngOnInit(): void {
    this.loadStocks();
  }

  loadStocks(): void {
    this.stockService.getAll().subscribe((response: ApiResponse<Stock[]>) => {
      this.stocks = response.data;
    });
  }

  showAddForm(): void {
    this.selectedStock = {};
    this.showForm = true;
  }

  selectStock(stock: Stock): void {
    this.selectedStock = { ...stock };
    this.showForm = true;
  }

  resetForm(): void {
    this.selectedStock = {};
    this.showForm = false;
  }

  saveStock(): void {
    // Assuming 'name' is the main editable property for simplicity, similar to Brand/Category
    if (this.selectedStock.name && this.selectedStock.quantity !== undefined) {
      if (this.selectedStock.id) {
        // Update existing stock
        this.stockService.update(this.selectedStock.id, { 
          name: this.selectedStock.name, 
          quantity: this.selectedStock.quantity 
        }).subscribe(() => {
            this.loadStocks();
            this.resetForm();
          });
      } else {
        // Create new stock
        this.stockService.create({ 
          name: this.selectedStock.name, 
          quantity: this.selectedStock.quantity 
        }).subscribe(() => {
            this.loadStocks();
            this.resetForm();
          });
      }
    }
  }

  deleteStock(id: number): void {
    if (confirm('Bạn có chắc chắn muốn xóa mặt hàng tồn kho này không?')) {
      this.stockService.delete(id).subscribe(() => {
        this.loadStocks();
      });
    }
  }
}
