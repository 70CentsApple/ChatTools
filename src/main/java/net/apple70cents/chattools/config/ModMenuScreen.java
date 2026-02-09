package net.apple70cents.chattools.config;

//#if FABRIC
//$$ import com.terraformersmc.modmenu.api.ConfigScreenFactory;
//$$ import com.terraformersmc.modmenu.api.ModMenuApi;
//$$ import net.fabricmc.api.EnvType;
//$$ import net.fabricmc.api.Environment;
//#elseif NEOFORGE
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
//#endif

//#if FABRIC
//$$ @Environment(EnvType.CLIENT)
//#endif
public class ModMenuScreen
		//#if FABRIC
		//$$ implements ModMenuApi
		//#endif
{
	//#if FABRIC
	//$$ @Override
	//$$ public ConfigScreenFactory<?> getModConfigScreenFactory() {
	//$$     return parent -> ConfigScreenGenerator.getConfigBuilder().setParentScreen(parent).build();
	//$$ }
	//#elseif NEOFORGE
	public static void registerConfigScreen() {
		ModLoadingContext.get()
				.registerExtensionPoint(
						IConfigScreenFactory.class,
						() -> (container, parent) -> ConfigScreenGenerator.getConfigBuilder().setParentScreen(parent).build()
				);
	}
	//#endif
}