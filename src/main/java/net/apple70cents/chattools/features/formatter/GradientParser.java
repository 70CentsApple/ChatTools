package net.apple70cents.chattools.features.formatter;

import net.apple70cents.chattools.utils.RegExUtils;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GradientParser {
    public static class Color {
        private final int red;
        private final int green;
        private final int blue;

        private Color(int r, int g, int b) {
            this.red = clamp(r);
            this.green = clamp(g);
            this.blue = clamp(b);
        }

        public static Color fromHex(String hex) {
            if (hex.startsWith("#")) {
                hex = hex.substring(1);
            }
            int length = hex.length();

            if (length == 3) { // short #RGB format
                hex = "" + hex.charAt(0) + hex.charAt(0) + hex.charAt(1) + hex.charAt(1) +
                        hex.charAt(2) + hex.charAt(2);
            } else if (length != 6) {
                return null;
            }

            try {
                int rgb = Integer.parseInt(hex, 16);
                return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        public int getRed() {
            return red;
        }

        public int getGreen() {
            return green;
        }

        public int getBlue() {
            return blue;
        }

        private static int clamp(int value) {
            return Math.max(0, Math.min(255, value));
        }
    }

    public static String parse(String input) {
        Pattern pattern = RegExUtils.getOrCompilePattern(
                "<gradient:(\\d+):\\[([^\\]]+)\\](?::(\\d+))?>(.*?)</gradient>",
                Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

        Matcher matcher = pattern.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String version = matcher.group(1);
            String colorsStr = matcher.group(2);
            int segmentLength = parseSegmentLength(matcher.group(3));
            String text = matcher.group(4).trim();

            BiFunction<Color, String, String> format = getFormat(version);
            String replacement = processGradient(text, colorsStr, format, segmentLength);

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    private static BiFunction<Color, String, String> getFormat(String formatVersion) {
        switch (formatVersion) {
            case "2":
                return ((color, s) -> String.format("[COLOR=#%02X%02X%02X]%s[/COLOR]", color.getRed(), color.getGreen(),
                        color.getBlue(), s));
            case "3":
                return ((color, s) -> String.format("{&#%02X%02X%02X}%s", color.getRed(), color.getGreen(),
                        color.getBlue(), s));
            case "4":
                return ((color, s) -> String.format("<#%02X%02X%02X>%s", color.getRed(), color.getGreen(),
                        color.getBlue(), s));
            case "5":
                return ((color, s) -> String.format("<##%02X%02X%02X>%s", color.getRed(), color.getGreen(),
                        color.getBlue(), s));
            case "1":
            default:
                return ((color, s) -> String.format("&#%02X%02X%02X%s", color.getRed(), color.getGreen(),
                        color.getBlue(), s));
        }
    }

    private static int parseSegmentLength(String input) {
        try {
            int size = Integer.parseInt(input);
            return size > 0 ? size : 1;
        } catch (Exception e) {
            return 1;
        }
    }

    private static String processGradient(String text, String colorsStr, BiFunction<Color, String, String> format, int segmentLength) {
        List<Color> colors = parseColors(colorsStr);
        if (colors.isEmpty() || text.isEmpty()) return text;

        // get grapheme clusters
        List<String> graphemes = new ArrayList<>();
        BreakIterator boundary = BreakIterator.getCharacterInstance();
        boundary.setText(text);
        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {
            graphemes.add(text.substring(start, end));
        }

        // don't count whitespaces when dividing groups, but they should remain in the final output
        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = new StringBuilder();
        int colorableCount = 0;

        for (int i = 0; i < graphemes.size(); i++) {
            String grapheme = graphemes.get(i);
            if (!grapheme.trim().isEmpty()) { // whitespaces won't contribute to colorableCount
                colorableCount++;
            }
            currentSegment.append(grapheme);

            if (colorableCount == segmentLength || i == graphemes.size() - 1) { // reach length or at the end
                segments.add(currentSegment.toString()); // add to segments list
                currentSegment.setLength(0); // clear currentSegment
                colorableCount = 0;
            }
        }

        // apply colors to them!!
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            double ratio = (segments.size() == 1) ? 0.0 : (double) i / (segments.size() - 1);
            Color color = interpolate(colors, ratio);
            result.append(format.apply(color, segments.get(i)));
        }

        return result.toString();
    }

    private static List<Color> parseColors(String colorsStr) {
        String[] hexArray = colorsStr.split(",");
        List<Color> colors = new ArrayList<>();

        for (String hex : hexArray) {
            Color color = Color.fromHex(hex.trim());
            if (color != null) {
                colors.add(color);
            }
        }
        return colors;
    }

    private static Color interpolate(List<Color> colors, double t) {
        if (colors.size() == 1) return colors.get(0);

        double segment = 1.0 / (colors.size() - 1);
        int index = Math.min((int) (t / segment), colors.size() - 2);
        double localT = (t - index * segment) / segment;

        Color start = colors.get(index);
        Color end = colors.get(index + 1);

        return new Color(lerp(start.getRed(), end.getRed(), localT), lerp(start.getGreen(), end.getGreen(), localT),
                lerp(start.getBlue(), end.getBlue(), localT));
    }

    private static int lerp(int start, int end, double t) {
        return (int) Math.round(start * (1 - t) + end * t);
    }
}