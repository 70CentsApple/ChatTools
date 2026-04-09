package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class ChatCompactor {
    public static Component appendTrailing(Component msg, int occurrencesCount) {
        if (occurrencesCount <= 1) {
            return msg;
        }
        int maxOccurrence = ConfigUtils.getInt("general.ChatCompactor.MaxOccurrence");
        String tail = "";
        if (occurrencesCount > maxOccurrence) {
            tail = " (" + maxOccurrence + "+)";
        } else {
            tail = " (" + occurrencesCount + ")";
        }
        return msg.copy().append(TextUtils.literal(tail).copy().withStyle(ChatFormatting.GRAY));
    }

    public static int calculateOccurrenceCount(Component message) {
        int previousOccurrenceCount = 0;
        if (ConfigUtils.getBoolean("general.ChatCompactor.Enabled") && message != null) {
            TextUtils.MessageUnit latestMessageUnit = TextUtils.getLatestMessage();
            if (latestMessageUnit == null) {
                return 1;
            }
            if (ConfigUtils.getBoolean("general.ChatCompactor.UseStrict")) {
                if (message.equals(latestMessageUnit.message)) {
                    previousOccurrenceCount = latestMessageUnit.occurrenceCount;
                }
            } else {
                if (TextUtils.wash(message.getString())
                             .equals(TextUtils.wash(latestMessageUnit.message.getString()))) {
                    previousOccurrenceCount = latestMessageUnit.occurrenceCount;
                }
            }
        }
        return previousOccurrenceCount + 1;
    }
}
