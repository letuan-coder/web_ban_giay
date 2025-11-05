
package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.product.ImageProductResponse;
import com.example.DATN.models.ImageProduct;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ImageProductMapper {
    ImageProductResponse toImageProductResponse(ImageProduct imageProduct);
    List<ImageProductResponse> toImageProductResponses(List<ImageProduct> imageProducts);
    ImageProduct toEntity(ImageProductResponse response);
}
