package net.apple70cents.chattools.utils;

import net.minecraft.network.chat.Component;

/**
 * Duck interface implemented by {@code ChatComponent} (via
 * {@code net.apple70cents.chattools.mixins.ChatComponentMixin}) so other code can ask which chat
 * message the mouse is currently pointing at.
 * <p>
 * This lives outside the {@code mixins} package on purpose: classes in a Mixin-owned package cannot
 * be referenced directly by ordinary code, and this interface must be cast to at call sites.
 *
 * @author 70CentsApple
 */
public interface ChatComponentDuck {
    /**
     * Resolves the chat message currently under the mouse cursor.
     *
     * @return the displayed component of the hovered message line, or null when the cursor is not
     * over any chat line (or the running version has no implementation).
     */
    Component chatTools$hoveredMessageAtCursor();
}
