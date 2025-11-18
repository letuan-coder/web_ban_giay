package com.example.DATN.services;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.SizeMapper;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.SizeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;
    private final String PREFIX = "SZ_";
    private String generateProductCode() {
        return UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }
    public static String generate(String prefix, String index) {
        return prefix + index;
    }
    @Transactional(rollbackOn = Exception.class)
    public List<SizeResponse> createSize(List<SizeRequest> requests) {
        List<SizeResponse> responses = new ArrayList<>();
        for(SizeRequest request: requests) {
            String sizeCode =generate(PREFIX, generateProductCode());
            if(sizeRepository.existsByName(request.getName())==true){
                continue;
            }
            Size size = Size.builder()
                    .code(sizeCode)
                    .name(request.getName())
                    .build();
            responses.add(sizeMapper.toSizeResponse(size));
            sizeRepository.save(size);
        }
        return responses;

    }

    public List<SizeResponse> getSizes() {
        return sizeRepository.findAll().stream()
                .map(sizeMapper::toSizeResponse)
                .toList();
    }

    public SizeResponse getSizeById(String id) {
        return sizeRepository.findByCode(id)
                .map(sizeMapper::toSizeResponse)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
    }

    public SizeResponse updateSize(String id, SizeRequest request) {
        Size size = sizeRepository.findByCode(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
        sizeMapper.updateSize(size, request);
        return sizeMapper.toSizeResponse(sizeRepository.save(size));
    }

    public void deleteSize(String id) {
        Size size = sizeRepository.findByCode(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.SIZE_NOT_FOUND));
        sizeRepository.delete(size);
    }
}
