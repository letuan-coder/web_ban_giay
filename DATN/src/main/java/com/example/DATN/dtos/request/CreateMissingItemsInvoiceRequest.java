package com.example.DATN.dtos.request;

import lombok.Data;
import java.util.List;

@Data
public class CreateMissingItemsInvoiceRequest {
    private Long originalTransactionId;
    private List<MissingItemDTO> missingItems;
}
