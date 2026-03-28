package net.apple70cents.chattools.config;

import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

//? if >=1.21.11 {
import net.minecraft.util.Util;
//?} else {
/*import net.minecraft.Util;
*///?}

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * Screen displayed when neither YACL nor Cloth Config is installed.
 * Prompts the user to download one of them.
 *
 * @author 70CentsApple
 */
public class MissingConfigLibScreen extends Screen {
    private final Screen parent;

    private static final String YACL_URL = "https://modrinth.com/mod/yacl";
    private static final String CLOTH_CONFIG_URL = "https://modrinth.com/mod/cloth-config";

    public MissingConfigLibScreen(Screen parent) {
        super(trans("gui.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

//? if >=1.19 {

//? if HAS_YACL {
        this.addRenderableWidget(Button.builder(
                TextUtils.literal("§a⬇ YetAnotherConfigLib (YACL)"),
                button -> Util.getPlatform().openUri(YACL_URL)
        ).bounds(centerX - 120, centerY - 10, 240, 20).build());
//?}

        this.addRenderableWidget(Button.builder(
                TextUtils.literal("§e⬇ Cloth Config"),
                button -> Util.getPlatform().openUri(CLOTH_CONFIG_URL)
        ).bounds(centerX - 120, centerY + 15, 240, 20).build());

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> this.onClose()
        ).bounds(centerX - 80, centerY + 50, 160, 20).build());
//?} elif >=1.17 {

/*//? if HAS_YACL {
        this.addRenderableWidget(new Button(centerX - 120, centerY - 10, 240, 20,
                TextUtils.literal("§a⬇ YetAnotherConfigLib (YACL)"),
                button -> Util.getPlatform().openUri(YACL_URL)));
//?}

        this.addRenderableWidget(new Button(centerX - 120, centerY + 15, 240, 20,
                TextUtils.literal("§e⬇ Cloth Config"),
                button -> Util.getPlatform().openUri(CLOTH_CONFIG_URL)));

        this.addRenderableWidget(new Button(centerX - 80, centerY + 50, 160, 20,
                CommonComponents.GUI_DONE,
                button -> this.onClose()));
*///?} else {

/*//? if HAS_YACL {
        this.addButton(new Button(centerX - 120, centerY - 10, 240, 20,
                TextUtils.literal("§a⬇ YetAnotherConfigLib (YACL)"),
                button -> Util.getPlatform().openUri(YACL_URL)));
//?}

        this.addButton(new Button(centerX - 120, centerY + 15, 240, 20,
                TextUtils.literal("§e⬇ Cloth Config"),
                button -> Util.getPlatform().openUri(CLOTH_CONFIG_URL)));

        this.addButton(new Button(centerX - 80, centerY + 50, 160, 20,
                CommonComponents.GUI_DONE,
                button -> Minecraft.getInstance().setScreen(parent)));
*///?}
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

        Component titleText = trans("gui.title");
        Component hintText = trans("gui.missingConfigLib");

//? if >=26.1 {
        context.centeredText(this.font, titleText, this.width / 2, this.height / 2 - 50, 0xffffffff);
        context.centeredText(this.font, hintText, this.width / 2, this.height / 2 - 35, 0xffaaaaaa);
//?} elif >=1.21.6 {
        /*context.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 50, 0xffffffff);
        context.drawCenteredString(this.font, hintText, this.width / 2, this.height / 2 - 35, 0xffaaaaaa);
*///?} elif >=1.20 {
        /*context.drawCenteredString(this.font, titleText, this.width / 2, this.height / 2 - 50, 0xffffff);
        context.drawCenteredString(this.font, hintText, this.width / 2, this.height / 2 - 35, 0xaaaaaa);
*///?} else {
        /*drawCenteredString(context, this.font, titleText, this.width / 2, this.height / 2 - 50, 0xffffff);
        drawCenteredString(context, this.font, hintText, this.width / 2, this.height / 2 - 35, 0xaaaaaa);
*///?}
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }
}
