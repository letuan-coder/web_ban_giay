package com.example.DATN.services;

import com.example.DATN.dtos.request.ColorRequest;
import com.example.DATN.dtos.respone.ColorResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.ColorMapper;
import com.example.DATN.models.Color;
import com.example.DATN.repositories.ColorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;

    private final String PREFIX = "CL";
    public static String generate(String prefix, long index) {
        return prefix + String.format("%03d", index);
    }
    @Transactional(rollbackOn = Exception.class)
    public ColorResponse createColor(ColorRequest request) {

        if (colorRepository.existsByHexCode(request.getHexCode())) {
            throw new ApplicationException(ErrorCode.HEXCODE_ALREADY_EXISTS);
        }
        Color color = new Color();
        color.setCode(generate(PREFIX, colorRepository.count() +1));
        color.setName(request.getName());
        color.setHexCode(request.getHexCode());
        return colorMapper.toColorResponse(colorRepository.save(color));
    }

    public List<ColorResponse> getColors() {
        return colorRepository.findAll().stream()
                .map(colorMapper::toColorResponse)
                .toList();
    }

    public ColorResponse getColorById(String id) {
        return colorRepository.findByCode(id)
                .map(colorMapper::toColorResponse)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
    }

    public ColorResponse updateColor(String id, ColorRequest request) {
        Color color = colorRepository.findByCode(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
        colorMapper.updateColor(color, request);
        return colorMapper.toColorResponse(colorRepository.save(color));
    }

    public void deleteColor(String id) {
        Color color = colorRepository.findByCode(id)
                .orElseThrow(() -> new ApplicationException(ErrorCode.COLOR_NOT_FOUND));
        colorRepository.delete(color);
    }
}