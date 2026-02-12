package net.apple70cents.chattools.features.formatter;

import net.apple70cents.chattools.config.SpecialUnits;
import net.apple70cents.chattools.utils.*;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Formatter {
    public static String work(String msg) {
        for (String s : (List<String>) ConfigUtils.get("formatter.DisableOnMatchList")) {
            if (RegExUtils.getOrCompilePattern(s, Pattern.MULTILINE).matcher(msg).matches()) {
                // return in advance, and don't work with it.
                return msg;
            }
        }
        boolean matched = false;
        String formatter = "{text}";
        for (SpecialUnits.FormatterUnit unit : SpecialUnits.FormatterUnit.fromList(
                (List) ConfigUtils.get("formatter.List"))) {
            if ("*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                    .matcher(ContextUtils.getSessionIdentifier())
                    .matches()) {
                matched = true;
                formatter = unit.formatter;
                // we just need the first match result, break immediately.
                break;
            }
        }

        if ((boolean) ConfigUtils.get("formatter.PreparsePlaceholdersEnabled")) {
            PlaceholderEngine.addNewTempMapping("text", args -> parse(msg));
        } else {
            PlaceholderEngine.addNewTempMapping("text", args -> msg);
        }

        String processed;
        if (matched) {
            LoggerUtils.info("[ChatTools] Chat Formatted.");
            processed = formatter;
        } else {
            processed = "{text}";
        }
        processed = parse(processed);
        PlaceholderEngine.clearTempMappings();

        if (processed.length() <= ((Number) ConfigUtils.get("formatter.DisableThreshold")).intValue()) {
            return processed;
        } else {
            return msg;
        }
    }

    public static String parse(String msg) {
        return GradientParser.parse(PlaceholderEngine.apply(msg));
    }
}
