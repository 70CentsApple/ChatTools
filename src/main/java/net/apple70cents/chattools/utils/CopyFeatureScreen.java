package net.apple70cents.chattools.utils;

import com.google.gson.JsonElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

public class CopyFeatureScreen extends Screen {
    private MultiLineLabel messageSplit;
    private Screen oldScreen;
    private TextUtils.MessageUnit unit;
    private Map<String, Button> buttons;
    private final Map<String, ButtonData> buttonDatas;
    private MultiLineLabel previewTextSplit;
    private MultiLineLabel additionalInfoSplit;

    protected static class ButtonData {
        public int row;
        public int column;
        public String content;

        public ButtonData(int row, int column, String content) {
            this.row = row;
            this.column = column;
            this.content = content;
        }
    }

    public CopyFeatureScreen(TextUtils.MessageUnit unit) {
        super(TextUtils.trans("texts.copy.title"));
        this.oldScreen = Minecraft.getInstance().screen;
        this.messageSplit = MultiLineLabel.EMPTY;
        this.unit = unit;
        this.buttons = new HashMap<>();
        this.buttonDatas = new HashMap<>();
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultiLineLabel.create(this.font, unit.message, this.width - 50);
        List<Component> additionalInfo = new ArrayList<>();
        if (unit.notViaChatPipeline) {
            additionalInfo.add(TextUtils.trans("texts.copy.additionalInfo.notViaChatPipeline"));
        }

        this.additionalInfoSplit = MultiLineLabel.create(this.font, TextUtils.textArray2text(additionalInfo),
                this.width - 50);

        JsonElement jsonElement = TextUtils.component2JsonElement(unit.message.copy());
        String textComponent = jsonElement != null ? jsonElement.toString() : "ERROR";
        this.buttonDatas.put("copyTextComponent", new ButtonData(-2, -1, textComponent));
        this.buttonDatas.put("copyObjectData", new ButtonData(-2, 1, unit.message.toString()));
        this.buttonDatas.put("copyRaw", new ButtonData(-1, -1, unit.message.getString()));
        this.buttonDatas.put("copyWithColorCodeEscaped",
                new ButtonData(-1, 1, TextUtils.decodeColorCodes(unit.message.getString())));
        this.buttonDatas.put("copyWithNoColorCode", new ButtonData(0, -1, TextUtils.wash(unit.message.getString())));
        this.buttonDatas.put("copyUnixTimestamp", new ButtonData(0, 1, String.valueOf(unit.unixTimestamp)));
        this.buttonDatas.put("copyTimestamp", new ButtonData(1, 0, this.getLongTimeDisplay(unit.unixTimestamp)));

        this.addButtons();
    }

    protected void addButtons() {
        int midH = this.height / 2;
        for (Map.Entry<String, ButtonData> buttonData : buttonDatas.entrySet()) {
            int y = midH + 21 * buttonData.getValue().row;
            int xOffset = buttonData.getValue().column * 100;
            buttons.put(buttonData.getKey(), addCenterButton(buttonData.getKey(), y, xOffset, 20, 200, (button -> {
                Minecraft.getInstance().keyboardHandler.setClipboard(buttonData.getValue().content);
            })));
        }
        addCenterButton("jumpTo", this.height - 50, 0, 20, 200, (button) -> {
            Minecraft mc = Minecraft.getInstance();
//? if >=1.21.9 {
            ChatScreen chatScreen = new ChatScreen("", false);
//?} else {
            /*ChatScreen chatScreen = new ChatScreen("");
*///?}
            mc.setScreen(chatScreen);

            List<TextUtils.MessageUnit> messages = new ArrayList<>(TextUtils.messageMap.values());
            List<TextUtils.MessageUnit> messagesAfter = messages.stream().skip(messages.indexOf(unit) + 1L).toList();
            int lines = 0;
//? if >=1.19.4 {
            int maxLineLength = Mth.floor(
                    (double) ChatComponent.getWidth(mc.options.chatWidth().get()) / mc.options.chatScale().get());
//?} else {
            /*int maxLineLength = Mth.floor((double) ChatComponent.getWidth(mc.options.chatWidth) / mc.options.chatScale);
*///?}
            for (TextUtils.MessageUnit msg : messagesAfter) {
                if (msg.occurrenceCount > 1) {
                    continue; // skip compacted messages
                }
                lines += ComponentRenderUtils.wrapComponents(msg.visualMessage, maxLineLength, mc.font).size();
            }
            mc.gui.getChat().scrollChat(lines);
        });
        addCenterButton("cancel", this.height - 30, 0, 20, 200, (button) -> {
            if (oldScreen instanceof ChatHistoryNavigatorScreen) {
                ChatHistoryNavigatorScreen oldNavScreen = (ChatHistoryNavigatorScreen) oldScreen;
                if (oldNavScreen.keywordField != null) {
                    oldNavScreen.chatUnitListWidget.setKeyword(oldNavScreen.keywordField.getValue());
                } else {
                    oldNavScreen.chatUnitListWidget.setKeyword("");
                }
            }
            Minecraft.getInstance().setScreen(oldScreen);
        });
    }

