package com.flowiee.dms.utils;

import com.flowiee.dms.exception.AuthenticationException;
import com.flowiee.dms.model.MODULE;
import com.flowiee.dms.model.UserPrincipal;
import com.flowiee.dms.utils.constants.CategoryType;
import net.logstash.logback.encoder.org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.File;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

public class CommonUtils {
    public static final String rootPath = "src/main/resources/static";
    public static final String fileUploadPath = rootPath + "/uploads/";
    public static final String templateExportExcelPath = rootPath + "/templates/excel";
    public static Date START_APP_TIME = null;

    public static String getCategoryType(String key) {
        Map<String, String> map = new HashMap<>();
        for (CategoryType c : CategoryType.values()) {
            map.put(c.getKey(), c.getName());
        }
        return map.get(key);
    }

    public static boolean isValidCategory(String type) {
        for (CategoryType c : CategoryType.values()) {
            if (c.getKey().equals(type)) {
                return true;
            }
        }
        return false;
    }

    public static String getFileExtension(String fileName) {
        String extension = "";
        if (ObjectUtils.isNotEmpty(fileName)) {
            int lastIndex = fileName.lastIndexOf('.');
            if (lastIndex > 0 && lastIndex < fileName.length() - 1) {
                extension = fileName.substring(lastIndex + 1);
            }
        }
        return extension;
    }

    public static String getPathDirectory(String systemModule) {
        try {
            StringBuilder path = new StringBuilder(fileUploadPath);
            if (MODULE.STORAGE.name().equals(systemModule)) {
                path.append("storage");
            } else if (MODULE.CATEGORY.name().equals(systemModule)) {
                path.append("category");
            }
            path.append("/").append(LocalDateTime.now().getYear());
            path.append("/").append(LocalDateTime.now().getMonth().getValue());
            path.append("/").append(LocalDateTime.now().getDayOfMonth());
            File folder = new File(path.toString());
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    System.out.println("mkdir OK");
                }
            }
            return path.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    public static String generateAliasName(String text) {
        String transformedText = "";
        if (text != null) {
            // Loại bỏ dấu tiếng Việt và ký tự đặc biệt
            String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
            Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
            String textWithoutAccents = pattern.matcher(normalizedText).replaceAll("");
            String cleanedText = textWithoutAccents.replaceAll("[^a-zA-Z0-9 ]", "");

            // Chuyển đổi thành chữ thường (lowercase)
            String lowercaseText = cleanedText.toLowerCase();

            // Thay thế khoảng trắng bằng dấu gạch ngang ("-")
            transformedText = lowercaseText.replace(" ", "-");

            if (transformedText.endsWith("-")) {
                transformedText = transformedText.substring(0, transformedText.length() - 1);
            }
        }
        return transformedText;
    }

    public static UserPrincipal getUserPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.isAuthenticated()) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        throw new AuthenticationException();
    }

    public static String generateUniqueString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}