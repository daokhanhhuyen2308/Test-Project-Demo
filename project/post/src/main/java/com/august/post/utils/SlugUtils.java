package com.august.post.utils;

import com.august.shared.enums.ErrorCode;
import com.august.shared.exception.AppCustomException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

public class SlugUtils {
    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s");
    public static String slug(String input, boolean isUnique){
        if (input == null) {
            throw new AppCustomException(ErrorCode.INPUT_REQUIREMENT);
        }

        String noWhiteSpace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(noWhiteSpace, Normalizer.Form.NFD);
        String slug = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        slug = slug.replace('đ', 'd').replace('Đ', 'd');

        slug = NON_LATIN.matcher(slug).replaceAll("");

        slug = slug.toLowerCase(Locale.ENGLISH);

        slug = slug.replaceAll("-+", "-");

        if (slug.startsWith("-")) slug = slug.substring(1);
        if (slug.endsWith("-")) slug = slug.substring(0, slug.length() - 1);

        if (isUnique) {
            String timeSuffix = Long.toString(System.currentTimeMillis(), 36);
            slug += "-" + timeSuffix;
        }

        return slug;

    }
}