    protected Button addCenterButton(String translationKey, int y, int xOffset, int buttonH, int buttonW, Button.OnPress func) {
//? if >=1.19 {
        Button buttonWidget = Button.builder(TextUtils.trans("texts.copy." + translationKey), func)
                .pos(this.width / 2 - buttonW / 2 + xOffset, y - buttonH / 2).size(buttonW, buttonH).build();
//?} else {
        /*Button buttonWidget = new Button(this.width/2 - buttonW/2 + xOffset,y - buttonH/2, buttonW, buttonH, TextUtils.trans("texts.copy." + translationKey), func);
*///?}

//? if >=1.17 {
        this.addRenderableWidget(buttonWidget);
//?} else {
        /*addButton(buttonWidget);
*///?}

        return buttonWidget;
    }

    @Override
//? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor context
//?} elif >=1.20 {
    /*public void render(GuiGraphics context
*///?} else {
    /*public void render(PoseStack context
*///?}
            , int mouseX, int mouseY, float delta) {
//? if >=26.1 {
        super.extractRenderState(context, mouseX, mouseY, delta);
//?} else {
        /*super.render(context, mouseX, mouseY, delta);
*///?}
        // this draws the title
        // context.drawCenteredString(this.font, this.title, this.width / 2, this.getTitleY(), 0xffffff);

        // this draws the message
//? if >=1.21.11 {
        this.messageSplit.visitLines(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2, this.getMessageY(),
                9, context.textRenderer());
//?} elif >=1.21.9 {
        /*this.messageSplit.render(context, MultiLineLabel.Align.CENTER, this.width / 2, this.getMessageY(), 9, true, 0xffffffff);
*///?} else {
        /*this.messageSplit.renderCentered(context, this.width / 2, this.getMessageY());
*///?}

        // this draws the content preview
        Component previewText = TextUtils.SPACER;
        for (Map.Entry<String, Button> button : buttons.entrySet()) {
            if (button.getValue().isMouseOver(mouseX, mouseY)) {
                previewText = TextUtils.of(buttonDatas.get(button.getKey()).content);
            }
        }
        this.previewTextSplit = MultiLineLabel.create(this.font, previewText, this.width - 50);
//? if >=1.21.11 {
        this.previewTextSplit.visitLines(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2,
                this.height / 2 + 50, 9, context.textRenderer());
//?} elif >=1.21.9 {
        /*this.previewTextSplit.render(context, MultiLineLabel.Align.CENTER, this.width / 2, this.height / 2 + 50, 9, true, 0xffffffff);
*///?} else {
        /*this.previewTextSplit.renderCentered(context, this.width / 2, this.height / 2 + 50);
*///?}

//? if >=1.21.11 {
        this.additionalInfoSplit.visitLines(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2,
                this.height - 75, 9, context.textRenderer());
//?} elif >=1.21.9 {
        /*this.additionalInfoSplit.render(context, MultiLineLabel.Align.CENTER, this.width / 2, this.height - 75, 9, true, 0xffffffff);
*///?} else {
        /*this.additionalInfoSplit.renderCentered(context, this.width / 2, this.height - 75);
*///?}
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return Mth.clamp(i - 29, 10, 30);
    }

    private int getMessageY() {
        return this.getTitleY() + 20;
    }

    private int getMessagesHeight() {
        return this.messageSplit.getLineCount() * 9;
    }

    private String getLongTimeDisplay(long timestamp) {
        Instant instant = Instant.ofEpochSecond(timestamp);
        LocalDateTime currentTime = LocalDateTime.ofEpochSecond(timestamp, 0,
                ZoneId.systemDefault().getRules().getOffset(instant));
        String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
        // yyyy/MM/dd HH:mm:ss UTC±XX:XX
        String longTimeDisplay = String.format("%4d/%d/%d %02d:%02d:%02d\nUTC%s", currentTime.getYear(),
                currentTime.getMonth().getValue(), currentTime.getDayOfMonth(), currentTime.getHour(),
                currentTime.getMinute(), currentTime.getSecond(), offsetString);
        return longTimeDisplay;
    }

}
