package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.network.chat.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class Timestamp {
    public static Component work(Component message, String hashcode) {
        Instant instant = Instant.now();
        long currentUnixTimestamp = instant.getEpochSecond();
        LocalDateTime currentTime = LocalDateTime.ofEpochSecond(currentUnixTimestamp, 0, ZoneId.systemDefault()
                                                                                               .getRules()
                                                                                               .getOffset(instant));
        // get zone offset
        String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
        Component shortTimeDisplay = TextUtils.of(timeInFormat((String) ConfigUtils.get("general.Timestamp.Pattern")));
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        Component longTimeDisplay = TextUtils.of(String.format("%4d/%d/%d %02d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime
                .getMonth()
                .getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString));
        if ((boolean) ConfigUtils.get("general.Timestamp.CopyToChatBar.Enabled")) {
            HoverEvent hoverEvent =
//? if >=1.21.5 {
                    new HoverEvent.ShowText(
//?} else {
                    /*new HoverEvent(HoverEvent.Action.SHOW_TEXT,
*///?}
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
            HoverEvent hoverEvent =
//? if >=1.21.5 {
                    new HoverEvent.ShowText(
//?} else {
                    /*new HoverEvent(HoverEvent.Action.SHOW_TEXT,
*///?}
                    longTimeDisplay);
            MutableComponent timestampText = shortTimeDisplay.copy().setStyle(Style.EMPTY.withHoverEvent(hoverEvent));
            return (TextUtils.SPACER.copy().append(timestampText)).append(message);
        }
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
