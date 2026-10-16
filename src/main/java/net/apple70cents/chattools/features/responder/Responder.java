package net.apple70cents.chattools.features.responder;

import com.google.gson.JsonElement;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.SpecialUnits;
import net.apple70cents.chattools.features.filter.ChatFilter;
import net.apple70cents.chattools.utils.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 70CentsApple
 */
public class Responder {
    private static final Minecraft mc = Minecraft.getInstance();
    public static long lastRequestTimestamp = -1L;

    public static boolean shouldWork(Component message) {
        boolean enabled = ConfigUtils.CHAT_TOOLS_ENABLED && ConfigUtils.getBoolean("responder.Enabled");
        // obviously, we should not respond to our own messages
        boolean notJustSent = !MessageUtils.hadJustSentMessage();
        boolean filterPassed = ChatFilter.shouldFilter(message) ? ConfigUtils.getBoolean("responder.RespondToFilteredMessages") : true;
        boolean awaitTimePassed = System.currentTimeMillis() - lastRequestTimestamp >= ConfigUtils.getLong("responder.MinAwaitTimeInMilliseconds");
        return enabled && notJustSent && filterPassed && awaitTimePassed;
    }

    public static void work(Component text) {
        String messageReceived = TextUtils.wash(text.getString());
        for (SpecialUnits.ResponderRuleUnit unit : SpecialUnits.ResponderRuleUnit.fromList((List) ConfigUtils.get("responder.List"))) {
            if ("*".equals(unit.address) || RegExUtils.getOrCompilePattern(unit.address)
                    .matcher(ContextUtils.getSessionIdentifier()).matches()) {
                if (RegExUtils.getOrCompilePattern(unit.pattern, Pattern.MULTILINE).matcher(messageReceived).find()) {
                    for (SpecialUnits.ResponderCommandEntry entry : unit.commands) {
                        long delayInMilliseconds = entry.minDelayInMilliseconds;
                        if (entry.maxDelayInMilliseconds > entry.minDelayInMilliseconds) {
                            delayInMilliseconds = java.util.concurrent.ThreadLocalRandom.current()
                                    .nextLong(entry.minDelayInMilliseconds, entry.maxDelayInMilliseconds + 1);
                        }
                        makeMessageSchedule(text, unit.pattern, entry.command, delayInMilliseconds, entry.forceDisableFormatter);
                    }
                    break;
                }
            }
        }
    }

    public static void makeMessageSchedule(Component messageReceived, String pattern, String msg, long delayInMilliseconds, boolean forceDisableFormatter) {
        LoggerUtils.info("[ChatTools] Will respond within " + delayInMilliseconds + "ms");
        lastRequestTimestamp = System.currentTimeMillis();
        long timeOnRequest = java.time.Instant.now().getEpochSecond();
        JsonElement jsonElement = TextUtils.component2JsonElement(messageReceived.copy());
        String jsonString = jsonElement != null ? jsonElement.toString() : "ERROR";
        TaskScheduler.schedule(() -> {
            // Resolve placeholders and send on the client thread so commands do not share temporary mappings.
            mc.execute(() -> {
                String message;
                try {
                    submitAllGroupsToPlaceholderEngine(TextUtils.wash(messageReceived.getString()), pattern);
                    PlaceholderEngine.addNewTempMapping("unix_time_on_request", args -> String.valueOf(timeOnRequest));
                    PlaceholderEngine.addNewTempMapping("original_text_component", args -> jsonString);
                    PlaceholderEngine.addNewTempMapping("original_text_raw", args -> messageReceived.getString());
                    PlaceholderEngine.addNewTempMapping("original_text_string", args -> TextUtils.wash(messageReceived.getString()));
                    PlaceholderEngine.addNewTempMapping("object_data", args -> messageReceived.toString());
                    message = PlaceholderEngine.apply(msg);
                } finally {
                    PlaceholderEngine.clearTempMappings();
                }

                LoggerUtils.info("[ChatTools] Respond to `" + pattern + "`, with message `" + message + "`");
                message = message.replace("\\{", "{").replace("\\}", "}");
                MessageUtils.sendToPublicChat(message, forceDisableFormatter);
            });
        }, delayInMilliseconds);
    }

    /**
     * Extract all named groups from rawPattern matched in rawMessageReceived and submit them to PlaceholderEngine as temp mappings.
     *
     * @param rawMessageReceived the raw message
     * @param rawPattern the raw pattern
     */
    static void submitAllGroupsToPlaceholderEngine(String rawMessageReceived, String rawPattern) {
        Pattern pattern = RegExUtils.getOrCompilePattern(rawPattern);
        Matcher matcher = pattern.matcher(rawMessageReceived);
        if (matcher.find()) {
            // Extract group names from pattern
            Pattern groupNamePattern = RegExUtils.getOrCompilePattern("\\(\\?<([a-zA-Z][a-zA-Z0-9_]*)>");
            Matcher groupNameMatcher = groupNamePattern.matcher(rawPattern);
            while (groupNameMatcher.find()) {
                String groupName = groupNameMatcher.group(1);
                String context;
                try {
                    context = matcher.group(groupName);
                } catch (IllegalArgumentException e) {
                    // group not found, skip
                    continue;
                }
                if (context != null && !context.isBlank()) {
                    final String finalContext = context;
                    PlaceholderEngine.addNewTempMapping(groupName, args -> finalContext);
                }
            }
        }
    }
}
