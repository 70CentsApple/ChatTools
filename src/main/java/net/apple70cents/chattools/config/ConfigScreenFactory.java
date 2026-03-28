package net.apple70cents.chattools.config;

import net.apple70cents.chattools.utils.KeyboardUtils;
import net.minecraft.client.gui.screens.Screen;

/**
 * Factory that determines which config library to use at runtime.
 * Priority: Cloth Config > YACL > MissingConfigLibScreen
 *
 * @author 70CentsApple
 */
public class ConfigScreenFactory {
    private static Boolean yaclLoaded = null;
    private static Boolean clothConfigLoaded = null;

    public static boolean isYACLLoaded() {
        if (yaclLoaded == null) {
            try {
                Class.forName("dev.isxander.yacl3.api.YetAnotherConfigLib");
                yaclLoaded = true;
            } catch (ClassNotFoundException e) {
                yaclLoaded = false;
            }
        }
        return yaclLoaded;
    }

    public static boolean isClothConfigLoaded() {
        if (clothConfigLoaded == null) {
            try {
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                clothConfigLoaded = true;
            } catch (ClassNotFoundException e) {
                clothConfigLoaded = false;
            }
        }
        return clothConfigLoaded;
    }

    public static Screen createScreen(Screen parent) {
        boolean shiftPressed = KeyboardUtils.isKeyPressingWithModifier("key.keyboard.left.shift",
                SpecialUnits.KeyModifiers.NONE, SpecialUnits.MacroModes.GREEDY);
        try {
            // has cloth, and not holding shift
            if (isClothConfigLoaded() && !(shiftPressed && isYACLLoaded())) {
                return ClothProvider.getScreen(parent);
            }
//? if HAS_YACL {
            // has yacl
            if (isYACLLoaded()) {
                return YACLProvider.getScreen(parent);
            }
//?}
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return new MissingConfigLibScreen(parent);
    }

    private static class ClothProvider {
        static Screen getScreen(Screen parent) {
            return ClothConfigScreenGenerator.getConfigBuilder().setParentScreen(parent).build();
        }
    }

//? if HAS_YACL {
    private static class YACLProvider {
        static Screen getScreen(Screen parent) {
            return YACLConfigScreenGenerator.createScreen(parent);
        }
    }
//?}
}