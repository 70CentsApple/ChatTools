package net.apple70cents.chattools.config.provider.yacl;

import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
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

import java.util.*;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * Screen for editing the command list inside a Macro rule.
 * Each command has: command text, delay, forceDisableFormatter.
 *
 * @author 70CentsApple
 */
public class MacroCommandListScreen extends Screen {
    private final Screen parent;
    private final Map<String, Object> macroItem;
    private List<Map<String, Object>> commands;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 52;
    private static final int LIST_TOP = 40;

    @SuppressWarnings("unchecked")
    public MacroCommandListScreen(Screen parent, Map<String, Object> macroItem) {
        super(trans("gui.editCommands"));
        this.parent = parent;
        this.macroItem = macroItem;
        this.commands = new ArrayList<>();
        List<?> rawList = (List<?>) macroItem.getOrDefault("commands", Collections.emptyList());
        for (Object obj : rawList) {
            if (obj instanceof Map) {
                commands.add(new LinkedHashMap<>((Map<String, Object>) obj));
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        rebuildWidgets();
    }

    @SuppressWarnings("unchecked")
    protected void rebuildWidgets() {
//? if >=1.17 {
        this.clearWidgets();
//?}

        int listBottom = this.height - 60;
        int visibleCount = (listBottom - LIST_TOP) / ITEM_HEIGHT;
        int maxScroll = Math.max(0, commands.size() - visibleCount);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        scrollOffset = Math.max(scrollOffset, 0);

        for (int i = 0; i < visibleCount && (i + scrollOffset) < commands.size(); i++) {
            int index = i + scrollOffset;
            int y = LIST_TOP + i * ITEM_HEIGHT;
            Map<String, Object> cmd = commands.get(index);

            // Command EditBox
            EditBox cmdBox = new EditBox(this.font, 30, y, this.width - 110, 20, TextUtils.literal("command"));
            cmdBox.setMaxLength(Integer.MAX_VALUE);
            cmdBox.setValue(cmd.getOrDefault("command", "").toString());
            cmdBox.setResponder(val -> cmd.put("command", val));
//? if >=1.17 {
            this.addRenderableWidget(cmdBox);
//?} else {
            /*this.addButton(cmdBox);
*///?}

            // Delay EditBox
            EditBox delayBox = new EditBox(this.font, 30, y + 22, 80, 20, TextUtils.literal("delay"));
            delayBox.setMaxLength(10);
            delayBox.setValue(cmd.getOrDefault("delayInMilliseconds", "0").toString());
            delayBox.setResponder(val -> {
                try { cmd.put("delayInMilliseconds", Double.parseDouble(val)); }
                catch (NumberFormatException ignored) {}
            });
//? if >=1.17 {
            this.addRenderableWidget(delayBox);
//?} else {
            /*this.addButton(delayBox);
*///?}

            // ForceDisableFormatter toggle
            boolean fdf = cmd.getOrDefault("forceDisableFormatter", false) instanceof Boolean
                    ? (Boolean) cmd.get("forceDisableFormatter") : false;
            String fdfString = TextUtils.trans("chatkeybindings.Macro.List.Commands.ForceDisableFormatter").getString();
            Component fdfDisplay = TextUtils.literal(fdfString + ": " + (fdf ? "§a✔" : "§c✘"));
//? if >=1.19 {
            this.addRenderableWidget(Button.builder(
                    fdfDisplay,
                    button -> {
                        boolean cur = cmd.getOrDefault("forceDisableFormatter", false) instanceof Boolean
                                ? (Boolean) cmd.get("forceDisableFormatter") : false;
                        cmd.put("forceDisableFormatter", !cur);
                        rebuildWidgets();
                    }
            ).bounds(115, y + 22, 170, 20).build());

            // Delete button
            this.addRenderableWidget(Button.builder(
                    TextUtils.literal("§c✕"),
                    button -> {
                        commands.remove(index);
                        rebuildWidgets();
                    }
            ).bounds(this.width - 75, y, 20, 20).build());
//?} elif >=1.17 {
            /*final int fi = index;
            this.addRenderableWidget(new Button(115, y + 22, 170, 20,
                    fdfDisplay,
                    button -> {
                        boolean cur = cmd.getOrDefault("forceDisableFormatter", false) instanceof Boolean
                                ? (Boolean) cmd.get("forceDisableFormatter") : false;
                        cmd.put("forceDisableFormatter", !cur);
                        rebuildWidgets();
                    }));
            this.addRenderableWidget(new Button(this.width - 75, y, 20, 20,
                    TextUtils.literal("§c✕"),
                    button -> { commands.remove(fi); rebuildWidgets(); }));
*///?} else {
            /*final int fi = index;
            this.addButton(new Button(115, y + 22, 170, 20,
                    fdfDisplay,
                    button -> {
                        boolean cur = cmd.getOrDefault("forceDisableFormatter", false) instanceof Boolean
                                ? (Boolean) cmd.get("forceDisableFormatter") : false;
                        cmd.put("forceDisableFormatter", !cur);
                        rebuildWidgets();
                    }));
            this.addButton(new Button(this.width - 75, y, 20, 20,
                    TextUtils.literal("§c✕"),
                    button -> { commands.remove(fi); rebuildWidgets(); }));
*///?}
        }

        // Add & Done buttons
//? if >=1.19 {
        this.addRenderableWidget(Button.builder(
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    Map<String, Object> newCmd = new LinkedHashMap<>();
                    newCmd.put("command", "");
                    newCmd.put("delayInMilliseconds", 0.0);
                    newCmd.put("forceDisableFormatter", false);
                    commands.add(newCmd);
                    scrollOffset = Math.max(0, commands.size() - visibleCount);
                    rebuildWidgets();
                }
        ).bounds(this.width / 2 - 120, this.height - 52, 110, 20).build());

        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    saveCommands();
                    Minecraft.getInstance().setScreen(parent);
                }
        ).bounds(this.width / 2 + 10, this.height - 52, 110, 20).build());
