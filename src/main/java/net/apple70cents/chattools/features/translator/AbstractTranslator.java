package net.apple70cents.chattools.features.translator;

import net.apple70cents.chattools.utils.LoggerUtils;
import net.apple70cents.chattools.utils.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;

public abstract class AbstractTranslator {
    protected final EditBox editBox;

    protected AbstractTranslator(EditBox editBox) {
        this.editBox = editBox;
    }

    public void work() {
        String text = editBox.getValue();
        editBox.setValue(TextUtils.trans("texts.translator.await").getString());
        new Thread(() -> {
            Minecraft mc = Minecraft.getInstance();
            try {
                LoggerUtils.info("[ChatTools] Start translating: " + text);
                String result = translate(text);
                mc.execute(() -> editBox.setValue(result));
                LoggerUtils.info("[ChatTools] Translation Result: " + result);
            } catch (Exception e) {
                LoggerUtils.error("[ChatTools] Error occurred when translating: " + text);
                e.printStackTrace();
                mc.execute(() -> editBox.setValue(TextUtils.trans("texts.translator.error", e).getString()));
            }
        }, "Chat-Tools-Translation").start();
    }

    protected abstract String translate(String text) throws Exception;
}
