import { Component, OnInit, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as feather from 'feather-icons';
import Chart from 'chart.js/auto';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { FormsModule } from '@angular/forms';
import { SaleService, ApiResponse, SaleResponse } from '../services/sale.service';
import { HttpClientModule } from '@angular/common/http';
// import { getElement } from 'pdfmake/build/pdfmake';

Chart.register(ChartDataLabels);

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit {
  showSearchBar: boolean = false;
  searchTerm: string = '';
  
  public salesChart: any;
  private dailyData: { [key: string]: number } = {};
  private monthlyData: { [key: string]: number } = {};
  private yearlyData: { [key: string]: number } = {};

  public tableData: { time: string, revenue: number }[] = [];
  public selectedPeriod: string = '';
  public sortDirection: 'asc' | 'desc' = 'desc';
  
  public selectedTimeFrame: 'monthly' | 'yearly' = 'yearly';

  constructor(private saleService: SaleService) { }

  ngOnInit(): void {
    this.saleService.getSalesData().subscribe((response: ApiResponse<SaleResponse>) => {
      this.monthlyData = response.data.monthly;
      this.dailyData = response.data.daily;
      this.yearlyData = response.data.yearly;
      
      this.updateChart();
    });
  }

  ngAfterViewInit(): void {
    feather.replace();
  }

  setTimeFrame(timeFrame: 'monthly' | 'yearly'): void {
    this.selectedTimeFrame = timeFrame;
    this.tableData = [];
    this.selectedPeriod = '';
    this.updateChart();
  }

  sortDetailsTable(): void {
    this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    this.tableData.sort((a, b) => {
      if (this.sortDirection === 'asc') {
        return a.revenue - b.revenue;
      } else {
        return b.revenue - a.revenue;
      }
    });
  }

  updateChart(): void {
    let labels: string[] = [];
    let data: number[] = [];
    let label = '';

    if (this.selectedTimeFrame === 'monthly') {
      labels = Object.keys(this.monthlyData);
      data = Object.values(this.monthlyData);
    } else {
      labels = Object.keys(this.yearlyData);
      data = Object.values(this.yearlyData);
    }

    if (this.salesChart) {
      this.salesChart.destroy();
    }

    const canvas = document.getElementById('salesChart') as HTMLCanvasElement;
    if (canvas) {
      this.salesChart = new Chart(canvas, {
        type: 'bar',
        data: {
          labels: labels,
          datasets: [{
            label: label,
            data: data,
            backgroundColor: this.selectedTimeFrame === 'monthly' ? 'rgba(0, 123, 255, 0.5)' : 'rgba(255, 193, 7, 0.5)',
            borderColor: this.selectedTimeFrame === 'monthly' ? 'rgba(0, 123, 255, 1)' : 'rgba(255, 193, 7, 1)',
            borderWidth: 1
          }]
        },
        options: {
          onClick: (event, elements) => {
            if (elements.length > 0) {
              const elementIndex = elements[0].index;
              const clickedLabel = labels[elementIndex];
              this.selectedPeriod = clickedLabel;
              
              if (this.selectedTimeFrame === 'yearly') {
                // Filter monthly data for the selected year
                this.tableData = Object.entries(this.monthlyData)
                  .filter(([key, value]) => key.startsWith(clickedLabel))
                  .map(([key, value]) => ({ time: key, revenue: value }));
              } else if (this.selectedTimeFrame === 'monthly') {
                // Filter daily data for the selected month
                this.tableData = Object.entries(this.dailyData)
                  .filter(([key, value]) => key.startsWith(clickedLabel))
                  .map(([key, value]) => ({ time: key, revenue: value }));
              }
            }
          },
          scales: {
            y: {
              beginAtZero: true
            }
          },
          plugins: {
            legend: {
              display: true
            },
            datalabels: {
              anchor: 'end',
              align: 'top',
              formatter: (value, context) => {
                return value.toLocaleString('vi-VN');
              },
              color: '#444'
            }
          }
        }
      });
    }
  }

  toggleSearchBar(): void {
    this.showSearchBar = !this.showSearchBar;
    if (!this.showSearchBar) {
      this.searchTerm = '';
    }
  }
}

