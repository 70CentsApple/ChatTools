package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.text.*;

import java.time.LocalDateTime;
import java.time.ZoneId;

public class Timestamp {
    public static Text work(Text message) {
        LocalDateTime currentTime = LocalDateTime.now();
        // get zone offset
        String offsetString = ZoneId.systemDefault().getRules().getOffset(currentTime).getId();
        Text shortTimeDisplay = TextUtils.of(timeInFormat((String) ChatTools.CONFIG.get("general.Timestamp.Pattern")));
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        Text longTimeDisplay = TextUtils.of(String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime
                .getMonth()
                .getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString));
        if ((boolean) ChatTools.CONFIG.get("general.Timestamp.CopyToChatBar.Enabled")) {
            return ((MutableText) shortTimeDisplay).setStyle(Style.EMPTY
               .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, ((MutableText) longTimeDisplay).append("\n\n" + TextUtils
               .trans("texts.copy").getString())))
               .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, TextUtils.backEscapeColorCodes(message.getString()))))
               .append(message);
        } else {
            return ((MutableText) shortTimeDisplay)
                    .setStyle(Style.EMPTY.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, longTimeDisplay)))
                    .append(message);
        }
    }

    private static String timeInFormat(String formatter) {
        formatter = TextUtils.escapeColorCodes(formatter);
        LocalDateTime time = LocalDateTime.now();
        formatter = formatter.replace("{hour}", String.format("%d", time.getHour()))
                             .replace("{minute}", String.format("%02d", time.getMinute()))
                             .replace("{second}", String.format("%02d", time.getSecond()));
        return formatter;
    }
}
