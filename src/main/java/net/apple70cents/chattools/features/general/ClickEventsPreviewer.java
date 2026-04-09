package net.apple70cents.chattools.features.general;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

//? if >=1.21.5 {
import net.minecraft.world.item.ItemStack;
//?}

//? if <1.20 {
/*import net.minecraft.world.item.TooltipFlag;
*///?}

/**
 * @author 70CentsApple
 */
public class ClickEventsPreviewer {
    public static Style work(Style style) {
        if (style == null) {
            return null;
        }
        HoverEvent hoverEvent = style.getHoverEvent();
        Component textToAppend = getComponentToAppend(style);

        // Return if already injected or with no text to append
        if (isModified(style) || textToAppend == null) {
            return style;
        }
        // Add two empty lines before it (Also works as a Style Spacer)
        Component textToAppendWithTwoEmptyLinesInFront = TextUtils.literal("\n\n").copy().append(textToAppend);
        if (hoverEvent == null) {
            style = style.withHoverEvent(TextUtils.showTextHoverEvent(textToAppend));
        } else {
            Component oldHoverComponent =
//? if >=1.21.5 {
                    "show_text".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowText) hoverEvent).value() : null;
//?} else {
                    /*hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
*///?}
            // Has Actions.SHOW_TEXT
            if (oldHoverComponent != null && !oldHoverComponent.getString().isBlank()) {
                Component newHoverComponent = (TextUtils.SPACER.copy().append(oldHoverComponent)).append(textToAppendWithTwoEmptyLinesInFront);
                style = style.withHoverEvent(TextUtils.showTextHoverEvent(newHoverComponent));
            } else {
//? if >=26.1 {
                HoverEvent.EntityTooltipInfo entityContent = "show_entity".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowEntity) hoverEvent).entity() : null;
                ItemStack itemContent = "show_item".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowItem) hoverEvent).item().create() : null;
