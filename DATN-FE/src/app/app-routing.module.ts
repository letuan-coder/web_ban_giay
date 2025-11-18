import { Routes } from '@angular/router';
import { ProductListComponent } from './products/product-list/product-list.component';
import { ProductCreateComponent } from './products/product-create/product-create.component';
import { LoginComponent } from './login/login.component';
import { ProductDetailComponent } from './products/product-detail/product-detail.component';
import { ProductEditComponent } from './products/product-edit/product-edit.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { ProductColorsComponent } from './products/product-colors/product-colors.component';
import { CategoriesComponent } from './admin/categories/categories.component';
import { BrandsComponent } from './admin/brands/brands.component';
import { PromotionsComponent } from './products/promotions/promotions.component';

export const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'products', component: ProductListComponent },
  { path: 'products/create', component: ProductCreateComponent },
  { path: 'products/edit/:id', component: ProductEditComponent }, // More specific route first
  { path: 'products/:id/:slug', component: ProductDetailComponent }, // More general route second
  { path: 'product-variants/update/:id', component: ProductColorsComponent },
  { path: 'admin/categories', component: CategoriesComponent },
  { path: 'admin/brands', component: BrandsComponent },
  { path: 'promotions', component: PromotionsComponent }
];
