import { Component, Input, Output, EventEmitter, ElementRef, HostListener, forwardRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NG_VALUE_ACCESSOR, ControlValueAccessor } from '@angular/forms';

@Component({
  selector: 'app-searchable-dropdown',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './searchable-dropdown.component.html',
  styleUrls: ['./searchable-dropdown.component.scss'],
})
export class SearchableDropdownComponent {
  @Input() data: any[] = [];
  @Input() displayField = 'name';
  @Input() searchFields: string[] = ['name'];
  @Input() placeholder = 'Tìm kiếm...';

  @Output() selectionChange = new EventEmitter<any>();

  searchTerm = '';
  filteredData: any[] = [];
  isDropdownOpen = false;
  private selectedItem: any = null;

  constructor(private eRef: ElementRef) {}

  @HostListener('document:click', ['$event'])
  clickout(event: Event) {
    if (!this.eRef.nativeElement.contains(event.target)) {
      this.isDropdownOpen = false;
    }
  }

  filterData() {
    this.isDropdownOpen = true;
    if (!this.searchTerm) {
      this.filteredData = [...this.data];
      // When clearing search, we don't know what to emit, so maybe emit null
      if (this.selectedItem) {
        this.selectedItem = null;
        this.selectionChange.emit(null);
      }
      return;
    }

    const lowerCaseSearchTerm = this.searchTerm.toLowerCase();
    this.filteredData = this.data.filter(item => {
      // Check the main display field first
      if (item[this.displayField]?.toLowerCase().includes(lowerCaseSearchTerm)) {
        return true;
      }
      // Then check other search fields, like NameExtension
      for (const field of this.searchFields) {
        const value = item[field];
        if (Array.isArray(value)) { // Handle array fields like NameExtension
          if (value.some(ext => ext.toLowerCase().includes(lowerCaseSearchTerm))) {
            return true;
          }
        } else if (typeof value === 'string') {
          if (value.toLowerCase().includes(lowerCaseSearchTerm)) {
            return true;
          }
        }
      }
      return false;
    });
  }

  selectItem(item: any) {
    this.searchTerm = item[this.displayField];
    this.selectedItem = item;
    this.isDropdownOpen = false;
    this.selectionChange.emit(item);
  }

  openDropdown() {
    this.isDropdownOpen = true;
    // When opening, show all data if input is empty
    if (!this.searchTerm) {
      this.filteredData = [...this.data];
    }
  }
}
