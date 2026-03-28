package net.apple70cents.chattools.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.RegExUtils;
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

import java.util.*;

import static net.apple70cents.chattools.utils.TextUtils.trans;

/**
 * Generic screen for editing complex rule lists (NotifierList, BubbleList, etc.)
 * Used by the YACL config screen when user clicks "Edit" on a complex list option.
 *
 * @author 70CentsApple
 */
public class RuleListScreen extends Screen {
    private final Screen parent;
    private final String configKey;
    private final String type;
    private List<Object> currentList;
    private int scrollOffset = 0;
    private static final int ITEM_HEIGHT = 24;
    private static final int LIST_TOP = 45;

    public RuleListScreen(Screen parent, String configKey, String type) {
        super(trans(configKey, "§f" + ContextUtils.getSessionIdentifier()));
        this.parent = parent;
        this.configKey = configKey;
        this.type = type;
        // Deep copy the list
        this.currentList = new ArrayList<>((List<Object>) ConfigUtils.get(configKey));
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

        // Clamp scroll
        int maxScroll = Math.max(0, currentList.size() - visibleCount);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        scrollOffset = Math.max(scrollOffset, 0);

        // Render each visible item
        for (int i = 0; i < visibleCount && (i + scrollOffset) < currentList.size(); i++) {
            int index = i + scrollOffset;
            int y = LIST_TOP + i * ITEM_HEIGHT;
            Object item = currentList.get(index);
            String summary = getSummary(item, type, configKey);

            // Edit button
//? if >=1.19 {
            this.addRenderableWidget(Button.builder(
                    TextUtils.literal("§f" + (index + 1) + ". " + summary),
                    button -> Minecraft.getInstance().setScreen(
                            new RuleEditScreen(this, configKey, type, currentList, index))
            ).bounds(30, y, this.width - 110, 20).build());

            // Delete button
            this.addRenderableWidget(Button.builder(
                    TextUtils.literal("§c✕"),
                    button -> {
                        currentList.remove(index);
                        saveList();
                        rebuildWidgets();
                    }
            ).bounds(this.width - 75, y, 20, 20).build());

            // Move up button
            if (index > 0) {
                this.addRenderableWidget(Button.builder(
                        TextUtils.literal("§7▲"),
                        button -> {
                            Collections.swap(currentList, index, index - 1);
                            saveList();
                            rebuildWidgets();
                        }
                ).bounds(this.width - 52, y, 20, 20).build());
            }

            // Move down button
            if (index < currentList.size() - 1) {
                this.addRenderableWidget(Button.builder(
                        TextUtils.literal("§7▼"),
                        button -> {
                            Collections.swap(currentList, index, index + 1);
                            saveList();
                            rebuildWidgets();
                        }
                ).bounds(this.width - 30, y, 20, 20).build());
            }
//?} elif >=1.17 {
            /*this.addRenderableWidget(new Button(30, y, this.width - 110, 20,
                    TextUtils.literal("§f" + (index + 1) + ". " + summary),
                    button -> Minecraft.getInstance().setScreen(
                            new RuleEditScreen(this, configKey, type, currentList, index))));
            final int fi = index;
            this.addRenderableWidget(new Button(this.width - 75, y, 20, 20,
                    TextUtils.literal("§c✕"),
                    button -> { currentList.remove(fi); saveList(); rebuildWidgets(); }));
*///?} else {
            /*final int fi = index;
            this.addButton(new Button(30, y, this.width - 110, 20,
                    TextUtils.literal("§f" + (index + 1) + ". " + summary),
                    button -> Minecraft.getInstance().setScreen(
                            new RuleEditScreen(this, configKey, type, currentList, fi))));
            this.addButton(new Button(this.width - 75, y, 20, 20,
                    TextUtils.literal("§c✕"),
                    button -> { currentList.remove(fi); saveList(); rebuildWidgets(); }));
*///?}
        }

        // Add new item button
//? if >=1.19 {
        this.addRenderableWidget(Button.builder(
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    currentList.add(createDefaultItem(type));
                    saveList();
                    // Scroll to show new item
                    scrollOffset = Math.max(0, currentList.size() - visibleCount);
                    rebuildWidgets();
                }
        ).bounds(this.width / 2 - 120, this.height - 52, 110, 20).build());

        // Done button
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    saveList();
                    Minecraft.getInstance().setScreen(parent);
                }
        ).bounds(this.width / 2 + 10, this.height - 52, 110, 20).build());
