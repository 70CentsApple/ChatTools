package net.apple70cents.chattools.config.provider.yacl;

import com.mojang.blaze3d.platform.InputConstants;
import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//? }

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

import java.util.function.Consumer;
import java.util.function.Supplier;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * A simple screen that captures a key press and saves it to the config.
 * Shown when editing a keycode option in the YACL config screen or rule edit screen.
 *
 * @author 70CentsApple
 */
public class KeyCaptureScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> onCapture;
    private final Supplier<String> currentGetter;

    public KeyCaptureScreen(Screen parent, Consumer<String> onCapture, Supplier<String> currentGetter) {
        super(trans("gui.pressAnyKey"));
        this.parent = parent;
        this.onCapture = onCapture;
        this.currentGetter = currentGetter;
    }

    public KeyCaptureScreen(Screen parent, String configKey) {
        this(parent,
                val -> {
                    ConfigUtils.set(configKey, val);
                    ConfigUtils.save();
                },
                () -> (String) ConfigUtils.get(configKey)
        );
    }

    @Override
//? if >=1.21.9 {
    public boolean keyPressed(KeyEvent keyEvent) {
        int key = keyEvent.key();
        InputConstants.Key key2 = InputConstants.getKey(keyEvent);
//?} else {
    /*public boolean keyPressed(int key, int scanCode, int modifiers) {
        InputConstants.Key key2 = InputConstants.getKey(key, scanCode);
 *///?}
        if ("key.keyboard.escape".equals(key2.getName())) {
            // Escape cancels
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        onCapture.accept(key2.getName());
        Minecraft.getInstance().setScreen(parent);
        return true;
    }

    @Override
//? if >=1.21.9 {
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
//?} else {
    /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
 *///?}
        InputConstants.Key key = InputConstants.Type.MOUSE.getOrCreate(button);
        onCapture.accept(key.getName());
        Minecraft.getInstance().setScreen(parent);
        return true;
    }

    @Override
    public void
//? if >=26.1 {
        extractRenderState
//?} else {
        /*render
*///?}
            (
//? if >=26.1 {
            GuiGraphicsExtractor context
//?} elif >=1.20 {
            /*GuiGraphics context
*///?} else {
            /*PoseStack context
*///?}
            , int mouseX, int mouseY, float delta) {
//? if >=26.1 {
        super.extractRenderState(context, mouseX, mouseY, delta);
//?} else {
        /*super.render(context, mouseX, mouseY, delta);
*///?}

        Component titleText = trans("gui.pressAnyKey");
        Component currentText = TextUtils.literal("§7" + trans("gui.currentKey").getString() + ": §e"
                + InputConstants.getKey(currentGetter.get()).getDisplayName().getString());
        Component escText = trans("gui.pressEscToCancel");

//? if >=26.1 {
        context.centeredText(this.font, titleText, this.width / 2, this.height / 2 - 20, 0xffffffff);
        context.centeredText(this.font, currentText, this.width / 2, this.height / 2, 0xffffffff);
        context.centeredText(this.font, escText, this.width / 2, this.height / 2 + 20, 0xff888888);
//?} elif >=1.21.6 {
        /*context.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 20, 0xffffffff);
        context.drawCenteredString(this.font, currentText, this.width / 2, this.height / 2, 0xffffffff);
        context.drawCenteredString(this.font, escText, this.width / 2, this.height / 2 + 20, 0xff888888);
*///?} elif >=1.20 {
        /*context.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 20, 0xffffff);
        context.drawCenteredString(this.font, currentText, this.width / 2, this.height / 2, 0xffffff);
        context.drawCenteredString(this.font, escText, this.width / 2, this.height / 2 + 20, 0x888888);
*///?} else {
        /*drawCenteredString(context, this.font, titleText, this.width / 2, this.height / 2 - 20, 0xffffff);
        drawCenteredString(context, this.font, currentText, this.width / 2, this.height / 2, 0xffffff);
        drawCenteredString(context, this.font, escText, this.width / 2, this.height / 2 + 20, 0x888888);
*///?}
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
