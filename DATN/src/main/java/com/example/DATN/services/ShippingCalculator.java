package com.example.DATN.services;


import com.example.DATN.dtos.respone.ghn.CalculateFeeRequest;
import com.example.DATN.dtos.respone.ghn.GhnCalculateFeeResponse;
import com.example.DATN.dtos.respone.ghn.ItemRequest;
import com.example.DATN.dtos.respone.order.CheckOutResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.Stock;
import com.example.DATN.models.Store;
import com.example.DATN.models.UserAddress;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.repositories.StoreRepository;
import com.example.DATN.repositories.projection.CheckOutProjection;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShippingCalculator {
    private final ProductVariantRepository productVariantRepository;
    private final BigDecimal BASE_SHIPPING_FEE = BigDecimal.valueOf(15);
    private final BigDecimal BASE_FEE_PER_ITEM = BigDecimal.ONE;
    private final StoreRepository storeRepository;
    private final GhnService ghnService;

    public List<ItemRequest> BuilderItemRequest(
            Map<String, ProductVariant> variantMap,
            Map<String, Integer> quantities) {
        List<ItemRequest> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : quantities.entrySet()) {
            String sku = entry.getKey();
            Integer quantity = entry.getValue();
            ProductVariant variant = variantMap.get(sku);
            if (variant == null) continue;
            items.add(ItemRequest.builder()
                    .name(variant.getProductColor().getProduct().getName())
                    .quantity(quantity)
                    .height(variant.getHeight())
                    .weight(variant.getWeight())
                    .length(variant.getLength())
                    .width(variant.getWidth())
                    .build());
        }
        return items;
    }

    public List<ItemRequest> buildItemRequestByResponse(CheckOutResponse response) {
        return response.getProducts().stream()
                .map(p -> ItemRequest.builder()
                        .name(p.getProductName())
                        .quantity(p.getQuantity())
                        .height(p.getHeight())
                        .weight(p.getWeight())
                        .length(p.getLength())
                        .width(p.getWidth())
                        .build()
                )
                .toList();
    }

    public BigDecimal recalculatorShippingAddress
            (Store store, CheckOutResponse response)
            throws JsonProcessingException {

        List<ItemRequest> itemRequests = buildItemRequestByResponse(response);
        CalculateFeeRequest request = CalculateFeeRequest.builder()
                .from_district_id(store.getDistrictCode())
                .from_ward_code(store.getWardCode())
                .to_district_id(response.getShippingAddressResponse().getDistrict_Id())
                .to_ward_code(response.getShippingAddressResponse().getWardCode())
                .service_id(53321)
                .service_type_id(2)
                .height(response.getTotal_height())
                .length(response.getTotal_length())
                .width(response.getTotal_width())
                .weight(response.getTotal_weight())
                .insurance_value(0)
                .cod_failed_amount(0)
                .items(itemRequests)
                .build();
        ResponseEntity<GhnCalculateFeeResponse> shippingFeeResponse = ghnService
                .calculateShippingFee(request, store.getStoreCodeGHN());
        return BigDecimal.valueOf(shippingFeeResponse.getBody().getData().getTotal());


    }

    public BigDecimal recalculatorItem(CheckOutResponse response) throws JsonProcessingException {
        List<ItemRequest> itemRequests = buildItemRequestByResponse(response);
        Store store = storeRepository.findById(response.getStoreId())
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));

        CalculateFeeRequest request = CalculateFeeRequest.builder()
                .from_district_id(store.getDistrictCode())
                .from_ward_code(store.getWardCode())
                .to_district_id(response.getShippingAddressResponse().getDistrict_Id())
                .to_ward_code(response.getShippingAddressResponse().getWardCode())
                .service_id(53321)
                .service_type_id(2)
                .height(response.getTotal_height())
                .length(response.getTotal_length())
                .width(response.getTotal_width())
                .weight(response.getTotal_weight())
                .insurance_value(0)
                .cod_failed_amount(0)
                .items(itemRequests)
                .build();
        ResponseEntity<GhnCalculateFeeResponse> shippingFeeResponse = ghnService
                .calculateShippingFee(request, store.getStoreCodeGHN());
        return BigDecimal.valueOf(shippingFeeResponse.getBody().getData().getTotal());
    }

    public BigDecimal calculatorShippingFee(Map<String, Integer> variants
            , Map<String, Stock> stock
            , Map<String, ProductVariant> variantMap
            , UserAddress userAddress
            , CheckOutProjection route
            , CheckOutResponse response) throws JsonProcessingException {
        List<ItemRequest> itemRequest = BuilderItemRequest(variantMap, variants);
        Store store = stock.values().stream()
                .map(Stock::getStore)
                .filter(Objects::nonNull)
                .findFirst()
                .orElseThrow(() -> new ApplicationException(ErrorCode.STORE_NOT_FOUND));
        CalculateFeeRequest request = CalculateFeeRequest.builder()
                .from_district_id(store.getDistrictCode())
                .from_ward_code(store.getWardCode())
                .to_district_id(userAddress.getDistrictCode())
                .to_ward_code(userAddress.getWardCode())
                .service_id(53321)
                .service_type_id(2)
                .height(response.getTotal_height())
                .length(response.getTotal_length())
                .width(response.getTotal_width())
                .weight(response.getTotal_weight())
                .insurance_value(0)
                .cod_failed_amount(0)
                .items(itemRequest)
                .build();
        ResponseEntity<GhnCalculateFeeResponse> shippingFeeResponse = ghnService.calculateShippingFee(request, store.getStoreCodeGHN());

        return BigDecimal.valueOf(shippingFeeResponse.getBody().getData().getTotal());
    }

}
