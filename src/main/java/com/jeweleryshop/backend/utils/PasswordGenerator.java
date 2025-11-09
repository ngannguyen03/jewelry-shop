package com.jeweleryshop.backend.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "12345678";
        String encodedPassword = encoder.encode(rawPassword);

        System.out.println("-------------------------------------------------------------------------------------------------");
        System.out.println("MẬT KHẨU MỚI CHO '12345678':");
        // CHUỖI NÀY CÓ THỂ KHÁC NHAU MỖI LẦN CHẠY
        System.out.println(encodedPassword);
        System.out.println("-------------------------------------------------------------------------------------------------");

        // SAO CHÉP CHUỖI BẮT ĐẦU BẰNG $2A$10$...
    }
}
