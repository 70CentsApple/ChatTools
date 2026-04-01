package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.minecraft.client.gui.Font;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

//? if >=1.21.9 {
@Mixin(Font.class)
//?} else {
/*@Mixin(net.minecraft.client.gui.font.FontSet.class)
*///?}
public class FontMixin {

    private static final String LETTERS_NUMS = "KFCV50";
    private static final String CHINESE = "肯德基疯狂星期四我";

    @ModifyVariable(method = "getGlyph", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private int injectCrazyThursday(int originalCodepoint) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED) {
            return originalCodepoint;
        }
        if (!ConfigUtils.KFC_ENABLED) {
            return originalCodepoint;
        }

        try {
            // if already a kfc character, return it unchanged
            if (LETTERS_NUMS.indexOf(originalCodepoint) != -1 || CHINESE.indexOf(originalCodepoint) != -1) {
                return originalCodepoint;
            }

            // is a letter or number
            if ((originalCodepoint >= '0' && originalCodepoint <= '9') ||
                    (originalCodepoint >= 'a' && originalCodepoint <= 'z') ||
                    (originalCodepoint >= 'A' && originalCodepoint <= 'Z')) {
                int hash = originalCodepoint * 31 ^ 0xA991E;
                int index = Math.abs(hash) % LETTERS_NUMS.length();

                return LETTERS_NUMS.codePointAt(index);
            }

            // is a Chinese character (CJK Unified Ideographs block)
            if (originalCodepoint >= 0x4E00 && originalCodepoint <= 0x9FA5) {

                int hash = originalCodepoint * 31 ^ 0x70CA;
                int index = Math.abs(hash) % CHINESE.length();

                return CHINESE.codePointAt(index);
            }

            // spaces, punctuation, and other characters remain unchanged
            return originalCodepoint;
        } catch (Exception e) {
            // In case of any unexpected error, return the original codepoint
            return originalCodepoint;
        }
    }
}
