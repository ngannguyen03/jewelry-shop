package com.jeweleryshop.backend.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class JwtKeyGenerator {

    public static void main(String[] args) {
        // TẠO 32 BYTES (256 bits) NGẪU NHIÊN CHO KHÓA BÍ MẬT
        SecureRandom secureRandom = new SecureRandom();
        byte[] key = new byte[32]; // <-- Đảm bảo 32 bytes
        secureRandom.nextBytes(key);

        // Mã hóa bằng Base64 URL Safe, không có padding (an toàn và hợp lệ)
        String safeBase64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key);
        System.out.println("KHÓA BÍ MẬT JWT MỚI (MIN 256 BITS): " + safeBase64Key);
        // SAO CHÉP CHUỖI ĐƯỢC IN RA
    }
}
