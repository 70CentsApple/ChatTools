package net.apple70cents.chatnotifier;

import net.apple70cents.chatnotifier.config.ConfigOptions;
import net.apple70cents.chatnotifier.config.ConfigScreenFactory;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.kyrptonaught.kyrptconfig.config.ConfigManager;
import net.kyrptonaught.kyrptconfig.keybinding.CustomKeyBinding;
import net.kyrptonaught.kyrptconfig.keybinding.DisplayOnlyKeyBind;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.MultilineText;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ChatNotifier implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MODID = "chatnotifier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);
    public static ConfigManager.SingleConfigManager config = new ConfigManager.SingleConfigManager(MODID,new ConfigOptions());
    public static List<ConfigOptions.RegexUnit> allowList = new ArrayList<>();
    public static List<ConfigOptions.RegexUnit> banList = new ArrayList<>();
    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        config.load();

        // 检查打开 mod 配置页面的热键是否被按下
        ClientTickEvents.START_WORLD_TICK.register(clientWorld -> {
            if (MinecraftClient.getInstance().currentScreen == null) {
                if (getConfig().openConfigScreenKeybind.isKeybindPressed()) {
                    MinecraftClient.getInstance().setScreen(ConfigScreenFactory.buildScreen(null));
                    return;
                }
            }
        });

        KeyBindingHelper.registerKeyBinding(new DisplayOnlyKeyBind(
                "key.chatnotifier.configScreen",
                "key.chatnotifier.modname",
                getConfig().openConfigScreenKeybind,
                setKey -> ChatNotifier.config.save()
        ));
    }

    public static ConfigOptions getConfig() {
        return (ConfigOptions) config.getConfig();
    }

    public static void addRegexUnit(String type){
        if(type.equals("ALLOW")) {
            getConfig().allowList.add(new ConfigOptions.RegexUnit());
        } else if(type.equals("BAN")){
            getConfig().banList.add(new ConfigOptions.RegexUnit());
        }
        config.save();
    }
}
