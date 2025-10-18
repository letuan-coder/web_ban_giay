package com.example.DATN.controllers;

import com.example.DATN.dtos.request.ghtk.GHTKRequest;
import com.example.DATN.services.GhtkServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ghtk")

public class GhtkController {

    private final GhtkServices ghtkService;

    @PostMapping("/create-order")
    public ResponseEntity<String> createOrder(
            @RequestBody GHTKRequest request) {
        String response = ghtkService.createOrder(request);
        return ResponseEntity.ok(response);
    }

}
