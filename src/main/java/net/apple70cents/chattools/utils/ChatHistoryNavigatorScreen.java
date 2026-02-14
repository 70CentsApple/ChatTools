package net.apple70cents.chattools.utils;

import net.apple70cents.chattools.features.general.Timestamp;
import net.apple70cents.chattools.features.notifier.BasicNotifier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
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

//#if MC>=12109
import net.minecraft.client.input.MouseButtonEvent;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
//#endif

//#if MC>=11900
import net.minecraft.client.gui.components.Tooltip;
//#endif

//#if MC>=12000
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratedElementType;
//#elseif MC>=11700
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.gui.components.events.GuiEventListener;
//$$ import net.minecraft.client.gui.narration.NarratableEntry;
//$$ import net.minecraft.client.gui.narration.NarrationElementOutput;
//$$ import net.minecraft.client.gui.narration.NarratedElementType;
//#else
//$$ import com.mojang.blaze3d.vertex.PoseStack;
//$$ import net.minecraft.client.gui.components.events.GuiEventListener;
//#endif

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

        //#if MC>=11700
        this.addRenderableWidget(this.keywordField);
        //#else
        //$$ this.addButton(this.keywordField);
        //#endif
        this.setInitialFocus(this.keywordField);

        this.chatUnitListWidget = new ChatUnitListWidget(Minecraft.getInstance(), this.width - 60, this.height - 120, 65, font.lineHeight + 3, this.keywordField.getValue(), this.chatUnitListWidget);
        //#if MC>=12002
        this.chatUnitListWidget.setX(30);
        //#else
        //$$ this.chatUnitListWidget.setLeftPos(30);
        //#endif

        //#if MC>=12005
        //#elseif MC>=12003
        //$$ this.chatUnitListWidget.setRenderBackground(false);
        //#else
        //$$ this.chatUnitListWidget.setRenderBackground(false);
        //$$ this.chatUnitListWidget.setRenderTopAndBottom(false);
        //#endif
        this.addWidget(chatUnitListWidget);

        // Mode Selector Button
        Component modeSelectorButtonText = TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode());
        Button.OnPress pressAction = (button) -> {
            this.chatUnitListWidget.switchToNextSearchMode();
            this.modeSelectorWidget.setMessage(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode()));
            //#if MC>=11900
            this.modeSelectorWidget.setTooltip(Tooltip.create(TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip")));
            //#endif

            this.chatUnitListWidget.setKeyword(this.keywordField.getValue());
            this.chatUnitListWidget.refreshUnitEntries();
        };
        //#if MC>=11900
        this.modeSelectorWidget = Button.builder(modeSelectorButtonText, pressAction)
                                        .bounds(this.width - 120, 35, 90, 20).build();
        this.addRenderableWidget(modeSelectorWidget);
        //#elseif MC>=11700
        //$$ this.modeSelectorWidget = new Button(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, poseStack, mouseX, mouseY) -> renderTooltip(poseStack, TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"), mouseX, mouseY));
        //$$ addRenderableWidget(modeSelectorWidget);
        //#else
        //$$ this.modeSelectorWidget = new Button(this.width - 120, 35, 90, 20, modeSelectorButtonText, pressAction, (button, poseStack, mouseX, mouseY) -> TextUtils.trans("texts.ChatHistoryNavigator.modes." + this.chatUnitListWidget.getSearchMode() + ".@Tooltip"));
        //$$ addButton(modeSelectorWidget);
        //#endif

        // Done button
        //#if MC>=11900
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, (button) -> {
            this.onClose();
        }).bounds(this.width / 2 - 80, this.height - 28, 160, 20).build());
        //#elseif MC>=11700
        //$$ addRenderableWidget(new Button(this.width / 2 - 80, this.height - 28, 160, 20, CommonComponents.GUI_DONE, (button) -> {this.onClose();}));
        //#else
        //$$ addButton(new Button(this.width / 2 - 80, this.height - 28, 160, 20, CommonComponents.GUI_DONE, (button) -> {Minecraft.getInstance().setScreen(null);}));
        //#endif
    }


    @Override
    public void render(
            //#if MC>=12000
            GuiGraphics context
            //#else
            //$$ PoseStack context
            //#endif
            , int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        // this draws the title
        //#if MC>=12106
        context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffffff);
        //#elseif MC>=12000
        //$$ context.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xffffff);
        //#else
        //$$ drawCenteredString(context, this.font, this.title, this.width / 2, 15, 0xffffff);
        //#endif

        if (this.chatUnitListWidget.searchMode == SearchModes.REGEX) {
            try {
                if (this.keywordField != null) {
                    Pattern.compile(this.keywordField.getValue());
                }
            } catch (PatternSyntaxException e) {
                Component errorText = TextUtils.literal(e.getDescription()).copy()
                                               .setStyle(Style.EMPTY.applyFormat(ChatFormatting.RED));
                //#if MC>=12106
                context.setTooltipForNextFrame(font, errorText, mouseX, mouseY);
                //#elseif MC>=12000
                //$$ context.renderTooltip(font, errorText, mouseX, mouseY);
                //#else
                //$$ renderTooltip(context, errorText, mouseX, mouseY);
                //#endif
            }
        }

        if (!chatUnitListWidget.hashcodeResultList.isEmpty()) {
            chatUnitListWidget.render(context, mouseX, mouseY, delta);
        }
    }

    protected class ChatUnitEntry extends ContainerObjectSelectionList.Entry<ChatUnitEntry> {
        TextUtils.MessageUnit messageUnit;

        public ChatUnitEntry(String hashcode) {
            this.messageUnit = TextUtils.getMessageUnitByHash(hashcode);
        }

        @Override
        //#if MC>=12109
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            int button = event.button();
        //#else
        //$$ public boolean mouseClicked(double mouseX, double mouseY, int button) {
        //#endif
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

        //#if MC>=11700
        public List<? extends NarratableEntry> narratables() {
            return Collections.emptyList();
        }
        //#endif

        public List<? extends GuiEventListener> children() {
            return Collections.emptyList();
        }

        @Override
        //#if MC>=12109
        public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        //#elseif MC>=12000
        //$$ public void render(GuiGraphics context, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        //#else
        //$$ public void render(PoseStack context, int index, int y, int x, int itemWidth, int itemHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
        //#endif
            //#if MC>=12109
            context.drawString(font, this.getText(), this.getContentX(), this.getContentY(), 0xffffffff);
            //#elseif MC>=12000
            //$$ context.drawString(font, this.getText(), x, y, 0xffffffff);
            //#else
            //$$ drawString(context, font, this.getText(), x, y, 0xffffff);
            //#endif
            if (hovered) {
                // set cursor
                //#if MC>=12109
                context.requestCursor(CursorTypes.POINTING_HAND);
                //#endif


                List<Component> timestamps = Arrays.stream(this.getTooltip().getString().split("\n")).map(TextUtils::of)
                                                   .collect(Collectors.toList());
                //#if MC>=12106
                context.setComponentTooltipForNextFrame(font, timestamps, mouseX, mouseY);
                //#elseif MC>=12000
                //$$ context.renderComponentTooltip(font, timestamps, mouseX, mouseY);
                //#else
                //$$ renderComponentTooltip(context, timestamps, mouseX, mouseY);
                //#endif
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
            //#if MC>=12002
            super(client, width, height, y, itemHeight);
            //#else
            //$$ super(client, width, height, y, y + height, itemHeight);
            //#endif
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
        //#if MC>=12102
        scrollBarX()
        //#elseif MC>=12006
        //$$ getDefaultScrollbarPosition()
        //#else
        //$$ getScrollbarPosition()
        //#endif
        {
            //#if MC>=12002
            int x = this.getX();
            //#else
            //$$ int x = this.x0;
            //#endif
            return this.width - 7 + x;
        }

        //#if MC>=12104
        @Override
        protected double scrollRate() {
            int lineHeight = font.lineHeight + 3;
            //#if MC>=12109
            boolean shiftDown = Minecraft.getInstance().hasShiftDown();
            //#else
            //$$ boolean shiftDown = hasShiftDown();
            //#endif
            return shiftDown ? lineHeight : lineHeight * 7;
        }
        //#else
        //$$ @Override
        //$$ public boolean mouseScrolled(
        //$$    //#if MC>=12004
        //$$        double mouseX, double mouseY, double horizontalAmount, double verticalAmount
        //$$    //#else
        //$$        //$$ double mouseX, double mouseY, double verticalAmount
        //$$    //#endif
        //$$     ) {
        //$$     int lineHeight = font.lineHeight + 3;
        //$$     double scrollAmount = hasShiftDown() ? lineHeight : lineHeight * 7;
        //$$     this.setScrollAmount(this.getScrollAmount() - verticalAmount * scrollAmount);
        //$$     return true;
        //$$ }
        //#endif

        //#if MC>=12005
        @Override
        protected void renderListBackground(GuiGraphics context) {
        }
        //#endif

        //#if MC>=11900
        @Nullable
        public Tooltip getTooltip() {
            if (getHovered() == null) {
                return null;
            }
            return Tooltip.create(getHovered().getTooltip());
        }
        //#endif

        //#if MC>=11700
        //#if MC>=12004
        protected void updateWidgetNarration
        //#else
        //$$ @Override public void updateNarration
        //#endif
        (NarrationElementOutput builder) {
            if (getHovered() != null) {
                builder.add(NarratedElementType.TITLE, getHovered().getText());
            }
        }
        //#endif
    }
}
