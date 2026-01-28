package com.example.DATN.services;

import com.example.DATN.dtos.request.SizeRequest;
import com.example.DATN.dtos.respone.SizeResponse;
import com.example.DATN.exception.ApplicationException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.mapper.SizeMapper;
import com.example.DATN.models.Size;
import com.example.DATN.repositories.ColorRepository;
import com.example.DATN.repositories.SizeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SizeService {

    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;
    private final String PREFIX = "SMA";
    private final ColorRepository colorRepository;


    public static String generate(String prefix, long
            index) {
        return prefix + index;
    }
    @Transactional(rollbackOn = Exception.class)
    public List<SizeResponse> createSize(List<SizeRequest> requests) {
        List<SizeResponse> responses = new ArrayList<>();
        for(SizeRequest request: requests) {
            long code = sizeRepository.count();;
            String sizeCode =generate(PREFIX, code);
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
//        List<Color> colors = List.of(
//                Color.builder().code("CL001").name("Đỏ").hexCode("#FF0000").build(),
//                Color.builder().code("CL002").name("Xanh dương").hexCode("#0000FF").build(),
//                Color.builder().code("CL003").name("Xanh lá").hexCode("#00FF00").build(),
//                Color.builder().code("CL004").name("Vàng").hexCode("#FFFF00").build(),
//                Color.builder().code("CL005").name("Cam").hexCode("#FFA500").build(),
//                Color.builder().code("CL006").name("Tím").hexCode("#800080").build(),
//                Color.builder().code("CL007").name("Hồng").hexCode("#FFC0CB").build(),
//                Color.builder().code("CL008").name("Nâu").hexCode("#8B4513").build(),
//                Color.builder().code("CL009").name("Đen").hexCode("#000000").build(),
//                Color.builder().code("CL010").name("Trắng").hexCode("#FFFFFF").build(),
//                Color.builder().code("CL011").name("Xám").hexCode("#808080").build(),
//                Color.builder().code("CL012").name("Be").hexCode("#F5F5DC").build(),
//                Color.builder().code("CL013").name("Xanh ngọc").hexCode("#40E0D0").build(),
//                Color.builder().code("CL014").name("Xanh navy").hexCode("#000080").build(),
//                Color.builder().code("CL015").name("Đỏ rượu vang").hexCode("#800000").build(),
//                Color.builder().code("CL016").name("Vàng nhạt").hexCode("#FFFACD").build(),
//                Color.builder().code("CL017").name("Xanh mint").hexCode("#98FF98").build(),
//                Color.builder().code("CL018").name("Hồng pastel").hexCode("#FFD1DC").build(),
//                Color.builder().code("CL019").name("Xanh lam nhạt").hexCode("#ADD8E6").build(),
//                Color.builder().code("CL020").name("Xanh olive").hexCode("#808000").build()
//        );
//
//        List<Size> sizes = List.of(
//                Size.builder().code("SMA001").name(36).build(),
//                Size.builder().code("SMA002").name(37).build(),
//                Size.builder().code("SMA003").name(38).build(),
//                Size.builder().code("SMA004").name(39).build(),
//                Size.builder().code("SMA005").name(40).build(),
//                Size.builder().code("SMA006").name(41).build(),
//                Size.builder().code("SMA007").name(42).build(),
//                Size.builder().code("SMA008").name(43).build(),
//                Size.builder().code("SMA009").name(44).build(),
//                Size.builder().code("SMA010").name(45).build()
//        );
//        colorRepository.saveAll(colors);
//        sizeRepository.saveAll(sizes);
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
