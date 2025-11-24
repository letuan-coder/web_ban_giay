package com.example.DATN.mapper;

import com.example.DATN.dtos.request.supplier.SupplierRequest;
import com.example.DATN.dtos.respone.supplier.SupplierResponse;
import com.example.DATN.models.Supplier;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SupplierMapper {
    @Mapping(target = "SupplierAddress" ,source = "supplierAddress")
    Supplier toSupplier(SupplierRequest request);
    SupplierResponse toSupplierResponse(Supplier supplier);
    void updateSupplier(@MappingTarget Supplier supplier, SupplierRequest request);
}
