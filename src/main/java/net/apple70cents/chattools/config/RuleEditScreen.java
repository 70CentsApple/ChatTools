package net.apple70cents.chattools.config;

import com.mojang.blaze3d.platform.InputConstants;
import net.apple70cents.chattools.utils.ConfigUtils;
import net.apple70cents.chattools.utils.ContextUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;

//? if >=1.21.9 {
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?}

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
 * Screen for editing individual rule items in a complex list.
 * Dynamically generates EditBox/Button widgets based on the rule's fields.
 *
 * @author 70CentsApple
 */
public class RuleEditScreen extends Screen {
    private final Screen parent;
    private final String configKey;
    private final String type;
    private final List<Object> listRef;
    private final int index;
    private final Map<String, Object> item;
    private final List<FieldEntry> fieldEntries = new ArrayList<>();

    private static final int FIELD_HEIGHT = 28;
    private static final int FIELDS_TOP = 40;
    private int scrollOffset = 0;

    @SuppressWarnings("unchecked")
    public RuleEditScreen(Screen parent, String configKey, String type, List<Object> listRef, int index) {
        super(trans(configKey, "§f" + ContextUtils.getSessionIdentifier()).copy()
                .append(TextUtils.literal(" #" + (index + 1))));
        this.parent = parent;
        this.configKey = configKey;
        this.type = type;
        this.listRef = listRef;
        this.index = index;
        // Work with a copy
        this.item = new LinkedHashMap<>((Map<String, Object>) listRef.get(index));
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
        fieldEntries.clear();

        int y = FIELDS_TOP - scrollOffset;
        int labelWidth = 130;
        int fieldX = labelWidth + 10;
        int fieldWidth = this.width - fieldX - 30;

        List<String> fieldOrder = getFieldOrder(type);
        for (String fieldName : fieldOrder) {
            Object value = item.get(fieldName);
            if (value == null) continue;

            if (fieldName.equals("commands")) {
                // Nested list - show a button to edit sub-commands
//? if >=1.19 {
                List<?> commands = (List<?>) value;
                Button cmdButton = Button.builder(
                        TextUtils.literal("§e✎ " + trans("gui.editCommands").getString() + " (" + commands.size() + ")"),
                        button -> {
                            saveItem();
                            Minecraft.getInstance().setScreen(new MacroCommandListScreen(this, item));
                        }
                ).bounds(fieldX, y, fieldWidth, 20).build();
                this.addRenderableWidget(cmdButton);
//?} elif >=1.17 {
                /*List<?> commands = (List<?>) value;
                Button cmdButton = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e✎ " + trans("gui.editCommands").getString() + " (" + commands.size() + ")"),
                        button -> { saveItem(); Minecraft.getInstance().setScreen(new MacroCommandListScreen(this, item)); });
                this.addRenderableWidget(cmdButton);
*///?} else {
                /*List<?> commands = (List<?>) value;
                Button cmdButton = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e✎ " + trans("gui.editCommands").getString() + " (" + commands.size() + ")"),
                        button -> { saveItem(); Minecraft.getInstance().setScreen(new MacroCommandListScreen(this, item)); });
                this.addButton(cmdButton);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, null, null, y));
            } else if (value instanceof Boolean) {
                boolean boolVal = (Boolean) value;
                Component boolDisplay = TextUtils.literal(boolVal ? "§a✔" : "§c✘");
//? if >=1.19 {
                Button toggle = Button.builder(
                        boolDisplay,
                        button -> {
                            boolean newVal = !(Boolean) item.get(fieldName);
                            item.put(fieldName, newVal);
                            rebuildWidgets();
                        }
                ).bounds(fieldX, y, fieldWidth, 20).build();
                this.addRenderableWidget(toggle);
//?} elif >=1.17 {
                /*Button toggle = new Button(fieldX, y, fieldWidth, 20, boolDisplay,
                        button -> { boolean newVal = !(Boolean) item.get(fieldName); item.put(fieldName, newVal); rebuildWidgets(); });
                this.addRenderableWidget(toggle);
*///?} else {
                /*Button toggle = new Button(fieldX, y, fieldWidth, 20, boolDisplay,
                        button -> { boolean newVal = !(Boolean) item.get(fieldName); item.put(fieldName, newVal); rebuildWidgets(); });
                this.addButton(toggle);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, null, null, y));
            } else if (fieldName.equals("key")) {
                // Keycode field
                String keyName;
                try {
                    keyName = InputConstants.getKey(value.toString()).getDisplayName().getString();
                } catch (Exception e) {
                    keyName = value.toString();
                }
//? if >=1.19 {
                Button keyBtn = Button.builder(
                        TextUtils.literal("§e" + keyName),
                        button -> Minecraft.getInstance().setScreen(new Screen(trans("gui.pressAnyKey")) {
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
                                    Minecraft.getInstance().setScreen(RuleEditScreen.this);
                                    return true;
                                }
                                item.put(fieldName, key2.getName());
                                Minecraft.getInstance().setScreen(RuleEditScreen.this);
                                return true;
                            }
                            @Override
//? if >=1.21.9 {
                            public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
                                int button = event.button();
//?} else {
                            /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
*///?}
                                item.put(fieldName, InputConstants.Type.MOUSE.getOrCreate(button).getName());
                                Minecraft.getInstance().setScreen(RuleEditScreen.this);
                                return true;
                            }
                        })
                ).bounds(fieldX, y, fieldWidth, 20).build();
                this.addRenderableWidget(keyBtn);
