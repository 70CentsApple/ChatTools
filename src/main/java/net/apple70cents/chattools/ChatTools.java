package net.apple70cents.chattools;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.config.common.ModMenuIntegration;
import net.apple70cents.chattools.features.chatkeybindings.Macro;
import net.apple70cents.chattools.features.chatkeybindings.Repeat;
import net.apple70cents.chattools.features.chatkeybindings.ReviewLastMessageWithUpArrowOnly;
import net.apple70cents.chattools.utils.*;
import net.apple70cents.chattools.features.bubble.BubbleRenderer;
import net.apple70cents.chattools.features.customjoinmessage.CustomJoinMessageSender;

import net.minecraft.client.Minecraft;

//? if FABRIC {
/*import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
*///?} elif NEOFORGE {
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
//?}

/**
 * @author 70CentsApple
 */
//? if NEOFORGE {
@Mod("chattools")
//?}
public class ChatTools
//? if FABRIC {
/*implements ModInitializer
*///?}
{

//? if FABRIC {
/*@Override
public void onInitialize() {
     	this.init();
}
*///?} elif NEOFORGE {
    public ChatTools() {
        this.init();
    }
//?}


    public void init() {
        LoggerUtils.init();
        ConfigUtils.init();
        // show welcome message if needed
//? if FABRIC {

/*//? if >=26.1 {
        ClientTickEvents.START_LEVEL_TICK.register(client -> {
//?} else {
        /^ClientTickEvents.START_WORLD_TICK.register(client -> {
^///?}

*///?} elif NEOFORGE {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
//?}
            if (ConfigUtils.getBoolean("general.ShowWelcomeMessageEnabled")) {
                if (Minecraft.getInstance().player != null) {
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.welcomeMessage").copy().setStyle(TextUtils.WEBSITE_URL_STYLE));
                    LoggerUtils.info("[ChatTools] Shown welcome message.");
                    ConfigUtils.set("general.ShowWelcomeMessageEnabled", false);
                    ConfigUtils.save();
                }
            }
        });

        // register features
//? if FABRIC {

/*//? if >=26.1 {
        ClientTickEvents.START_LEVEL_TICK.register(client -> {
//?} else {
        /^ClientTickEvents.START_WORLD_TICK.register(client -> {
^///?}

*///?} elif NEOFORGE {
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
//?}
            if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
                return;
            }
            Repeat.tick();
            ReviewLastMessageWithUpArrowOnly.tick();
            if (ConfigUtils.MACRO_ENABLED) {
                Macro.tick();
            }
        });

        // register commands
        CommandRegistryUtils.register();

        // network events (join & disconnect)
//? if FABRIC {
/*        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> onJoin());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> onDisconnect());
*///?} elif NEOFORGE {
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> onJoin());
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut event) -> onDisconnect());
//?}

//? if NEOFORGE {
        ModMenuIntegration.registerConfigScreen();
//?}

//? if !LITE_VERSION {
        Runnable checkAddonsRunnable = () -> {
            if (DownloadUtils.shouldCheckIfFullyReady()) {
                if (!DownloadUtils.checkIfFullyReady()) {
                    DownloadUtils.startDownload();
                    LoggerUtils.info("[ChatTools] Not yet fully ready, downloading...");
                }
                LoggerUtils.info("[ChatTools] Initial download thread terminated.");
            } else {
                LoggerUtils.info("[ChatTools] No need to check addons readiness.");
            }
        };
        TaskScheduler.schedule(checkAddonsRunnable, 0);
//?}
    }

    public static void onJoin() {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return;
        }
        if (ConfigUtils.getBoolean("customJoinMessage.Enabled")) {
            CustomJoinMessageSender.work(ContextUtils.getSessionIdentifier());
        }
    }

    public static void onDisconnect() {
        TaskScheduler.cancelAll();
        BubbleRenderer.clear();
    }
}
