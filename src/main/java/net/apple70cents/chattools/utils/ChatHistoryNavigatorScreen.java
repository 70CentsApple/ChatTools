package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.features.general.Timestamp;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
//?}

//? if >=1.19 {
import net.minecraft.client.gui.components.Tooltip;
//?}

//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
//?} elif >=1.20 {
/*import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
*///?} elif >=1.17 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.events.GuiEventListener;
*///?}

/**
 * @author 70CentsApple
 */
public class ChatHistoryNavigatorScreen extends Screen {
    @Nullable
    public EditBox keywordField;
    ChatUnitListWidget chatUnitListWidget;
    Button modeSelectorWidget;

    public ChatHistoryNavigatorScreen(Component title) {
        super(title);
    }

    @Override
    protected void init() {
        super.init();
        this.keywordField = new EditBox(this.font, 30, 35, this.width - 155, 20, this.keywordField, TextUtils.trans("texts.ChatHistoryNavigator.placeholder"));
        this.keywordField.setMaxLength(Integer.MAX_VALUE);
        this.keywordField.setResponder(keyword -> {
            this.chatUnitListWidget.setKeyword(keyword);
        });

//? if >=1.17 {
        this.addRenderableWidget(this.keywordField);
//?} else {
        /*this.addButton(this.keywordField);
*///?}
        this.setInitialFocus(this.keywordField);

        this.chatUnitListWidget = new ChatUnitListWidget(Minecraft.getInstance(), this.width - 60, this.height - 120, 65, font.lineHeight + 3, this.keywordField.getValue(), this.chatUnitListWidget);
//? if >=1.20.2 {
        this.chatUnitListWidget.setX(30);
//?} else {
        /*this.chatUnitListWidget.setLeftPos(30);
*///?}

//? if >=1.20.5 {
//?} elif >=1.20.3 {
        /*this.chatUnitListWidget.setRenderBackground(false);
*///?} else {
        /*this.chatUnitListWidget.setRenderBackground(false);
        this.chatUnitListWidget.setRenderTopAndBottom(false);
*///?}
        this.addWidget(chatUnitListWidget);

        // Mode Selector Button
        Component modeSelectorButtonText = TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode());
        Button.OnPress pressAction = (button) -> {
            this.chatUnitListWidget.switchToNextSearchMode();
            this.modeSelectorWidget.setMessage(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode()));
//? if >=1.19 {
            this.modeSelectorWidget.setTooltip(Tooltip.create(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip")));
//?}

            this.chatUnitListWidget.setKeyword(this.keywordField.getValue());
            this.chatUnitListWidget.refreshUnitEntries();
        };
//? if >=1.19 {
        this.modeSelectorWidget = Button.builder(modeSelectorButtonText, pressAction)
                                        .bounds(this.width - 120, 35, 90, 20).build();
        this.addRenderableWidget(modeSelectorWidget);
//?} elif >=1.17 {
        /*this.modeSelectorWidget = new Button(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, poseStack, mouseX, mouseY) -> renderTooltip(poseStack, TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"), mouseX, mouseY));
        addRenderableWidget(modeSelectorWidget);
*///?} else {
        /*this.modeSelectorWidget = new Button(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, poseStack, mouseX, mouseY) -> TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"));
        addButton(modeSelectorWidget);
*///?}

        // Done button
//? if >=1.19 {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 80, this.height - 28, 160, 20).build());
//?} elif >=1.17 {
        /*addRenderableWidget(new Button(this.width / 2 - 80, this.height - 28, 160, 20, CommonComponents.GUI_DONE, (button) -> {this.onClose();}));
*///?} else {
        /*addButton(new Button(this.width / 2 - 80, this.height - 28, 160, 20, CommonComponents.GUI_DONE, (button) -> {Minecraft.getInstance().setScreen(null);}));
*///?}
    }


    @Override
    public void
//? if >=26.1{
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
        // this draws the title
//? if >=26.1 {
        context.centeredText(this.font, this.title, this.width / 2, 15, 0xffffffff);
//?} elif >=1.21.6 {
        /*context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffffff);
*///?} elif >=1.20 {
        /*context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffff);
*///?} else {
        /*drawCenteredString(context, this.font, this.title, this.width / 2, 15, 0xffffff);
*///?}

