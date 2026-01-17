package com.example.DATN.controllers;

import com.example.DATN.dtos.request.product.ProductVariantRequest;
import com.example.DATN.dtos.request.product.UpdateProductVariantRequest;
import com.example.DATN.dtos.respone.ApiResponse;
import com.example.DATN.dtos.respone.product.ProductVariantResponse;
import com.example.DATN.models.ProductColor;
import com.example.DATN.models.ProductVariant;
import com.example.DATN.models.ProductVariantIndex;
import com.example.DATN.repositories.ProductColorRepository;
import com.example.DATN.repositories.ProductVariantRepository;
import com.example.DATN.services.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    private final ProductVariantRepository productVariantRepository;
    private final ProductColorRepository productColorRepository;
    private final ElasticsearchOperations operations;

    @GetMapping("/search")
    public List<ProductVariantIndex> search
            (@RequestParam String keyword,
             @RequestParam(defaultValue = "0") int page,
             @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(b -> b.should(
                                sh -> sh.match(m -> m.field("name").query(keyword))
                        ).should(
                                sh -> sh.match(m -> m.field("color").query(keyword))
                        ).should(
                                sh -> sh.match(m -> m.field("size").query(keyword))
                        ).should(
                                sh -> sh.wildcard(w -> w.field("sku").value("*" + keyword + "*"))
                        )
                ))
                .withPageable(pageable)
                .build();
        SearchHits<ProductVariantIndex> result =
                operations.search(query, ProductVariantIndex.class);
        return result.getSearchHits().stream()
                .map(SearchHit::getContent)
                .toList();
    }



    @PostMapping(value = "/list/{product_color_id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> createListProductVariant(
            @PathVariable UUID product_color_id,
            @Valid @RequestBody ProductVariantRequest variantRequest) {
        List<ProductVariantResponse> responses = productVariantService.
                createListProductVariant(product_color_id, variantRequest);
        List<UUID> ids = responses.stream().map(ProductVariantResponse::getId)
                .collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
//        List<ProductVariantIndex> variantIndices = variants.stream()
//                .map(productVariantIndexMapper::toIndex)
//                .collect(Collectors.toList());
//        indexRepository.saveAll(variantIndices);

        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(responses)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductVariantResponse> getProductVariantById(
            @PathVariable UUID id) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.getProductVariantById(id))
                .build();
    }


    @GetMapping("/sku/{sku}")
    public ApiResponse<ProductVariantResponse> getProductVariantBySKU(
            @PathVariable String sku) {
        return ApiResponse.<ProductVariantResponse>builder()
                .data(productVariantService.getProductVariantBySKU(sku))
                .build();
    }

    @PatchMapping("/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> updateProductVariantByColor(
            @PathVariable UUID id,
            @RequestBody @Valid List<UpdateProductVariantRequest> listofupdaterequest) {
        List<ProductVariantResponse> responses = productVariantService.updateProductVariant(id, listofupdaterequest);

        List<UUID> ids = responses.stream().map(ProductVariantResponse::getId).collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
//        List<ProductVariantIndex> variantIndices = variants.stream()
//                .map(productVariantIndexMapper::toIndex)
//                .collect(Collectors.toList());
//        indexRepository.saveAll(variantIndices);

        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(responses)
                .build();
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductVariant(@PathVariable UUID id) {
        productVariantService.deleteProductVariant(id);
//        indexRepository.deleteById(id);
        return ApiResponse.<Void>builder().build();
    }

    @DeleteMapping("/colors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteProductColor(@PathVariable UUID id) {
        Optional<ProductColor> productColor = productColorRepository.findById(id);
        List<ProductVariant> variantsToDelete = productVariantRepository.findAllByproductColor(productColor.get());
//        if (variantsToDelete != null && !variantsToDelete.isEmpty()) {
//            List<UUID> variantIds = variantsToDelete.stream().map(ProductVariant::getId).collect(Collectors.toList());
//            indexRepository.deleteAllById(variantIds);
//        }
        productVariantService.deleteProductColor(id);
        return ApiResponse.<Void>builder().build();
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> updateProductVariant(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateProductVariantRequest request) {
        List<ProductVariantResponse> responses = productVariantService
                .UpdateProductVariantById(id, request);
        List<UUID> ids = responses.stream().map(ProductVariantResponse::getId)
                .collect(Collectors.toList());
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
//        List<ProductVariantIndex> variantIndices = variants.stream()
//                .map(productVariantIndexMapper::toIndex)
//                .collect(Collectors.toList());
//        indexRepository.saveAll(variantIndices);
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(responses)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-variants")
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> GetAllVariants() {
        ApiResponse response = ApiResponse.<List<ProductVariantResponse>>builder()
                .data(productVariantService.getallproductvariant())
                .build();
        return ResponseEntity.ok(response);
    }
}
