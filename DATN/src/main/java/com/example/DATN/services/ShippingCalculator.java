package com.example.DATN.services;


import com.example.DATN.constant.WeightTier;
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

    //    @Cacheable(
//            value = "product_variant",
//            key = "#sku",
//            unless = "#result == null"
//    )
//    public ProductVariant getBySku(String sku) {
//        return productVariantRepository.findBysku(sku)
//                .orElseThrow(() -> new ApplicationException(
//                        ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
//    }
//    public ShippingFeeResponse calculateShippingFee(Integer id) {
//        ItemRequest item1 = ItemRequest.builder()
//                .name("Áo thun")
//                .quantity(2)
//                .height(5)
//                .weight(300)
//                .length(30)
//                .width(20)
//                .build();
//
//        ItemRequest item2 = ItemRequest.builder()
//                .name("Quần jean")
//                .quantity(1)
//                .height(8)
//                .weight(700)
//                .length(40)
//                .width(30)
//                .build();
//
//        CalculateFeeRequest request = CalculateFeeRequest.builder()
//                .fromDistrictId(1449)
//                .fromWardCode("20707")
//                .toDistrictId(1450)
//                .toWardCode("910375")
//                .serviceId(53321)
//                .serviceTypeId(2)
//                .height(15)
//                .length(40)
//                .width(30)
//                .weight(1000)
//                .insuranceValue(500000)
//                .codFailedAmount(0)
//                .coupon(null)
//                .items(List.of(item1, item2))
//                .build();
//        return ghnService.calculateShippingFee(request, id);
//
//    }

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
     List<ItemRequest>  itemRequests=  buildItemRequestByResponse(response);
     Store store =storeRepository.findById(response.getStoreId())
             .orElseThrow(()->new ApplicationException(ErrorCode.STORE_NOT_FOUND));
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

    public BigDecimal calculatorShippingFeeByWeight(Map<String, Integer> variants) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        Integer totalWeight = 0;

        for (Map.Entry<String, Integer> Productvariant : variants.entrySet()) {
            Integer quantity = Productvariant.getValue();
            String sku = Productvariant.getKey();
            ProductVariant variant = productVariantRepository.findBysku(sku)
                    .orElseThrow(() -> new ApplicationException(ErrorCode.PRODUCT_VARIANT_NOT_FOUND));
            totalWeight += variant.getWeight() * quantity;

        }
        WeightTier weightTier = WeightTier.fromWeight(totalWeight);
        switch (weightTier) {
            case FREE_WEIGHT -> {
                totalPrice = BigDecimal.ZERO;
            }
            case LIGHT -> {
                totalPrice = WeightTier.LIGHT.getExtraFee();
            }
            case MEDIUM -> {
                totalPrice = WeightTier.MEDIUM.getExtraFee();
            }
            case HEAVY -> {
                totalPrice = WeightTier.HEAVY.getExtraFee();
            }
            case VERY_HEAVY -> {
                totalPrice = WeightTier.VERY_HEAVY.getExtraFee();
            }
        }
        return totalPrice;
    }

    public BigDecimal calculateShippingFeeByDistance(double distanceKm) {
        int category;

        if (distanceKm >= 0 && distanceKm < 3) {
            category = 0;
        } else if (distanceKm >= 3 && distanceKm < 7) {
            category = 1;
        } else if (distanceKm >= 7 && distanceKm < 10) {
            category = 2;
        } else if (distanceKm >= 10 && distanceKm < 20) {
            category = 3;
        } else {
            category = 4;
        }
        switch (category) {
            case 0:
                return BigDecimal.valueOf(15_000);
            case 1:
                return BigDecimal.valueOf(30_000);
            case 2:
                return BigDecimal.valueOf(50_000);
            case 3:
                return BigDecimal.valueOf(100_000);
            default:
                return BigDecimal.valueOf(150_000);
        }

    }
}
