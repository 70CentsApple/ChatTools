package net.apple70cents.chattools.utils;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.math.MathHelper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

public class CopyFeatureScreen extends Screen {
    private MultilineText messageSplit;
    private Screen oldScreen;
    private TextUtils.MessageUnit unit;

    public CopyFeatureScreen(TextUtils.MessageUnit unit) {
        super(TextUtils.trans("texts.copy.title"));
        this.oldScreen = MinecraftClient.getInstance().currentScreen;
        this.messageSplit = MultilineText.EMPTY;
        this.unit = unit;
    }

    @Override
    protected void init() {
        super.init();
        this.messageSplit = MultilineText.create(this.textRenderer, unit.message, this.width - 50);
        this.addButtons();
    }

    protected void addButtons() {
        int midH = this.height / 2;
        Keyboard kb = MinecraftClient.getInstance().keyboard;
        addCenterButton("copyRaw", midH - 21 * 2, (button -> {
            kb.setClipboard(unit.message.getString());
        }));
        addCenterButton("copyWithColorCodeEscaped", midH - 21, (button -> {
            kb.setClipboard(TextUtils.backEscapeColorCodes(unit.message.getString()));
        }));
        addCenterButton("copyWithNoColorCode", midH, (button -> {
            kb.setClipboard(TextUtils.wash(unit.message.getString()));
        }));
        addCenterButton("copyUnixTimestamp", midH + 21, (button -> {
            kb.setClipboard(String.valueOf(unit.unixTimestamp));
        }));
        addCenterButton("copyTimestamp", midH + 21 * 2, (button -> {
            Instant instant = Instant.ofEpochSecond(unit.unixTimestamp);
            LocalDateTime currentTime = LocalDateTime.ofEpochSecond(unit.unixTimestamp, 0, ZoneId.systemDefault()
                                                                                                 .getRules()
                                                                                                 .getOffset(instant));
            String offsetString = ZoneId.systemDefault().getRules().getOffset(instant).getId();
            // yyyy/MM/dd HH:mm:ss UTC±XX:XX
            String longTimeDisplay = String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime
                    .getMonth()
                    .getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString);
            kb.setClipboard(longTimeDisplay);
        }));
        addCenterButton("cancel", this.height - 30, (button) -> {
            MinecraftClient.getInstance().setScreen(oldScreen);
        });
    }

    protected void addCenterButton(String translationKey, int y, ButtonWidget.PressAction func) {
        int buttonW = 200;
        int buttonH = 20;
        //#if MC>=11900
        this.addDrawableChild(ButtonWidget.builder(TextUtils.trans("texts.copy." + translationKey), func)
                                     .position(this.width / 2 - buttonW / 2, y - buttonH / 2).size(buttonW, buttonH)
                                     .build());
        //#elseif MC>=11700
        //$$ addDrawableChild(new ButtonWidget(this.width / 2 - buttonW / 2, y - buttonH / 2, buttonW, buttonH, TextUtils.trans("texts.copy." + translationKey), func));
        //#else
        //$$ addButton(new ButtonWidget(this.width / 2 - buttonW / 2, y - buttonH / 2, buttonW, buttonH, TextUtils.trans("texts.copy." + translationKey), func));
        //#endif
    }

    @Override
    public void render(
            //#if MC>=12000
            DrawContext context
            //#else
            //$$ MatrixStack context
            //#endif
            , int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // this draws the title
        // context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.getTitleY(), 16777215);
        // this draws the message
        this.messageSplit.drawCenterWithShadow(context, this.width / 2, this.getMessageY());
    }

    private int getTitleY() {
        int i = (this.height - this.getMessagesHeight()) / 2;
        return MathHelper.clamp(i - 29, 10, 30);
    }

    private int getMessageY() {
        return this.getTitleY() + 20;
    }

    private int getMessagesHeight() {
        return this.messageSplit.count() * 9;
    }

}
