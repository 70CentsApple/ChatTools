package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.MyToastNotification;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.network.MessageType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.apple70cents.chattools.ChatTools.config;

@Mixin(ChatHudListener.class)
public abstract class ChatHudListenerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(MessageType type, Text message, UUID senderUuid, CallbackInfo ci) {
        if (config.chatBubblesEnabled) {
            BubbleRenderer.addChatBubble(message);
        }
        message = highlightAndNotify(type, message, senderUuid);
        // 显示聊天时间
        if (config.displayChatTimeEnabled) {
            LocalDateTime currentTime = LocalDateTime.now();
            // 获取时区偏移量
            ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(currentTime);
            String offsetString = zoneOffset.getId();
            message = new LiteralText(String.format("§e[%d:%02d:%02d] §r", currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond()))
                    // 悬停文本
                    .styled(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            // yyyy/MM/dd hh:mm:ss UTC±XX:XX
                            Text.of(String.format("%4d/%d/%d %d:%02d:%02d\nUTC%s", currentTime.getYear(), currentTime.getMonth().getValue(), currentTime.getDayOfMonth(), currentTime.getHour(), currentTime.getMinute(), currentTime.getSecond(), offsetString))))).append(message);
        }
        if (type != MessageType.CHAT) {
            this.client.inGameHud.getChatHud().addMessage(message);
        } else {
            this.client.inGameHud.getChatHud().queueMessage(message);
        }
        ci.cancel();
    }

    /**
     * **尝试**高亮消息并做对应的消息提醒功能
     *
     * @param type       消息类型
     * @param text       消息
     * @param senderUuid 发送者UUID
     * @return 经过高亮处理后的消息
     */
    private Text highlightAndNotify(MessageType type, Text text, UUID senderUuid) {
        ModClothConfig config = ModClothConfig.get();
        // 匹配机制
        boolean shouldMatch = false;
        for (int i = 0; i < config.allowList.size(); i++) {
            if (Pattern.compile(config.allowList.get(i), Pattern.MULTILINE).matcher(text.getString()).find()) { // 匹配白名单正则表达式
                shouldMatch = true;
                break;
            }
        }
        if (config.matchSelfName && Pattern.compile(this.client.player.getName().getString(), Pattern.MULTILINE).matcher(text.getString()).find()) { // 应匹配名字 && 匹配名字
            shouldMatch = true;
        }
        for (int i = 0; i < config.banList.size(); i++) {
            if (Pattern.compile(config.banList.get(i), Pattern.MULTILINE).matcher(text.getString()).find() || // 匹配黑名单正则表达式
                    (config.ignoreSystemMessage && type.equals(MessageType.SYSTEM))) { // 应忽略系统消息 && 系统消息
                shouldMatch = false;
                break;
            }
        }
        if (!config.modEnabled) {
        } // 启用 ChatTools 则向下走
        else if (!shouldMatch) {
        } // 匹配 Regex 则向下走
        else if (config.ignoreSelf && (senderUuid.equals(this.client.player.getUuid()))) {
        } // 如果 忽略自己的信息且是消息自己发出的 则 什么都不做
        else {
            // 下面的是主要代码
            ChatTools.LOGGER.info("[ChatTools] Found the latest chat message matches customized RegEx");
            System.setProperty("java.awt.headless", "false");

            // 弹窗提示
            if (config.toastNotify && !this.client.isWindowFocused()) {
                MyToastNotification.toast(new TranslatableText("key.chattools.toast.title").getString(), text.getString());
            }

            // ActionBar 提示
            if (config.actionbarSettings.actionbarNotifyEnabled) {
                this.client.player.sendMessage(new TranslatableText("key.chattools.match"), true);
            }

            // 音效提示
            if (config.soundSettings.soundNotifyEnabled) {
                this.client.player.playSound(new SoundEvent(new Identifier(config.soundSettings.chatNotifySound)), SoundCategory.PLAYERS, config.soundSettings.chatNotifyVolume * 0.01F, config.soundSettings.chatNotifyPitch * 0.1F);
            }

            // 高亮提示
            String prefix = config.highlightSettings.highlightPrefix.replace('&', '§').replace("\\§", "&");
            Text highlightedMsg;
            if (config.highlightSettings.enforceOverwriting) {
                highlightedMsg = (Text) new TranslatableText(prefix + text.getString());
            } else {
                highlightedMsg = (Text) new TranslatableText(prefix).append(text);
            }
            Text targetMsg = config.highlightSettings.highlightEnabled ? highlightedMsg : text;
            return targetMsg;
        }
        return text;
    }
}