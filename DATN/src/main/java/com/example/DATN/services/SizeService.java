package com.example.DATN.services;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.SizeMapper;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.SizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;
    private final String PREFIX = "SZ";

    public static String generate(String prefix, long index) {
        return prefix + String.format("%03d", index);
    }
    public SizeResponse createSize(SizeRequest request) {
        Size size = sizeMapper.toSize(request);
        size.setCode(generate(PREFIX, sizeRepository.count() +1));
        return sizeMapper.toSizeResponse(sizeRepository.save(size));
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
