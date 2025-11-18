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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ColorService {

    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;

    private final String PREFIX = "CL_";
    private String generateColorCode() {
        return UUID.randomUUID().toString().substring(0, 5).toUpperCase();
    }
    public static String generate(String prefix, String index) {
        return prefix + index;
    }
    @Transactional(rollbackOn = Exception.class)
    public List<ColorResponse> addColorList(List<ColorRequest> requests) {
        List<ColorResponse> responses = new ArrayList<>();
        for (ColorRequest request: requests) {
            String index = generateColorCode();
            if (colorRepository.existsByHexCode(request.getHexCode())) {
                throw new ApplicationException(ErrorCode.HEXCODE_ALREADY_EXISTS);
            }
            Color color =  Color.builder()
                    .code(generate(PREFIX, index))
                    .name(request.getName())
                    .hexCode(request.getHexCode())
                    .build();
            responses.add(colorMapper.toColorResponse(color));
            colorRepository.save(color);
            }
        return responses;
    }
    public ColorResponse createColor(ColorRequest request){
        String index = generateColorCode();
        if (colorRepository.existsByHexCode(request.getHexCode())) {
            throw new ApplicationException(ErrorCode.HEXCODE_ALREADY_EXISTS);
        }
        Color color =  Color.builder()
                .code(generate(PREFIX, index))
                .name(request.getName())
                .hexCode(request.getHexCode())
                .build();
        colorRepository.save(color);
       return colorMapper.toColorResponse(color);
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