        if (this.chatUnitListWidget.searchMode == SearchModes.REGEX) {
            try {
                if (this.keywordField != null) {
                    Pattern.compile(this.keywordField.getValue());
                }
            } catch (PatternSyntaxException e) {
                Component errorText = TextUtils.literal(e.getDescription()).copy()
                                               .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED));
//? if >=1.21.6 {
                context.setTooltipForNextFrame(font, errorText, mouseX, mouseY);
//?} elif >=1.20 {
                /*context.renderTooltip(font, errorText, mouseX, mouseY);
*///?} else {
                /*renderTooltip(context, errorText, mouseX, mouseY);
*///?}
            }
        }

        if (!chatUnitListWidget.hashcodeResultList.isEmpty()) {
//? if >=26.1{
            chatUnitListWidget.extractRenderState(context, mouseX, mouseY, delta);
//?} else {
            /*chatUnitListWidget.render(context, mouseX, mouseY, delta);
*///?}
        }
    }

    protected class ChatUnitEntry extends ContainerObjectSelectionList.Entry<ChatUnitEntry> {
        TextUtils.MessageUnit messageUnit;

        public ChatUnitEntry(String hashcode) {
            this.messageUnit = TextUtils.getMessageUnitByHash(hashcode);
        }

        @Override
//? if >=1.21.9 {
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            int button = event.button();
//?} else {
        /*public boolean mouseClicked(double mouseX, double mouseY, int button) {
*///?}
            // left click
            if (button == 0) {
                Minecraft.getInstance().setScreen(new CopyFeatureScreen(messageUnit));
                return true;
            }
            return false;
        }

        public Component getText() {
            if (this.messageUnit == null) {
                return TextUtils.literal("§lOutdated message! It should NOT be here!");
            }
            if (!(boolean) ConfigUtils.get("general.ChatHistoryNavigator.ShowTimestampsEnabled")) {
                return this.messageUnit.message;
            } else {
                LocalDateTime time = LocalDateTime.ofEpochSecond(this.messageUnit.unixTimestamp, 0, ZoneId.systemDefault().getRules().getOffset(Instant.now()));
                Component timestamp = TextUtils.of(Timestamp.timeInFormat((String) ConfigUtils.get("general.Timestamp.Pattern"), time));
                return (TextUtils.SPACER.copy().append(timestamp)).append(this.messageUnit.message);
            }
        }

        public Component getTooltip() {
            LocalDateTime time = LocalDateTime.ofEpochSecond(this.messageUnit.unixTimestamp, 0, ZoneId.systemDefault()
                                                                                                      .getRules()
                                                                                                      .getOffset(Instant.now()));
            String offsetString = ZoneId.systemDefault().getRules().getOffset(Instant.now()).getId();
            // yyyy/MM/dd HH:mm:ss UTC±XX:XX
            Component longTimeDisplay = TextUtils.of(String.format("%4d/%d/%d %02d:%02d:%02d\nUTC%s", time.getYear(), time
                    .getMonth()
                    .getValue(), time.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond(), offsetString));
            return longTimeDisplay;
        }

//? if >=1.17 {
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
//?}

        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
//? if >=26.1 {
        public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//?} elif >=1.21.9 {
        /*public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
*///?} elif >=1.20 {
        /*public void render(GuiGraphics context, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
*///?} else {
        /*public void render(PoseStack context, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
*///?}
//? if >=26.1 {
            context.text(font, this.getText(), this.getContentX(), this.getContentY(), 0xffffffff);