//?} elif >=1.17 {
        /*this.addRenderableWidget(new Button(this.width / 2 - 120, this.height - 52, 110, 20,
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    currentList.add(createDefaultItem(type));
                    saveList();
                    scrollOffset = Math.max(0, currentList.size() - visibleCount);
                    rebuildWidgets();
                }));
        this.addRenderableWidget(new Button(this.width / 2 + 10, this.height - 52, 110, 20,
                CommonComponents.GUI_DONE,
                button -> { saveList(); Minecraft.getInstance().setScreen(parent); }));
*///?} else {
        /*this.addButton(new Button(this.width / 2 - 120, this.height - 52, 110, 20,
                TextUtils.literal("§a+ ").copy().append(trans("gui.addNew")),
                button -> {
                    currentList.add(createDefaultItem(type));
                    saveList();
                    scrollOffset = Math.max(0, currentList.size() - visibleCount);
                    rebuildWidgets();
                }));
        this.addButton(new Button(this.width / 2 + 10, this.height - 52, 110, 20,
                CommonComponents.GUI_DONE,
                button -> { saveList(); Minecraft.getInstance().setScreen(null); }));
*///?}
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
        int maxScroll = Math.max(0, currentList.size() - visibleCount);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        rebuildWidgets();
        return true;
    }

    @SuppressWarnings("unchecked")
    private void saveList() {
        ConfigUtils.set(configKey, new ArrayList<>(currentList));
        ConfigUtils.save();
        // Reload from config to ensure consistency
        this.currentList = new ArrayList<>((List<Object>) ConfigUtils.get(configKey));
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
        Component itemsCountText = trans("gui.items", String.valueOf(currentList.size()));
//? if >=26.1 {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xffffffff);
        context.centeredText(this.font, itemsCountText, this.width / 2, 30, 0xffffffff);
//?} elif >=1.21.6 {
        /*super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffffff);
        context.drawCenteredString(this.font, itemsCountText, this.width / 2, 30, 0xffffffff);
*///?} elif >=1.20 {
        /*super.render(context, mouseX, mouseY, delta);
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffff);
        context.drawCenteredString(this.font, itemsCountText, this.width / 2, 30, 0xffffff);
*///?} else {
        /*super.render(context, mouseX, mouseY, delta);
        drawCenteredString(context, this.font, this.title, this.width / 2, 15, 0xffffff);
        drawCenteredString(context, this.font, itemsCountText, this.width / 2, 30, 0xffffff);
*///?}
    }

    @Override
    public void onClose() {
        saveList();
        Minecraft.getInstance().setScreen(parent);
    }

    // ---- Utility methods ----

    @SuppressWarnings("unchecked")
    private static String getSummary(Object item, String type, String configKey) {
        if (!(item instanceof Map)) {
            return item.toString();
        }
        Map<String, Object> map = (Map<String, Object>) item;

        String address = (String) map.getOrDefault("address", "*");
        boolean isSessionMatch = "*".equals(address) || RegExUtils.getOrCompilePattern(address)
                .matcher(ContextUtils.getSessionIdentifier()).matches();
        String colorPrefix = isSessionMatch ? "§a" : "§6";

        switch (type) {
            case "NotifierList": {
                String pattern = (String) map.getOrDefault("pattern", "");
                if ("*".equals(address) && "".equals(pattern)) {
                    return trans(configKey + ".@New").getString();
                }
                return trans(configKey + ".@Display", colorPrefix + address, pattern).getString();
            }
            case "BubbleList": {
                String pattern = (String) map.getOrDefault("pattern", "");
                boolean fallback = (Boolean) map.getOrDefault("fallback", false);
                if ("*".equals(address) && "<(?<name>.*?)> (?<message>.*)".equals(pattern)) {
                    return trans(configKey + ".@New").getString();
                }
                return trans(configKey + ".@Display", colorPrefix + address,
                        fallback ? "§a✔" : "§c✘", pattern).getString();
            }
            case "ResponderList": {
                String pattern = (String) map.getOrDefault("pattern", "");
                String message = (String) map.getOrDefault("message", "");
                boolean forceDisableFormatter = (Boolean) map.getOrDefault("forceDisableFormatter", false);
                long minDelay = ((Number) map.getOrDefault("minDelayInMilliseconds", 50.0)).longValue();
                long maxDelay = ((Number) map.getOrDefault("maxDelayInMilliseconds", 50.0)).longValue();

                if ("*".equals(address) && "Repeat my words:(?<word>.*)".equals(pattern) && "You said {word}.".equals(message)) {
                    return trans(configKey + ".@New").getString();
                }

                String delayInMillisecondsText = maxDelay > minDelay ? minDelay + "~" + maxDelay : String.valueOf(minDelay);
                return trans(configKey + ".@Display", colorPrefix + address,
                        forceDisableFormatter ? "§a✔" : "§c✘", delayInMillisecondsText, pattern, message).getString();
            }
            case "FormatterList": {
                String formatter = (String) map.getOrDefault("formatter", "{text}");
                if ("*".equals(address) && "{text}".equals(formatter)) {
                    return trans(configKey + ".@New").getString();
                }
                return trans(configKey + ".@Display", colorPrefix + address, formatter).getString();
            }
            case "CustomJoinMessageList": {
                String message = (String) map.getOrDefault("message", "");
                boolean forceDisableFormatter = (Boolean) map.getOrDefault("forceDisableFormatter", false);
                long delay = ((Number) map.getOrDefault("delayInMilliseconds", 1000.0)).longValue();

                if ("*".equals(address) && "/login xxx".equals(message)) {
                    return trans(configKey + ".@New").getString();
                }
                return trans(configKey + ".@Display", colorPrefix + address,
                        forceDisableFormatter ? "§a✔" : "§c✘", delay, message).getString();
            }
            case "MacroList": {
                String keyName = (String) map.getOrDefault("key", "key.keyboard.unknown");
                if (InputConstants.UNKNOWN.getName().equals(keyName)) {
                    return trans(configKey + ".@New").getString();
                }

                String modifier = (String) map.getOrDefault("modifier", "NONE");
                List<?> commands = (List<?>) map.getOrDefault("commands", Collections.emptyList());
                String firstCmd = "";
                if (!commands.isEmpty()) {
                    Object cmd = commands.get(0);
                    if (cmd instanceof Map) {
                        firstCmd = (String) ((Map) cmd).getOrDefault("command", "");
                    }
                }

                String displayKeyName;
                try {
                    displayKeyName = InputConstants.getKey(keyName).getDisplayName().getString();
                } catch (Exception e) {
                    displayKeyName = keyName;
                }

                if ("NONE".equals(modifier)) {
                    return trans(configKey + ".@Display", "§6" + displayKeyName, firstCmd).getString();
                } else {
                    return trans(configKey + ".@Display", "§6" + modifier + " + " + displayKeyName, firstCmd).getString();
                }
            }
            default:
                return item.toString();
        }
    }

    private static Object createDefaultItem(String type) {
        Map<String, Object> item = new LinkedHashMap<>();
        switch (type) {
            case "NotifierList":
                item.put("address", "*");
                item.put("pattern", "");
                item.put("toast", true);
                item.put("sound", true);
                item.put("actionbar", true);
                item.put("highlight", true);
                return item;
            case "BubbleList":
                item.put("address", "*");
                item.put("pattern", "<(?<name>.*?)> (?<message>.*)");
                item.put("fallback", false);
                return item;
            case "ResponderList":
                item.put("address", "*");
                item.put("pattern", "Repeat my words:(?<word>.*)");
                item.put("message", "You said {word}.");
                item.put("minDelayInMilliseconds", 50.0);
                item.put("maxDelayInMilliseconds", 50.0);
                item.put("forceDisableFormatter", false);
                return item;
            case "FormatterList":
                item.put("address", "*");
                item.put("formatter", "{text}");
                return item;
            case "CustomJoinMessageList":
                item.put("address", "*");
                item.put("message", "/login xxx");
                item.put("delayInMilliseconds", 1000.0);
                item.put("forceDisableFormatter", false);
                return item;
            case "MacroList":
                item.put("key", "key.keyboard.unknown");
                item.put("modifier", "NONE");
                item.put("mode", "LAZY");
                List<Map<String, Object>> commands = new ArrayList<>();
                Map<String, Object> cmd = new LinkedHashMap<>();
                cmd.put("command", "");
                cmd.put("delayInMilliseconds", 0.0);
                cmd.put("forceDisableFormatter", false);
                commands.add(cmd);
                item.put("commands", commands);
                return item;
            default:
                return item;
        }
    }
}
