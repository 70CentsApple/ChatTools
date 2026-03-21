package net.apple70cents.chattools;

import net.apple70cents.chattools.config.ModMenuScreen;
import net.apple70cents.chattools.features.chatkeybindings.Macro;
import net.apple70cents.chattools.features.chatkeybindings.Repeat;
import net.apple70cents.chattools.features.chatkeybindings.ReviewLastMessageWithUpArrowOnly;
import net.apple70cents.chattools.utils.*;

import net.minecraft.client.Minecraft;

//#if FABRIC
//$$ import net.fabricmc.api.ModInitializer;
//$$ import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#elseif NEOFORGE
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//#endif

/**
 * @author 70CentsApple
 */
//#if NEOFORGE
@Mod("chattools")
//#endif
public class ChatTools
//#if FABRIC
//$$ implements ModInitializer
//#endif
{

//#if FABRIC
//$$ @Override
//$$ public void onInitialize() {
//$$ 	this.init();
//$$ }
//#elseif NEOFORGE
    public ChatTools() {
        this.init();
    }
//#endif


    public void init() {
        LoggerUtils.init();
        ConfigUtils.init();
        // show welcome message if needed
//#if FABRIC
//$$    ClientTickEvents.START_WORLD_TICK.register(client -> {
//#elseif NEOFORGE
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
//#endif
            if ((boolean) ConfigUtils.get("general.ShowWelcomeMessageEnabled")) {
                if (Minecraft.getInstance().player != null) {
                    MessageUtils.sendToNonPublicChat(TextUtils.trans("texts.welcomeMessage").copy().setStyle(TextUtils.WEBSITE_URL_STYLE));
                    LoggerUtils.info("[ChatTools] Shown welcome message.");
                    ConfigUtils.set("general.ShowWelcomeMessageEnabled", false);
                    ConfigUtils.save();
                }
            }
        });

        // register features
//#if FABRIC
//$$    ClientTickEvents.START_WORLD_TICK.register(client -> {
//#elseif NEOFORGE
        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Pre event) -> {
//#endif
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

//#if NEOFORGE
        ModMenuScreen.registerConfigScreen();
//#endif

        Runnable runnable = () -> {
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
        // Start the file download in a new thread
        Thread downloadThread = new Thread(runnable, "ChatTools-Download-Thread");
        downloadThread.start();
    }
}