//?} elif >=1.21.9 {
            /*context.drawString(font, this.getText(), this.getContentX(), this.getContentY(), 0xffffffff);
*///?} elif >=1.20 {
            /*context.drawString(font, this.getText(), x, y, 0xffffffff);
*///?} else {
            /*drawString(context, font, this.getText(), x, y, 0xffffff);
*///?}
            if (hovered) {
                // set cursor
//? if >=1.21.9 {
                context.requestCursor(CursorTypes.POINTING_HAND);
//?}


                List<Component> timestamps = Arrays.stream(this.getTooltip().getString().split("\n")).map(TextUtils::of)
                                                   .collect(Collectors.toList());
//? if >=1.21.6 {
                context.setComponentTooltipForNextFrame(font, timestamps, mouseX, mouseY);
//?} elif >=1.20 {
                /*context.renderComponentTooltip(font, timestamps, mouseX, mouseY);
*///?} else {
                /*renderComponentTooltip(context, timestamps, mouseX, mouseY);
*///?}
            }
        }
    }

    public enum SearchModes {
        CASE_INSENSITIVE, CASE_SENSITIVE, SUBSCRIBED, REGEX
    }

    protected class ChatUnitListWidget extends AbstractSelectionList<ChatUnitEntry> {
        private List<String> hashcodeResultList;
        private SearchModes searchMode;

        protected void updateResultList(String keyword) {
            if (hashcodeResultList == null) {
                hashcodeResultList = new ArrayList<>();
            }
            hashcodeResultList.clear();
            if ((keyword == null || keyword.trim().isEmpty()) && searchMode != SearchModes.SUBSCRIBED) {
                return;
            }

            if (searchMode == SearchModes.REGEX) {
                try {
                    Pattern.compile(keyword);
                } catch (PatternSyntaxException e) {
                    return;
                }
            }
            Predicate<Map.Entry<String, TextUtils.MessageUnit>> filter = null;
            switch (searchMode) {
                case CASE_INSENSITIVE:
                    filter = entry -> TextUtils.wash(entry.getValue().message.getString().toLowerCase())
                                               .contains(keyword.toLowerCase());
                    break;
                case CASE_SENSITIVE:
                    filter = entry -> TextUtils.wash(entry.getValue().message.getString()).contains(keyword);
                    break;
                case SUBSCRIBED:
                    filter = entry -> BasicNotifier.shouldWork(entry.getValue().message) != null;
                    break;
                case REGEX:
                    try {
                        filter = entry -> Pattern.compile(keyword, Pattern.MULTILINE)
                                                 .matcher(TextUtils.wash(entry.getValue().message.getString())).find();
                    } catch (PatternSyntaxException e) {
                        return;
                    }
                    break;
            }
            Stream<Map.Entry<String, TextUtils.MessageUnit>> stream = TextUtils.messageMap.entrySet().stream();
            hashcodeResultList = stream.filter(filter).map(Map.Entry::getKey).collect(Collectors.toList());
        }

        protected void refreshUnitEntries() {
            if (chatUnitListWidget == null) {
                return;
            }
            this.clearEntries();
            for (String hashcode : hashcodeResultList) {
                this.addEntry(new ChatUnitEntry(hashcode));
            }
        }

        public void setKeyword(String keyword) {
            this.updateResultList(keyword);
            this.refreshUnitEntries();
        }

        public void switchToNextSearchMode() {
            if (this.searchMode == SearchModes.CASE_INSENSITIVE) {
                this.searchMode = SearchModes.CASE_SENSITIVE;
            } else if (this.searchMode == SearchModes.CASE_SENSITIVE) {
                this.searchMode = SearchModes.SUBSCRIBED;
            } else if (this.searchMode == SearchModes.SUBSCRIBED) {
                this.searchMode = SearchModes.REGEX;
            } else if (this.searchMode == SearchModes.REGEX) {
                this.searchMode = SearchModes.CASE_INSENSITIVE;
            }
        }

        public void setSearchMode(SearchModes searchMode) {
            this.searchMode = searchMode;
        }

        public SearchModes getSearchMode() {
            return this.searchMode;
        }

        public ChatUnitListWidget(Minecraft client, int width, int height, int y, int itemHeight, String keyword, @Nullable ChatUnitListWidget copyFrom) {
//? if >=1.20.2 {
            super(client, width, height, y, itemHeight);
//?} else {
            /*super(client, width, height, y, y + height, itemHeight);
*///?}
            this.searchMode = SearchModes.CASE_INSENSITIVE;
            if (copyFrom != null) {
                this.hashcodeResultList = copyFrom.hashcodeResultList;
                this.searchMode = copyFrom.getSearchMode();
            } else {
                this.hashcodeResultList = new ArrayList<>();
                this.updateResultList(keyword);
            }
            this.refreshUnitEntries();
        }

        @Override
        public int getRowWidth() {
            return this.width - 15;
        }

        @Override
        protected int
//? if >=1.21.2 {
        scrollBarX()
//?} elif >=1.20.6 {
        /*getDefaultScrollbarPosition()
*///?} else {
        /*getScrollbarPosition()
*///?}
        {
//? if >=1.20.2 {
            int x = this.getX();
//?} else {
            /*int x = this.x0;
*///?}
            return this.width - 7 + x;
        }

//? if >=1.21.4 {
        @Override
        protected double scrollRate() {
            int lineHeight = font.lineHeight + 3;
    //? if >=1.21.9 {
            boolean shiftDown = Minecraft.getInstance().hasShiftDown();
    //?} else {
            /*boolean shiftDown = hasShiftDown();
    *///?}
            return shiftDown ? lineHeight : lineHeight * 7;
        }
//?} else {
        /*@Override
        public boolean mouseScrolled(
           //? if >=1.20.4 {
                double mouseX, double mouseY, double horizontalAmount, double verticalAmount
           //?} else {
                /^double mouseX, double mouseY, double verticalAmount
           ^///?}
            ) {
            int lineHeight = font.lineHeight + 3;
            double scrollAmount = hasShiftDown() ? lineHeight : lineHeight * 7;
            this.setScrollAmount(this.getScrollAmount() - verticalAmount * scrollAmount);
            return true;
        }
*///?}

//? if >=26.1 {
        @Override
        protected void extractListBackground(@NotNull GuiGraphicsExtractor context) {
        }
//?} elif >=1.20.5 {
        /*@Override
        protected void renderListBackground(GuiGraphics context) {
        }
*///?}

//? if >=1.19 {
        @Nullable
        public Tooltip getTooltip() {
            if (getHovered() == null) {
                return null;
            }
            return Tooltip.create(getHovered().getTooltip());
        }
//?}

//? if >=1.17 {
    //? if >=1.20.4 {
        protected void updateWidgetNarration
    //?} else {
        /*@Override public void updateNarration
    *///?}
        (NarrationElementOutput builder) {
            if (getHovered() != null) {
                builder.add(NarratedElementType.TITLE, getHovered().getText());
            }
        }
//?}
    }
}