//?} elif >=1.17 {
        /*this.addRenderableWidget(new Button(this.width / 2 - 120, this.height - 52, 110, 20,
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    Map<String, Object> newCmd = new LinkedHashMap<>();
                    newCmd.put("command", ""); newCmd.put("delayInMilliseconds", 0.0); newCmd.put("forceDisableFormatter", false);
                    commands.add(newCmd);
                    scrollOffset = Math.max(0, commands.size() - visibleCount);
                    rebuildWidgets();
                }));
        this.addRenderableWidget(new Button(this.width / 2 + 10, this.height - 52, 110, 20,
                CommonComponents.GUI_DONE,
                button -> { saveCommands(); Minecraft.getInstance().setScreen(parent); }));
*///?} else {
        /*this.addButton(new Button(this.width / 2 - 120, this.height - 52, 110, 20,
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    Map<String, Object> newCmd = new LinkedHashMap<>();
                    newCmd.put("command", ""); newCmd.put("delayInMilliseconds", 0.0); newCmd.put("forceDisableFormatter", false);
                    commands.add(newCmd);
                    scrollOffset = Math.max(0, commands.size() - visibleCount);
                    rebuildWidgets();
                }));
        this.addButton(new Button(this.width / 2 + 10, this.height - 52, 110, 20,
                CommonComponents.GUI_DONE,
                button -> { saveCommands(); Minecraft.getInstance().setScreen(parent); }));
*///?}
    }

    private void saveCommands() {
        macroItem.put("commands", new ArrayList<>(commands));
    }

    @Override
    public boolean mouseScrolled(
//? if >=1.20.4 {
            double mouseX, double mouseY, double horizontalAmount, double verticalAmount
//?} else {
            /*double mouseX, double mouseY, double verticalAmount
*///?}
    ) {
        scrollOffset -= (int) verticalAmount;
        int listBottom = this.height - 60;
        int visibleCount = (listBottom - LIST_TOP) / ITEM_HEIGHT;
        int maxScroll = Math.max(0, commands.size() - visibleCount);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        rebuildWidgets();
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
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xffffffff);
//?} elif >=1.21.6 {
        /*super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffffff);
*///?} elif >=1.20 {
        /*super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffff);
*///?} else {
        /*super.render(context, mouseX, mouseY, delta);
        drawCenteredString(context, this.font, this.title, this.width / 2, 15, 0xffffff);
*///?}
    }

    @Override
    public void onClose() {
        saveCommands();
        Minecraft.getInstance().setScreen(parent);
    }
}
