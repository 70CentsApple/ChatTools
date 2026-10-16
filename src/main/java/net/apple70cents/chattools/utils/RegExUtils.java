package net.apple70cents.chattools.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class RegExUtils {
    private static final Map<String, Pattern> patternCache = new ConcurrentHashMap<>();

    public static Pattern getOrCompilePattern(String regex) {
        return getOrCompilePattern(regex, 0);
    }

    public static Pattern getOrCompilePattern(String regex, int flags) {
        return patternCache.computeIfAbsent(regex + "|" + flags, k -> Pattern.compile(regex, flags));
    }
}