//?} elif >=1.17 {
                /*Button keyBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + keyName),
                        button -> {Minecraft.getInstance().setScreen(RuleEditScreen.this);});
                this.addRenderableWidget(keyBtn);
*///?} else {
                /*Button keyBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + keyName),
                        button -> {Minecraft.getInstance().setScreen(RuleEditScreen.this);});
                this.addButton(keyBtn);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, null, null, y));
            } else if (fieldName.equals("modifier")) {
                // Enum cycle for modifier
                String modVal = value.toString();
//? if >=1.19 {
                Button modBtn = Button.builder(
                        TextUtils.literal("§e" + modVal),
                        button -> {
                            SpecialUnits.KeyModifiers[] vals = SpecialUnits.KeyModifiers.values();
                            int idx = 0;
                            for (int i = 0; i < vals.length; i++) {
                                if (vals[i].name().equals(item.get(fieldName).toString())) {
                                    idx = i;
                                    break;
                                }
                            }
                            item.put(fieldName, vals[(idx + 1) % vals.length].name());
                            rebuildWidgets();
                        }
                ).bounds(fieldX, y, fieldWidth, 20).build();
                this.addRenderableWidget(modBtn);
//?} elif >=1.17 {
                /*Button modBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + modVal),
                        button -> {
                            SpecialUnits.KeyModifiers[] vals = SpecialUnits.KeyModifiers.values();
                            int idx = 0;
                            for (int i = 0; i < vals.length; i++) {
                                if (vals[i].name().equals(item.get(fieldName).toString())) { idx = i; break; }
                            }
                            item.put(fieldName, vals[(idx + 1) % vals.length].name());
                            rebuildWidgets();
                        });
                this.addRenderableWidget(modBtn);
*///?} else {
                /*Button modBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + modVal),
                        button -> {
                            SpecialUnits.KeyModifiers[] vals = SpecialUnits.KeyModifiers.values();
                            int idx = 0;
                            for (int i = 0; i < vals.length; i++) {
                                if (vals[i].name().equals(item.get(fieldName).toString())) { idx = i; break; }
                            }
                            item.put(fieldName, vals[(idx + 1) % vals.length].name());
                            rebuildWidgets();
                        });
                this.addButton(modBtn);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, null, null, y));
            } else if (fieldName.equals("mode")) {
                // Macro mode cycle
                String modeVal = value.toString();
                String[] modes = {"LAZY", "GREEDY"};
//? if >=1.19 {
                Button modeBtn = Button.builder(
                        TextUtils.literal("§e" + modeVal),
                        button -> {
                            String cur = item.get(fieldName).toString();
                            item.put(fieldName, cur.equals("LAZY") ? "GREEDY" : "LAZY");
                            rebuildWidgets();
                        }
                ).bounds(fieldX, y, fieldWidth, 20).build();
                this.addRenderableWidget(modeBtn);
//?} elif >=1.17 {
                /*Button modeBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + modeVal),
                        button -> {
                            String cur = item.get(fieldName).toString();
                            item.put(fieldName, cur.equals("LAZY") ? "GREEDY" : "LAZY");
                            rebuildWidgets();
                        });
                this.addRenderableWidget(modeBtn);
*///?} else {
                /*Button modeBtn = new Button(fieldX, y, fieldWidth, 20,
                        TextUtils.literal("§e" + modeVal),
                        button -> {
                            String cur = item.get(fieldName).toString();
                            item.put(fieldName, cur.equals("LAZY") ? "GREEDY" : "LAZY");
                            rebuildWidgets();
                        });
                this.addButton(modeBtn);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, null, null, y));
            } else {
                // String/Number field - use EditBox
                EditBox editBox = new EditBox(this.font, fieldX, y, fieldWidth, 20, TextUtils.literal(fieldName));
                editBox.setMaxLength(Integer.MAX_VALUE);
                editBox.setValue(value.toString());
                editBox.setResponder(newVal -> {
                    Object parsedValue = parseFieldValue(value, newVal);
                    item.put(fieldName, parsedValue);
                });
//? if >=1.17 {
                this.addRenderableWidget(editBox);
//?} else {
                /*this.addButton(editBox);
*///?}
                fieldEntries.add(new FieldEntry(fieldName, editBox, null, y));
            }
            y += FIELD_HEIGHT;
        }

        // Done button
