package com.example.DATN.mapper;

import com.example.DATN.models.Product;
import com.example.DATN.models.ProductVariantIndex;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring",uses = {ProductMapper.class
        ,ProductColorMapper.class,SizeMapper.class})
public interface ProductVariantIndexMapper {
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
            @Mapping(target = "productCode",source = "productCode")
            @Mapping(target = "name",source = "name")
            @Mapping(target = "productId",source = "id")
            @Mapping(target = "price",source = "price")
            @Mapping(target = "thumbnailUrl",source = "thumbnailUrl")
         ProductVariantIndex toIndexProduct(Product product );


}
