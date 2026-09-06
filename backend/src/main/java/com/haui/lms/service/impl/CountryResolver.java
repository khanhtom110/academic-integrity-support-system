package com.haui.lms.service.impl;

import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Doi qua lai giua ma quoc gia ISO va ten quoc gia, va doc ten nuoc tu chuoi don vi cong tac in tren bai bao.
 * <p>
 * Bang ten nuoc dung ngay thu vien chuan cua Java nen khong phai goi API nao, chi bo sung them mot so cach viet tat hay
 * gap tren bai bao ma thu vien khong biet.
 */
final class CountryResolver {

    /**
     * Ten nuoc gan nhu luon nam o cuoi chuoi dia chi. Chi quet nguoc tu cuoi len bay nhieu doan de tranh bat nham ten
     * duong hay ten thanh pho trung voi ten nuoc, vi du "Jordan Road" o Hong Kong hay "Atlanta, Georgia, USA".
     */
    private static final int TRAILING_SEGMENTS_TO_SCAN = 3;

    private static final Map<String, String> CODE_BY_NAME = buildNameIndex();

    private CountryResolver() {
    }

    /**
     * Doi ma ISO sang ten tieng Anh. Khong nhan ra thi tra lai chinh ma do.
     */
    static String nameOf(String code) {
        if (!StringUtils.hasText(code)) {
            return code;
        }
        String name = Locale.of("", code.trim().toUpperCase(Locale.ROOT)).getDisplayCountry(Locale.ENGLISH);
        return StringUtils.hasText(name) && !name.equals(code) ? name : code;
    }

    /**
     * Doc ma quoc gia tu chuoi don vi cong tac nguyen ban, vi du "Department of Physics, The Institute of Science, 15
     * Madam Cama Road, Mumbai, 400032, India" tra ve "IN".
     *
     * @return ma ISO, hoac null neu khong nhan ra nuoc nao
     */
    static String codeFromAffiliation(String rawAffiliation) {
        if (!StringUtils.hasText(rawAffiliation)) {
            return null;
        }

        String[] segments = rawAffiliation.split(",");
        int stopAt = Math.max(0, segments.length - TRAILING_SEGMENTS_TO_SCAN);

        for (int i = segments.length - 1; i >= stopAt; i--) {
            String code = CODE_BY_NAME.get(normalize(segments[i]));
            if (code != null) {
                return code;
            }
        }
        return null;
    }

    private static Map<String, String> buildNameIndex() {
        Map<String, String> index = new HashMap<>();

        for (String code : Locale.getISOCountries()) {
            String name = Locale.of("", code).getDisplayCountry(Locale.ENGLISH);
            if (StringUtils.hasText(name)) {
                index.putIfAbsent(normalize(name), code);
            }
        }

        // Cach viet hay gap tren bai bao ma thu vien chuan khong nhan ra
        index.put("usa", "US");
        index.put("u s a", "US");
        index.put("united states of america", "US");
        index.put("uk", "GB");
        index.put("great britain", "GB");
        index.put("england", "GB");
        index.put("scotland", "GB");
        index.put("wales", "GB");
        index.put("northern ireland", "GB");
        index.put("korea", "KR");
        index.put("south korea", "KR");
        index.put("republic of korea", "KR");
        index.put("north korea", "KP");
        index.put("russia", "RU");
        index.put("russian federation", "RU");
        index.put("vietnam", "VN");
        index.put("viet nam", "VN");
        index.put("iran", "IR");
        index.put("taiwan", "TW");
        index.put("prc", "CN");
        index.put("pr china", "CN");
        index.put("p r china", "CN");
        index.put("peoples republic of china", "CN");
        index.put("czech republic", "CZ");
        index.put("czechia", "CZ");
        index.put("the netherlands", "NL");
        index.put("holland", "NL");
        index.put("uae", "AE");
        index.put("turkiye", "TR");

        return index;
    }

    /**
     * Bo dau cham, gach noi va khoang trang thua de "U.S.A." va "U S A" deu khop cung mot muc.
     */
    private static String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[.\\-_]", " ").replaceAll("[^a-z ]", " ")
                .replaceAll("\\s+", " ").trim();
    }
}
