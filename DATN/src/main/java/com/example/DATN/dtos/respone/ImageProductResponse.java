
package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageProductResponse {

    private Long id;
    private String imageUrl;
    private String altText;
}
