package net.apple70cents.chatnotifier.config;

import com.terraformersmc.modmenu.util.mod.Mod;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.apple70cents.chatnotifier.ChatNotifier;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

import java.util.ArrayList;
import java.util.List;

@Config.Gui.Background("minecraft:textures/block/pink_concrete_powder.png")
@Config(name = "chatnotifier")
public class ModConfig implements ConfigData {
    public static void init(){
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        GuiRegistry registry = AutoConfig.getGuiRegistry(ModConfig.class);
    }
    @ConfigEntry.Gui.Tooltip
    public boolean modEnabled = true;

    @ConfigEntry.Gui.Excluded
    public boolean shouldShowWelcomeMessage = true;

    public static class SoundSettings{
        public boolean soundNotifyEnabled = true;
        public String chatNotifySound = "block.note_block.bit";
        public Float chatNotifyVolume = 1.0F;
        public Float chatNotifyPitch = 1.0F;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public SoundSettings soundSettings = new SoundSettings();

    public static class ActionbarSettings{
        public boolean actionbarNotifyEnabled = true;
    }
    @ConfigEntry.Gui.CollapsibleObject
    public ActionbarSettings actionbarSettings = new ActionbarSettings();

    public static class HighlightSettings{
        @ConfigEntry.Gui.Tooltip
        public boolean highlightEnabled = true;

        @ConfigEntry.Gui.Tooltip
        public String highlightPrefix = "&a→ &6";

        @ConfigEntry.Gui.Tooltip
        public boolean enforceOverwriting = false;
    }
    @ConfigEntry.Gui.CollapsibleObject
    @ConfigEntry.Gui.Tooltip
    public HighlightSettings highlightSettings = new HighlightSettings();

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreSelf = true;

    @ConfigEntry.Gui.Tooltip
    public boolean matchSelfName = true;

    @ConfigEntry.Gui.Tooltip
    public boolean ignoreSystemMessage = true;

    @ConfigEntry.Gui.Tooltip
    public boolean toastNotify = false;

    @ConfigEntry.Gui.Tooltip
    public List<String> allowList = new ArrayList<>(); // 十分肤色正确的变量名

    @ConfigEntry.Gui.Tooltip
    public List<String> banList = new ArrayList<>(); // 十分肤色正确的变量名
}