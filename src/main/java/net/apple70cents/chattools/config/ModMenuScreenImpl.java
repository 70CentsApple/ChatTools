package net.apple70cents.chattools.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;

@Environment(EnvType.CLIENT)
public class ModMenuScreenImpl implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModMenuScreenImpl::getConfigScreen;
    }

    public static Screen getConfigScreen(Screen parent) {
        boolean clothConfigInstalled = FabricLoader.getInstance().isModLoaded("cloth-config") ||
                FabricLoader.getInstance().isModLoaded("cloth-config2");
        boolean yaclInstalled = FabricLoader.getInstance().isModLoaded("yet_another_config_lib_v3");
        if (clothConfigInstalled && !yaclInstalled) {
            return ClothConfigScreenGenerator.getConfigBuilder().setParentScreen(parent).build();
        }
        if (yaclInstalled) {
            if (Screen.hasShiftDown() && clothConfigInstalled){
                return ClothConfigScreenGenerator.getConfigBuilder().setParentScreen(parent).build();
            } else {
                return YaclScreenGenerator.getBuilder().build().generateScreen(parent);
            }
        }
        // TODO 引导界面
        return null;
    }
}