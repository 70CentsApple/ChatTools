package net.apple70cents.chattools.config;

//? if FABRIC {
/*import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
*///?} elif NEOFORGE {

import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
//?}

//? if FABRIC {
/*@Environment(EnvType.CLIENT)
*///?}
public class ModMenuScreen
//? if FABRIC {
/*implements ModMenuApi
*///?}
{
//? if FABRIC {
/*@Override
public ConfigScreenFactory<?> getModConfigScreenFactory() {
         return parent -> net.apple70cents.chattools.config.ConfigScreenFactory.createScreen(parent);
}
*///?} elif NEOFORGE {
    public static void registerConfigScreen() {
        ModLoadingContext.get()
                .registerExtensionPoint(
                        IConfigScreenFactory.class,
                        () -> (container, parent) -> net.apple70cents.chattools.config.ConfigScreenFactory.createScreen(parent)
                );
    }
//?}
}