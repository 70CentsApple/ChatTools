package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.network.chat.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Timestamp {
    public static Component work(Component message, String hashcode) {
        Instant instant = Instant.now();
        Component shortTimeDisplay = TextUtils.of(timeInFormat(ConfigUtils.getString("general.Timestamp.Pattern")));
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        Component longTimeDisplay = TextUtils.of(formatLongTime(instant.getEpochSecond()));
        if (ConfigUtils.getBoolean("general.CopyMenu.TimestampEnabled")) {
            HoverEvent hoverEvent = TextUtils.showTextHoverEvent(
                    longTimeDisplay.copy().append("\n\n").append(TextUtils.trans("texts.copy.launch")));
            ClickEvent clickEvent =
//? if >=1.21.5 {
                    new ClickEvent.RunCommand(
//?} else {
                    /*new ClickEvent(ClickEvent.Action.RUN_COMMAND,
*///?}
                    "/chattools get_message " + hashcode);
            MutableComponent timestampText = shortTimeDisplay.copy().setStyle(Style.EMPTY.withHoverEvent(hoverEvent)
                                                                                              .withClickEvent(clickEvent));
            return (TextUtils.SPACER.copy().append(timestampText)).append(message);
        } else {
            HoverEvent hoverEvent = TextUtils.showTextHoverEvent(longTimeDisplay);
            MutableComponent timestampText = shortTimeDisplay.copy().setStyle(Style.EMPTY.withHoverEvent(hoverEvent));
            return (TextUtils.SPACER.copy().append(timestampText)).append(message);
        }
    }

    public static String formatLongTime(long unixTimestamp, String delimiter) {
        Instant instant = Instant.ofEpochSecond(unixTimestamp);
        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime time = LocalDateTime.ofInstant(instant, zone);
        String offsetString = zone.getRules().getOffset(instant).getId();
        return String.format("%4d/%d/%d %02d:%02d:%02d" + delimiter + "UTC%s",
                time.getYear(), time.getMonthValue(), time.getDayOfMonth(),
                time.getHour(), time.getMinute(), time.getSecond(), offsetString);
    }

    public static String formatLongTime(long unixTimestamp) {
        return formatLongTime(unixTimestamp, "\n");
    }

    public static String timeInFormat(String formatter, LocalDateTime time) {
        formatter = TextUtils.encodeColorCodes(formatter);
        formatter = formatter.replace("{hour}", String.format("%02d", time.getHour()))
                             .replace("{minute}", String.format("%02d", time.getMinute()))
                             .replace("{second}", String.format("%02d", time.getSecond()));
        return formatter;
    }

    public static String timeInFormat(String formatter) {
        return timeInFormat(formatter, LocalDateTime.now());
    }
}
