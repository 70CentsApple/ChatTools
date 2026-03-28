package net.apple70cents.chattools.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.chat.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//? if >=1.21.6 {
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;

import java.net.URI;
//?} elif >=1.21.5 {
/*import net.minecraft.data.registries.VanillaRegistries;
import java.net.URI;
*///?} elif >=1.20.5 {
/*import net.minecraft.data.registries.VanillaRegistries;
*///?}

/**
 * @author 70CentsApple
 */
public class TextUtils {
    public static final Style WEBSITE_URL_STYLE = Style.EMPTY.withUnderlined(true).withClickEvent(
//? if >=1.21.5 {
            new ClickEvent.OpenUrl(URI.create("https://70centsapple.top/blogs/#/chat-tools-faq"))
//?} else {
/*new ClickEvent(ClickEvent.Action.OPEN_URL, "https://70centsapple.top/blogs/#/chat-tools-faq")
*///?}
    ).withHoverEvent(
//? if >=1.21.5 {
            new HoverEvent.ShowText(
//?} else {
/*new HoverEvent(HoverEvent.Action.SHOW_TEXT,
*///?}
                    ConfigScreenTooltipUtils.getTooltip("general.FAQ", "FAQ", null)));
    public static final String PREFIX = "key.chattools.";
    public static final Component SPACER = literal("").copy().setStyle(Style.EMPTY);

    public static class MessageUnit {
        public Component message;
        public Component visualMessage;
        public long unixTimestamp;
        public int occurrenceCount;
        public boolean notViaChatPipeline = false;

        public MessageUnit(Component message, Component visualMessage, long unixTimestamp, int occurrenceCount, boolean notViaChatPipeline) {
            this.message = message;
            this.visualMessage = visualMessage;
            this.unixTimestamp = unixTimestamp;
            this.occurrenceCount = occurrenceCount;
            this.notViaChatPipeline = notViaChatPipeline;
        }
    }

    // For a newly received message, the key is its hashcode and the value is its MessageUnit
    public static Map<String, MessageUnit> messageMap = new LinkedHashMap<>();
    public static MessageUnit latestMessage = null;

