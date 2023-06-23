package net.apple70cents.chattools.features.chatnotifier;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

public class ChatNotifier {
    static MinecraftClient client = MinecraftClient.getInstance();
    static ModClothConfig config = ModClothConfig.get();

    private static boolean justSentMessage = false;

    public static boolean isJustSentMessage() {
        return justSentMessage;
    }

    public static void setJustSentMessage(boolean bl) {
        justSentMessage = bl;
    }

    public static Text deal(Text message, MessageIndicator indicator) {
        if (config.chatBubblesEnabled) {
            BubbleRenderer.addChatBubble(message);
        }
        message = highlightAndNotify(message, indicator);

        // 处理昵称隐藏
        if (config.nickHiderSettings.nickHiderEnabled && client.player != null) {
            message = Text.of(message.getString().replace(client.player.getDisplayName().getString(), config.nickHiderSettings.nickHiderText.replace('&', '§').replace("\\§", "&")));
        }

        // 显示聊天时间
        if (config.displayChatTimeEnabled) {
            // TODO 自定义的Timestamp格式
            // TODO 兼容Compact Chat
            LocalDateTime currentTime = LocalDateTime.now();
            // 获取时区偏移量
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(currentTime);
            String offsetString = zoneOffset.getId();
            // §e[hh:mm:ss] §r
            Text shortTimeDisplay = Text.of(String.format("§e[%d:%02d:%02d] §r", currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()));
            // yyyy/MM/dd hh:mm:ss UTC±XX:XX
            Text longTimeDisplay = Text.of(String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime.getMonth().getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString));
            message = stylish((MutableText) shortTimeDisplay,
                    // 悬停文本
                    (style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, longTimeDisplay)))).append(message);
        }
        return message;
    }

    /**
     * 给文字加样式
     *
     * @param text         被处理的文字
     * @param styleUpdater 样式（lambda表达式）
     * @return 处理后的文字
     */
    private static MutableText stylish(MutableText text, UnaryOperator<Style> styleUpdater) {
        text.setStyle(styleUpdater.apply(text.getStyle()));
        return text;
    }

    /**
     * <i>尝试</i>高亮消息并做对应的消息提醒功能
     *
     * @param text 消息
     * @return 经过高亮处理后的消息
     */
    private static Text highlightAndNotify(Text text, MessageIndicator indicator) {
        // 匹配机制
        boolean shouldMatch = false;
        for (int i = 0; i < config.allowList.size(); i++) {
            if (Pattern.compile(config.allowList.get(i), Pattern.MULTILINE).matcher(text.getString()).find()) { // 匹配白名单正则表达式
                shouldMatch = true;
                break;
            }
        }
        if (config.matchSelfName && client.player != null && Pattern.compile(client.player.getName().getString(), Pattern.MULTILINE).matcher(text.getString()).find()) { // 应匹配名字 && 匹配名字
            shouldMatch = true;
        }
        for (int i = 0; i < config.banList.size(); i++) {
            if (Pattern.compile(config.banList.get(i), Pattern.MULTILINE).matcher(text.getString()).find()) {// 匹配黑名单正则表达式
                shouldMatch = false;
                break;
            }
        }
        // 应忽略系统消息 && 系统消息
        if (indicator != null) {
            if (config.ignoreSystemMessage && (indicator.equals(MessageIndicator.system()) || indicator.equals(MessageIndicator.singlePlayer()))) {
                shouldMatch = false;
            }
        }
        if (!config.modEnabled) { // 启用 ChatTools 则向下走
            setJustSentMessage(false);
            return text;
        } else if (!shouldMatch) { // 匹配 Regex 则向下走
            setJustSentMessage(false);
            return text;
        } else if (config.ignoreSelf && isJustSentMessage()) {
            // 判断发件人为自己 与 `忽略自身消息`启用
            setJustSentMessage(false);
            return text;
        } else {
            setJustSentMessage(false);
            // 下面的是主要代码
            ChatTools.LOGGER.info("[ChatTools] Found the latest chat message matches customized RegEx");

            // 弹窗提示
            if (config.toastNotifySettings.toastNotifyEnabled && !client.isWindowFocused()) {
                if (ModClothConfig.ToastMode.POWERSHELL.equals(config.toastNotifySettings.toastNotifyMode)) {
                    SystemToast.toastWithPowershell(Text.translatable("key.chattools.toast.title").getString(), text.getString());
                } else if (ModClothConfig.ToastMode.AWT.equals(config.toastNotifySettings.toastNotifyMode)) {
                    SystemToast.toastWithAWT(Text.translatable("key.chattools.toast.title").getString(), text.getString());
                }
            }

            // ActionBar 提示
            if (config.actionbarSettings.actionbarNotifyEnabled && client.player != null) {
                client.player.sendMessage(Text.translatable("key.chattools.match"), true);
            }

            // 音效提示
            if (config.soundSettings.soundNotifyEnabled && client.player != null) {
                client.player.playSound(SoundEvent.of(new Identifier(config.soundSettings.chatNotifySound)), SoundCategory.PLAYERS, config.soundSettings.chatNotifyVolume * 0.01F, config.soundSettings.chatNotifyPitch * 0.1F);
            }

            // 高亮提示
            String prefix = config.highlightSettings.highlightPrefix.replace('&', '§').replace("\\§", "&");
            Text highlightedMsg;
            if (config.highlightSettings.enforceOverwriting) {
                highlightedMsg = Text.translatable(prefix + text.getString());
            } else {
                highlightedMsg = Text.translatable(prefix).append(text);
            }
            return config.highlightSettings.highlightEnabled ? highlightedMsg : text;
        }
    }
}
