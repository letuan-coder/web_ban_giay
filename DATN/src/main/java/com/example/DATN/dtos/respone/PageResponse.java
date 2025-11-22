package com.example.DATN.dtos.respone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageResponse<T> {
    private Integer page;
    private Integer size;
    private Integer limit;
    private Long totalElements;
    private Integer totalPages;
    private List<T> content;
}