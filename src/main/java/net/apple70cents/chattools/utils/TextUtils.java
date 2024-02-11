package net.apple70cents.chattools.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.text.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class TextUtils {
    public static final Style WEBSITE_URL_STYLE = Style.EMPTY.withUnderline(true)
                                                             .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://70centsapple.top/blogs/#/chat-tools-faq"))
                                                             .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, trans("faq")));
    public static final String PREFIX = "key.chattools.";

    public static class MessageUnit {
        public Text message;
        public long unixTimestamp;

        public MessageUnit(Text message, long unixTimestamp) {
            this.message = message;
            this.unixTimestamp = unixTimestamp;
        }
    }

    public static int index = -1;
    // For a newly received message, the key is its index and the value is its information
    public static List<MessageUnit> messageMap = new LinkedList<>();

    public static int putMessageMap(Text text, long unixTimestamp) {
        messageMap.add(new MessageUnit(text, unixTimestamp));
        index++;
        return index;
    }

    public static MessageUnit getMessageMap(int idx){
        try {
            return messageMap.get(idx);
        } catch (Exception e){
            return null;
        }
    }


    public static Text literal(String str) {
        //#if MC>=11900
        return Text.literal(str);
        //#else
        //$$return new LiteralText(str);
        //#endif
    }

    public static Text transWithPrefix(String str, String prefix) {
        //#if MC>=11900
        return Text.translatable(prefix + str);
        //#else
        //$$return new TranslatableText(prefix + str);
        //#endif
    }

    public static Text transWithPrefix(String str, String prefix, Object... args) {
        //#if MC>=11900
        return Text.translatable(prefix + str, args);
        //#else
        //$$return new TranslatableText(prefix + str, args);
        //#endif
    }

    public static Text trans(String str, Object... args) {
        return of(transWithPrefix(str, PREFIX, args).getString().strip());
    }

    public static Text trans(String str) {
        return of(transWithPrefix(str, PREFIX).getString().strip());
    }

    public static Text of(String str) {
        return Text.of(str);
    }

    /**
     * removes color codes in the string
     *
     * @param str the string
     * @return string with no color codes
     */
    public static String wash(String str) {
        return Pattern.compile("§.").matcher(str).replaceAll("");
    }

    public static String escapeColorCodes(String str) {
        return str.replace('&', '§').replace("\\§", "&");
    }

    public static String backEscapeColorCodes(String str) {
        return str.replace('§', '&');
    }

    /**
     * replace a {@link MutableText}
     *
     * @param text      the text
     * @param oldString old string
     * @param newString new string
     * @return text after replacement
     */
    public static MutableText replaceText(MutableText text, String oldString, String newString) {
        JsonElement jsonElement = Text.Serialization.toJsonTree(text);
        replaceFieldValue(jsonElement, oldString, newString);
        return Text.Serialization.fromJsonTree(jsonElement);
    }

    private static void replaceFieldValue(JsonElement jsonElement, String oldValue, String newValue) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> ele : jsonObject.entrySet()) {
                String key = ele.getKey();
                JsonElement value = ele.getValue();
                if (value.isJsonPrimitive() && value.getAsString().contains(oldValue)) {
                    jsonObject.addProperty(key, value.getAsString().replace(oldValue, newValue));
                } else {
                    replaceFieldValue(value, oldValue, newValue);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                replaceFieldValue(element, oldValue, newValue);
            }
        }
    }
}
