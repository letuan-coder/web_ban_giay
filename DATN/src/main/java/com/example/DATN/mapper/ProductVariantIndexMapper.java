//package com.example.DATN.mapper;
//
//import com.example.DATN.models.ProductVariant;
//import com.example.DATN.models.ProductVariantIndex;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ProductVariantIndexMapper {
//    public ProductVariantIndex toIndex(ProductVariant entity) {
//        ProductVariantIndex index = new ProductVariantIndex();
//
//        index.setId(entity.getId());
//        index.setProductId(entity.getProductColor().getProduct().getId().toString());
//        index.setSku(entity.getSku());
//        index.setName(entity.getProductColor().getProduct().getName());
//        index.setColor(entity.getProductColor().getColor().getName());
//        index.setSize(entity.getSize().getName().toString());
//        index.setPrice(entity.getPrice());
//
//        return index;
//    }
//}
