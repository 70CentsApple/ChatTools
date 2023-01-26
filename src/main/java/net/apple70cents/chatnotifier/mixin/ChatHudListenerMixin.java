package net.apple70cents.chatnotifier.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.apple70cents.chatnotifier.ChatNotifier;
import net.apple70cents.chatnotifier.MyToastNotification;
import net.apple70cents.chatnotifier.config.ModConfig;
import net.apple70cents.chatnotifier.config.ModConfigProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHudListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.network.MessageType;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
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

import java.awt.*;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.apple70cents.chatnotifier.ChatNotifier.config;

@Mixin(ChatHudListener.class)
public abstract class ChatHudListenerMixin {

    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
    public void onChatMessage(MessageType type, Text text, UUID senderUuid, CallbackInfo ci) {
        ModConfig config = ChatNotifier.config;
        // 匹配机制
        boolean shouldMatch = false;
        for (int i = 0; i < config.allowList.size(); i++) {
            if (Pattern.compile(config.allowList.get(i), Pattern.MULTILINE).matcher(text.getString()).find()){ // 匹配白名单正则表达式
                shouldMatch = true;
                break;
            }
        }
        if(config.matchSelfName && Pattern.compile(this.client.player.getName().getString(), Pattern.MULTILINE).matcher(text.getString()).find()){ // 应匹配名字 && 匹配名字
            shouldMatch = true;
        }
        for (int i = 0; i < config.banList.size(); i++) {
            if (Pattern.compile(config.banList.get(i), Pattern.MULTILINE).matcher(text.getString()).find() || // 匹配黑名单正则表达式
                    (config.ignoreSystemMessage && type.equals(MessageType.SYSTEM))) { // 应忽略系统消息 && 系统消息
                shouldMatch = false;
                break;
            }
        }
        /*
        boolean shouldMatch = (Pattern.compile(config.chatNotifyRegEx, Pattern.MULTILINE).matcher(text.getString()).find()
                || (config.matchSelfName && Pattern.compile(this.client.player.getName().getString(), Pattern.MULTILINE).matcher(text.getString()).find())
        ) && !(config.ignoreSystemMessage && type.equals(MessageType.SYSTEM)); // (匹配正则表达式 || (应匹配名字 && 匹配名字) ) && !(应忽略系统消息 && 系统消息)
         */
        if (!config.modEnabled) {
        } // 启用 ChatNotifier 则向下走
        else if (!shouldMatch) {
        } // 匹配 Regex 则向下走
        else if (config.ignoreSelf && (senderUuid.equals(this.client.player.getUuid()))) {
        } // 如果 忽略自己的信息且是消息自己发出的 则 什么都不做
        else {
            // 下面的是主要代码
            ChatNotifier.LOGGER.info("Found the latest chat message matches customized RegEx");
            System.setProperty("java.awt.headless", "false");

            // 弹窗提示
            if (config.toastNotify && !this.client.isWindowFocused()) {
                MyToastNotification.toast(new TranslatableText("key.chatnotifier.toast.title").getString(), text.getString());
            }

            // ActionBar 提示
            if (config.actionbarSettings.actionbarNotifyEnabled) {
                this.client.player.sendMessage(new TranslatableText("key.chatnotifier.match"), true);
            }

            // 音效提示
            if (config.soundSettings.soundNotifyEnabled) {
                this.client.player.playSound(new SoundEvent(new Identifier(config.soundSettings.chatNotifySound)), SoundCategory.PLAYERS, config.soundSettings.chatNotifyVolume, config.soundSettings.chatNotifyPitch);
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
            if (type != MessageType.CHAT) {
                this.client.inGameHud.getChatHud().addMessage(targetMsg);
            } else {
                this.client.inGameHud.getChatHud().queueMessage(targetMsg);
            }
            ci.cancel();
        }
    }
}