package net.apple70cents.chattools.config;

import com.mojang.blaze3d.systems.RenderSystem;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigManager;
import me.shedaniel.autoconfig.gui.ConfigScreenProvider;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.ComposedGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.DefaultGuiRegistryAccess;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.autoconfig.gui.registry.api.GuiRegistryAccess;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.apple70cents.chattools.ChatTools;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;

import java.lang.annotation.Annotation;

@Environment(EnvType.CLIENT)
public class ModMenuScreen implements ModMenuApi {
//    private static final GuiRegistry defaultGuiRegistry =
//            DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            if(!ChatTools.CONFIGS_LOADED)return null;

            return ModClothConfig.getConfigBuilder().setParentScreen(parent).build();
//            return new ConfigScreenProvider<>(
//                    (ConfigManager<ModConfig>) AutoConfig.getConfigHolder(ModConfig.class),
//                    getGuiRegistryAccess(), parent).get();
        };
    }

//    public static GuiRegistryAccess getGuiRegistryAccess() {
//        return new ComposedGuiRegistryAccess(defaultGuiRegistry,
//                AutoConfig.getGuiRegistry(ModConfig.class), new DefaultGuiRegistryAccess());
//    }
}