    /**
     * Generates a random string conducted by 0-9,a-z
     *
     * @param length the length
     * @return the random string
     */
    public static String generateRandomString(int length) {
        final String CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";
        final Random RANDOM = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(index));
        }
        return stringBuilder.toString();
    }

    public static MessageUnit getLatestMessage() {
        return latestMessage;
    }

    protected static void setLatestMessage(MessageUnit unit) {
        latestMessage = unit;
    }

    public static void putMessageMapWithHashcode(String hashcode, MessageUnit messageUnit) {
        int maxSize = ((Number) ConfigUtils.get("general.MaxHistoryLength")).intValue();
        while (messageMap.size() > maxSize) {
            // pops the first element
            messageMap.remove(messageMap.keySet().iterator().next());
        }

        messageMap.put(hashcode, messageUnit);
        setLatestMessage(messageUnit);
    }

    public static String generateHashcode(Component message) {
        // TODO The `hashcode` is NOT REALLY a hashcode, but actually just a random string
        String hashcode = generateRandomString(6);
        int retries = 0;
        while (messageMap.containsKey(hashcode) && retries < 10) {
            hashcode = generateRandomString(6);
            retries++;
        }
        return hashcode;
    }

    public static MessageUnit getMessageUnitByHash(String hash) {
        try {
            return messageMap.get(hash);
        } catch (Exception e) {
            return null;
        }
    }


    public static Component literal(String str) {
//? if >=1.19 {
        return Component.literal(str);
//?} else {
       /*return new TextComponent(str);
*///?}
    }

    public static Component transWithPrefix(String str, String prefix) {
//? if >=1.19 {
        return Component.translatable(prefix + str);
//?} else {
       /*return new TranslatableComponent(prefix + str);
*///?}
    }

    public static Component transWithPrefix(String str, String prefix, Object... args) {
//? if >=1.19 {
        return Component.translatable(prefix + str, args);
//?} else {
       /*return new TranslatableComponent(prefix + str, args);
*///?}
    }

    public static Component trans(String str, Object... args) {
        return transWithPrefix(str, PREFIX, args);
    }

    public static Component trans(String str) {
        return transWithPrefix(str, PREFIX);
    }

    public static Component of(String str) {
        return Component.nullToEmpty(str);
    }

    public static Component empty() {
//? if >=1.19 {
        return Component.empty();
//?} else {
       /*return of("");
*///?}
    }

    /**
     * removes color codes in the string
     *
     * @param str the string
     * @return string with no color codes
     */
    public static String wash(String str) {
        return RegExUtils.getOrCompilePattern("§.").matcher(str).replaceAll("");
    }

    /**
     * turn '&' into REAL color codes in the string
     * it will not modify '\&'
     *
     * @param str the string
     * @return string with color codes
     */
    public static String encodeColorCodes(String str) {
        return str.replace('&', '§').replace("\\§", "&");
    }

    /**
     * turn REAL color codes into '&' in the string
     *
     * @param str the string
     * @return a string, in which color codes are turned into '&'
     */
    public static String decodeColorCodes(String str) {
        return str.replace('§', '&');
    }

    public static Component textArray2text(List<Component> texts) {
        MutableComponent result = (MutableComponent) literal("");
        for (int i = 0; i < texts.size(); i++) {
            result.append(texts.get(i));
            if (i != texts.size() - 1) {
                result.append(literal("\n"));
            }
        }
        return result;
    }

    public static JsonElement component2JsonElement(MutableComponent text) {
        try {
//? if >=1.21.6 {
            JsonElement jsonElement = ComponentSerialization.CODEC.encode(text,
                    RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE), null).result().orElse(null);
//?} elif >=1.20.5 {
            /*JsonElement jsonElement = new Component.SerializerAdapter(VanillaRegistries.createLookup()).serialize(text, null, null);
*///?} else {
            /*JsonElement jsonElement = Component.Serializer.toJsonTree(text);
*///?}
            return jsonElement;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MutableComponent jsonElement2Component(JsonElement jsonElement) {
        try {
//? if >=1.21.6 {
            return ComponentSerialization.CODEC.parse(RegistryAccess.EMPTY.createSerializationContext(JsonOps.INSTANCE),
                    jsonElement).result().orElse(null).copy();
//?} elif >=1.20.5 {
            /*return new Component.SerializerAdapter(VanillaRegistries.createLookup()).deserialize(jsonElement, null, null);
*///?} else {
            /*return Component.Serializer.fromJson(jsonElement);
*///?}
        } catch (Exception e) {
            e.printStackTrace();
            return TextUtils.literal("ERROR").copy();
        }
    }

    /**
     * replace keywords in a {@link MutableComponent}
     *
     * @param text             the text
     * @param oldStringPattern the RegEx pattern of the old string
     * @param newString        new string
     * @return text after replacement
     */
    public static MutableComponent replaceComponentText(MutableComponent text, Pattern oldStringPattern, String newString) {
        JsonElement jsonElement = component2JsonElement(text);
        replaceTextFieldValue(jsonElement, oldStringPattern, newString, null);
        return jsonElement2Component(jsonElement);
    }

    private static void replaceTextFieldValue(JsonElement jsonElement, Pattern oldValuePattern, String newValue, String parentKey) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> ele : jsonObject.entrySet()) {
                String key = ele.getKey();
                JsonElement value = ele.getValue();

                if (isProtectedField(key)) {
                    continue;
                }

                if (isTextField(key) && value.isJsonPrimitive()) {
                    Matcher matcher = oldValuePattern.matcher(value.getAsString());
                    if (matcher.find()) {
                        jsonObject.addProperty(key, matcher.replaceAll(newValue));
                    }
                } else {
                    replaceTextFieldValue(value, oldValuePattern, newValue, key);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonElement element = jsonArray.get(i);

                if (element.isJsonPrimitive()) {
                    if (parentKey != null && isTextField(parentKey)) {
                        Matcher matcher = oldValuePattern.matcher(element.getAsString());
                        if (matcher.find()) {
                            jsonArray.set(i, new com.google.gson.JsonPrimitive(matcher.replaceAll(newValue)));
                        }
                    }
                } else {
                    replaceTextFieldValue(element, oldValuePattern, newValue, parentKey);
                }
            }
        }
    }

    /**
     * replace the colors of a {@link MutableComponent} into white
     *
     * @param text the text
     * @return text after replacement
     */
    public static MutableComponent replaceComponentColor(MutableComponent text) {
        JsonElement jsonElement = component2JsonElement(text);
        replaceColorFieldValue(jsonElement);
        return jsonElement2Component(jsonElement);
    }

    private static void replaceColorFieldValue(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            for (Map.Entry<String, JsonElement> ele : jsonObject.entrySet()) {
                String key = ele.getKey();
                JsonElement value = ele.getValue();

                if (isProtectedField(key)) {
                    continue;
                }

                if (isTextField(key) && value.isJsonPrimitive()) {
                    jsonObject.addProperty(key, wash(value.getAsString()));
                } else if (isColorField(key) && value.isJsonPrimitive()) {
                    jsonObject.addProperty(key, "white");
                } else {
                    replaceColorFieldValue(value);
                }
            }
        } else if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            for (JsonElement element : jsonArray) {
                replaceColorFieldValue(element);
            }
        }
    }

    private static boolean isProtectedField(String key) {
        return key.contains("enchantments") || key.contains("tag");
    }

    private static boolean isTextField(String key) {
        return key.contains("text") || key.contains("value") || key.contains("extra");
    }

    private static boolean isColorField(String key) {
        return key.contains("color");
    }
}
