package com.example.DATN.controllers;

import com.example.DATN.dtos.request.ghtk.GhnOrderInfo;
import com.example.DATN.dtos.respone.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/webhook/ghn")
@RequiredArgsConstructor
public class GhnController{

    @PostMapping("/order-status")
    public ApiResponse<?> createOrderGHN (GhnOrderInfo info){
    return null;
    }
}
