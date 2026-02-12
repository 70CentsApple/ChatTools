package net.apple70cents.chattools.features.filter;

import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.MessageUtils;
import net.apple70cents.chattools.utils.RegExUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.regex.Pattern;

public class ChatFilter {

    public static boolean shouldFilter(Component text) {
        if (!(boolean) ConfigUtils.get("filter.Enabled")) {
            return false;
        }
        // only respond once
        if (TextUtils.wash(text.getString())
                     .contains(TextUtils.wash(TextUtils.trans("texts.filterPlaceholder").getString()))) {
            return false;
        }
        List<String> filterList = (List<String>) ConfigUtils.get("filter.List");
        String washed = TextUtils.wash(text.getString());
        for (String pattern : filterList) {
            if (RegExUtils.getOrCompilePattern(pattern, Pattern.MULTILINE).matcher(washed).find()) {
                return true;
            }
        }
        return false;
    }

    public static void sendPlaceholderIfActive() {
        if (!(boolean) ConfigUtils.get("filter.FilteredPlaceholderEnabled")) {
            return;
        }
        Style style = Style.EMPTY.withHoverEvent(
                //#if MC>=12105
                new HoverEvent.ShowText(
                //#else
                //$$ new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                //#endif
                TextUtils.trans("texts.filterPlaceholder.@Tooltip")));
        Component placeholder = TextUtils.trans("texts.filterPlaceholder").copy().setStyle(style);
        MessageUtils.sendToNonPublicChat(placeholder);
    }
}