//?} elif >=1.21.5 {
                /*HoverEvent.EntityTooltipInfo entityContent = "show_entity".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowEntity) hoverEvent).entity() : null;
                ItemStack itemContent = "show_item".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowItem) hoverEvent).item() : null;
*///?} else {
                /*HoverEvent.EntityTooltipInfo entityContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ENTITY);
                HoverEvent.ItemStackInfo itemContent = hoverEvent.getValue(HoverEvent.Action.SHOW_ITEM);
*///?}
                if (entityContent != null) {
                    // Has Actions.SHOW_ENTITY
                    oldHoverComponent = TextUtils.textArray2text(entityContent.getTooltipLines());
                } else if (itemContent != null) {
                    // Has Actions.SHOW_ITEM
                    oldHoverComponent = TextUtils.textArray2text(
//? if >=1.21.5 {
                            Screen.getTooltipFromItem(Minecraft.getInstance(), itemContent)
//?} elif >=1.20 {
                            /*Screen.getTooltipFromItem(Minecraft.getInstance(), itemContent.getItemStack())
*///?} elif >=1.19 {
                            /*itemContent.getItemStack().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.ADVANCED)
*///?} else {
                            /*itemContent.getItemStack().getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.ADVANCED)
*///?}
                    );
                }
                if (oldHoverComponent != null) {
                    Component newHoverComponent = (TextUtils.SPACER.copy().append(oldHoverComponent)).append(textToAppendWithTwoEmptyLinesInFront);
                    style = style.withHoverEvent(TextUtils.showTextHoverEvent(newHoverComponent));
                } else {
                    style = style.withHoverEvent(TextUtils.showTextHoverEvent(textToAppendWithTwoEmptyLinesInFront));
                }
            }
        }
        return style;
    }

    @Unique
    private static Component getComponentToAppend(Style style) {
        boolean hasInsertion = style.getInsertion() != null && !style.getInsertion().isBlank();
        boolean hasClickEvent = style.getClickEvent() != null;
        if (!hasInsertion && !hasClickEvent) {
            return null;
        }
        List<Component> texts = new ArrayList<>();
        texts.add(TextUtils.trans("texts.PreviewClickEvents.overall"));
        if (hasInsertion) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.insertion", style.getInsertion()));
        }
        if (hasClickEvent) {
            texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent"));
            ClickEvent clickEvent = style.getClickEvent();
//? if >=1.21.5 {
            String value = "";
            switch (clickEvent.action()) {
                case OPEN_URL:
                    value = ((ClickEvent.OpenUrl) clickEvent).uri().toString();
                    break;
                case OPEN_FILE:
                    value = ((ClickEvent.OpenFile) clickEvent).file().getAbsolutePath();
                    break;
                case RUN_COMMAND:
                    value = ((ClickEvent.RunCommand) clickEvent).command();
                    break;
                case SUGGEST_COMMAND:
                    value = ((ClickEvent.SuggestCommand) clickEvent).command();
                    break;
//? if >=1.21.6 {
                case SHOW_DIALOG:
                    value = ((ClickEvent.ShowDialog) clickEvent).dialog().getRegisteredName();
                    break;
//?}
                case CHANGE_PAGE:
                    value = String.valueOf(((ClickEvent.ChangePage) clickEvent).page());
                    break;
                case COPY_TO_CLIPBOARD:
                    value = ((ClickEvent.CopyToClipboard) clickEvent).value();
                    break;
//? if >=1.21.6 {
                case CUSTOM:
                    value = ((ClickEvent.Custom) clickEvent).id() + " → " + ((ClickEvent.Custom) clickEvent).payload();
                    break;
//?}
                default:
                    value = "[ERROR]";
            }
            Component valueComponent = TextUtils.of(value).copy().withStyle(ChatFormatting.GREEN);
            String action = clickEvent.action().getSerializedName();
//?} elif >=1.20.2 {
            /*Component valueComponent = TextUtils.of(clickEvent.getValue()).copy().withStyle(ChatFormatting.GREEN);
            String action = clickEvent.getAction().getSerializedName();
*///?} else {
            /*Component valueComponent = TextUtils.of(clickEvent.getValue()).copy().withStyle(ChatFormatting.GREEN);
            String action = clickEvent.getAction().getName();
*///?}
            switch (action) {
                case "open_url":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.openUrl", valueComponent));
                    break;
                case "open_file":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.openFile", valueComponent));
                    break;
                case "run_command":
                    if (valueComponent.getString().contains("/chattools get_message")) {
                        // skip it
                        texts.remove(texts.size() - 1);
                        // the overall text is the only remained text
                        if (texts.size() <= 1) {
                            return null;
                        }
                    } else {
                        texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.runCommand", valueComponent));
                    }
                    break;
                case "suggest_command":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.suggestCommand", valueComponent));
                    break;
//? if >=1.21.6 {
                case "show_dialog":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.showDialog", valueComponent));
                    break;
//?}
                case "change_page":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.changePage", valueComponent));
                    break;
                case "copy_to_clipboard":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.copyToClipboard", valueComponent));
                    break;
//? if >=1.21.6 {
                case "custom":
                    texts.add(TextUtils.trans("texts.PreviewClickEvents.clickEvent.custom", valueComponent));
                    break;
//?}
                default:
                    LoggerUtils.warn("[ChatTools] Unknown clickEvent action type: " + action);
            }
        }
        return TextUtils.textArray2text(texts);
    }

    @Unique
    private static Boolean isModified(Style style) {
        HoverEvent hoverEvent = style.getHoverEvent();
        if (hoverEvent == null) {
            return false;
        }
//? if >=1.21.5 {
        Component tooltip = "show_text".equals(hoverEvent.action().getSerializedName()) ? ((HoverEvent.ShowText) hoverEvent).value() : null;
//?} else {
        /*Component tooltip = hoverEvent.getValue(HoverEvent.Action.SHOW_TEXT);
*///?}
        if (tooltip == null || tooltip.getString().isBlank()) {
            return false;
        }
        return tooltip.getString().contains(TextUtils.trans("texts.PreviewClickEvents.overall").getString());
    }
}
