package com.example.DATN.helper;

import org.springframework.stereotype.Component;

@Component
public class FormatInputString {
    public String formatInputString(String name) {
        if (name == null || name.isBlank()) return name;

        name = name.trim().toLowerCase(); // Chuyển toàn bộ về chữ thường
        String[] words = name.split("\\s+"); // Tách theo khoảng trắng
        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                formatted.append(Character.toUpperCase(word.charAt(0)))  // Viết hoa chữ đầu
                        .append(word.substring(1))                      // Giữ phần còn lại
                        .append(" ");
            }
        }
        return formatted.toString().trim(); // Xóa khoảng trắng thừa cuối cùng
    }
}
