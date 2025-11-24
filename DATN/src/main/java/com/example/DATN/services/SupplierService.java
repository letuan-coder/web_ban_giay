package com.example.DATN.services;

import com.example.DATN.constant.SupplierStatus;
import com.example.DATN.dtos.request.supplier.SupplierRequest;
import com.example.DATN.dtos.respone.supplier.SupplierResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.SupplierMapper;
import com.example.DATN.models.Supplier;
import com.example.DATN.repositories.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        if (supplierRepository.findByName(request.getName()).isPresent()) {
            throw new ApplicationException(ErrorCode.SUPPLIER_NAME_EXISTED);
        }
        if (supplierRepository.findByTaxCode(request.getTaxCode()).isPresent()) {
            throw new ApplicationException(ErrorCode.SUPPLIER_TAXCODE_EXISTED);
        }
        if (supplierRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApplicationException(ErrorCode.SUPPLIER_EMAIL_EXISTED);
        }
        if (supplierRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new ApplicationException(ErrorCode.SUPPLIER_PHONE_EXISTED);
        }

        Supplier supplier = supplierMapper.toSupplier(request);
        if (request.getStatus() == null) {
            supplier.setStatus(SupplierStatus.ACTIVE);
        }
        return supplierMapper.toSupplierResponse(supplierRepository.save(supplier));
    }

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toSupplierResponse)
                .collect(Collectors.toList());
    }

    public SupplierResponse getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));
        return supplierMapper.toSupplierResponse(supplier);
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND));

        // Check for uniqueness constraints for other suppliers
        supplierRepository.findByName(request.getName())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> { throw new ApplicationException(ErrorCode.SUPPLIER_NAME_EXISTED); });
        supplierRepository.findByTaxCode(request.getTaxCode())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> { throw new ApplicationException(ErrorCode.SUPPLIER_TAXCODE_EXISTED); });
        supplierRepository.findByEmail(request.getEmail())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> { throw new ApplicationException(ErrorCode.SUPPLIER_EMAIL_EXISTED); });
        supplierRepository.findByPhoneNumber(request.getPhoneNumber())
                .filter(s -> !s.getId().equals(id))
                .ifPresent(s -> { throw new ApplicationException(ErrorCode.SUPPLIER_PHONE_EXISTED); });

        supplierMapper.updateSupplier(existingSupplier, request);
        return supplierMapper.toSupplierResponse(supplierRepository.save(existingSupplier));
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ApplicationException(ErrorCode.SUPPLIER_NOT_FOUND);
        }
        supplierRepository.deleteById(id);
    }
}
