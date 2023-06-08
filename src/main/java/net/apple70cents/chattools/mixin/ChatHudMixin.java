package net.apple70cents.chattools.mixin;

import net.apple70cents.chattools.ChatTools;
import net.apple70cents.chattools.MyToastNotification;
import net.apple70cents.chattools.config.ModClothConfig;
import net.apple70cents.chattools.features.chatbubbles.BubbleRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import static net.apple70cents.chattools.ChatTools.config;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @ModifyArgs(method = "addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;Lnet/minecraft/client/gui/hud/MessageIndicator;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageSignatureData;ILnet/minecraft/client/gui/hud/MessageIndicator;Z)V"))
    public void dealWithMessage(Args args) {
        Text message = args.get(0);
        MessageSignatureData signature = args.get(1);
        MessageIndicator indicator = args.get(3);
        if (config.chatBubblesEnabled) {
            BubbleRenderer.addChatBubble(message);
        }
        message = highlightAndNotify(message, indicator);
        // 显示聊天时间
        if (config.displayChatTimeEnabled) {
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
        args.set(0, message);
    }

    /**
     * 给文字加样式
     *
     * @param text         被处理的文字
     * @param styleUpdater 样式（lambda表达式）
     * @return 处理后的文字
     */
    private MutableText stylish(MutableText text, UnaryOperator<Style> styleUpdater) {
        text.setStyle((Style) styleUpdater.apply(text.getStyle()));
        return text;
    }

    /**
     * **尝试**高亮消息并做对应的消息提醒功能
     *
     * @param text 消息
     * @return 经过高亮处理后的消息
     */
    private Text highlightAndNotify(Text text, MessageIndicator indicator) {
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
        if (!config.modEnabled) {
        } // 启用 ChatTools 则向下走
        else if (!shouldMatch) {
        } // 匹配 Regex 则向下走
        else if (config.ignoreSelf && (this.client.player.getName().getString().equals(BubbleRenderer.findPlayerName(MinecraftClient.getInstance().world.getPlayers(), text.getString())))) {
            // 这里不再是UUID审查了
        } // 如果 忽略自己的信息且是消息自己发出的 则 什么都不做
        else {
            // 下面的是主要代码
            ChatTools.LOGGER.info("[ChatTools] Found the latest chat message matches customized RegEx");
            System.setProperty("java.awt.headless", "false");

            // 弹窗提示
            if (config.toastNotify && !this.client.isWindowFocused()) {
                MyToastNotification.toast(Text.translatable("key.chattools.toast.title").getString(), text.getString());
            }

            // ActionBar 提示
            if (config.actionbarSettings.actionbarNotifyEnabled) {
                this.client.player.sendMessage(Text.translatable("key.chattools.match"), true);
            }

            // 音效提示
            if (config.soundSettings.soundNotifyEnabled) {
                this.client.player.playSound(SoundEvent.of(new Identifier(config.soundSettings.chatNotifySound)), SoundCategory.PLAYERS, config.soundSettings.chatNotifyVolume * 0.01F, config.soundSettings.chatNotifyPitch * 0.1F);
            }

            // 高亮提示
            String prefix = config.highlightSettings.highlightPrefix.replace('&', '§').replace("\\§", "&");
            Text highlightedMsg;
            if (config.highlightSettings.enforceOverwriting) {
                highlightedMsg = (Text) Text.translatable(prefix + text.getString());
            } else {
                highlightedMsg = (Text) Text.translatable(prefix).append(text);
            }
            Text targetMsg = config.highlightSettings.highlightEnabled ? highlightedMsg : text;
            return targetMsg;
        }
        return text;
    }
}