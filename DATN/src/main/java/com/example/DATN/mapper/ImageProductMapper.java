
package com.example.DATN.mapper;

import com.example.DATN.dtos.respone.ImageProductResponse;
import com.example.DATN.models.ImageProduct;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ImageProductMapper {
    ImageProductResponse toImageProductResponse(ImageProduct imageProduct);
    ImageProduct toEntity(ImageProductResponse response);
}
