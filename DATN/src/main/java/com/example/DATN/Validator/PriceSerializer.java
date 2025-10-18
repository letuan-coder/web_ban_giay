package com.example.DATN.Validator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class PriceSerializer extends JsonSerializer<BigDecimal> {
    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {

        if (value == null) {
            gen.writeNull();
            return;
        }

        // Làm tròn xuống đến đồng (0 số thập phân)
        BigDecimal rounded = value.setScale(0, BigDecimal.ROUND_DOWN);

        // Định dạng theo kiểu Việt Nam: dùng "." ngăn cách hàng nghìn
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setGroupingSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#,###", symbols);
        String formatted = formatter.format(rounded);

        gen.writeString(formatted + " VND");
    }
}