//? if >=1.19 {
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    saveItem();
                    Minecraft.getInstance().setScreen(parent);
                }
        ).bounds(this.width / 2 - 80, this.height - 30, 160, 20).build());
//?} elif >=1.17 {
        /*this.addRenderableWidget(new Button(this.width / 2 - 80, this.height - 30, 160, 20,
                CommonComponents.GUI_DONE,
                button -> { saveItem(); Minecraft.getInstance().setScreen(parent); }));
*///?} else {
        /*this.addButton(new Button(this.width / 2 - 80, this.height - 30, 160, 20,
                CommonComponents.GUI_DONE,
                button -> { saveItem(); Minecraft.getInstance().setScreen(parent); }));
*///?}
    }

    private void saveItem() {
        listRef.set(index, new LinkedHashMap<>(item));
        ConfigUtils.set(configKey, new ArrayList<>(listRef));
        ConfigUtils.save();
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

        // Draw field labels
        for (FieldEntry entry : fieldEntries) {
            String capitalizedField = entry.fieldName.substring(0, 1).toUpperCase() + entry.fieldName.substring(1);
            Style style = Style.EMPTY.withHoverEvent(
//? if >=1.21.5 {
                    new HoverEvent.ShowText(
//?} else {
/*new HoverEvent(HoverEvent.Action.SHOW_TEXT,
*///?}
                    TextUtils.trans(this.configKey + "." + capitalizedField + ".@Tooltip")));
            Component label = TextUtils.trans(this.configKey + "." + capitalizedField).copy().withStyle(style);
//? if >=26.1 {
            context.text(this.font, label, 10, entry.y + 6, 0xffffffff);
//?} elif >=1.20 {
            /*context.drawString(this.font, label, 10, entry.y + 6, 0xffffff);
*///?} else {
            /*drawString(context, this.font, label, 10, entry.y + 6, 0xffffff);
*///?}
        }
    }

    @Override
    public void onClose() {
        saveItem();
        Minecraft.getInstance().setScreen(parent);
    }

    private static Object parseFieldValue(Object original, String newVal) {
        if (original instanceof Double || original instanceof Float) {
            try { return Double.parseDouble(newVal); } catch (NumberFormatException e) { return original; }
        }
        if (original instanceof Integer || original instanceof Long) {
            try { return Integer.parseInt(newVal); } catch (NumberFormatException e) { return original; }
        }
        if (original instanceof Number) {
            try { return Double.parseDouble(newVal); } catch (NumberFormatException e) { return original; }
        }
        return newVal;
    }

    private static List<String> getFieldOrder(String type) {
        switch (type) {
            case "NotifierList":
                return Arrays.asList("address", "pattern", "toast", "sound", "actionbar", "highlight");
            case "BubbleList":
                return Arrays.asList("address", "pattern", "fallback");
            case "ResponderList":
                return Arrays.asList("address", "pattern", "message", "minDelayInMilliseconds", "maxDelayInMilliseconds", "forceDisableFormatter");
            case "FormatterList":
                return Arrays.asList("address", "formatter");
            case "CustomJoinMessageList":
                return Arrays.asList("address", "message", "delayInMilliseconds", "forceDisableFormatter");
            case "MacroList":
                return Arrays.asList("key", "modifier", "mode", "commands");
            default:
                return Collections.emptyList();
        }
    }

    private static class FieldEntry {
        final String fieldName;
        final EditBox editBox;
        final Button button;
        final int y;

        FieldEntry(String fieldName, EditBox editBox, Button button, int y) {
            this.fieldName = fieldName;
            this.editBox = editBox;
            this.button = button;
            this.y = y;
        }
    }
}
