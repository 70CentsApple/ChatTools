package net.apple70cents.chattools.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.apple70cents.chattools.features.general.ChatAnimationProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}

//? if >=26.1 {
import net.apple70cents.chattools.config.common.ConfigUtils;
import org.spongepowered.asm.mixin.injection.Coerce;
import com.llamalad7.mixinextras.sugar.Local;
//?} elif >=1.21.6 {
/*import net.apple70cents.chattools.config.common.ConfigUtils;
import org.spongepowered.asm.mixin.injection.Coerce;
import com.llamalad7.mixinextras.sugar.Local;
*///?} elif >=1.20 {
/*import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
*///?} elif >=1.19 {
/*import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
*///?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.util.FormattedCharSequence;
import net.apple70cents.chattools.features.general.ChatAnimator;
*///?}

/**
 * Render-side per-line slide-in / push-up. The "which lines are new"
 * bookkeeping lives in {@link ChatComponentMixin}; we delegate the
 * actual offset math to {@link ChatAnimationProxy}.
 *
 * <p>All Minecraft-class access goes through {@code @Shadow} or direct typed
 * calls in this mixin so it survives Fabric/Forge production remapping —
 * {@link ChatAnimationProxy} itself contains no reflection on Minecraft type
 * names. The mixin hands the proxy a {@code List<?>} and small lambdas it
 * built from {@code @Shadow}-ed fields.
 *
 * <p>Selected by stonecutter:
 * <ul>
 *   <li><b>&gt;= 26.1</b>: {@code extractRenderState(ChatGraphicsAccess,…)} +
 *       {@code forEachLine(AlphaCalculator, LineConsumer)}, where {@code accept}
 *       takes {@code (Line, lineIndex, alpha)}. The consumer is wrapped in a
 *       reflective Proxy that sandwiches each {@code accept} with a
 *       {@code graphics.updatePose} translate.</li>
 *   <li><b>1.21.11</b>: same shape as 26.1, only the outer method is named
 *       {@code render(ChatGraphicsAccess,…)} (renamed in 26.1).</li>
 *   <li><b>1.21.6 – 1.21.10</b>: {@code render(GuiGraphics,…)} with a
 *       {@code forEachLine(int,int,boolean,int,LineConsumer)} whose
 *       {@code accept} takes {@code (int x, int startY, int endY, Line, idx, fade)}.
 *       The decorator mutates those three int coords directly — both background
 *       and text inside the lambda read from them, so they slide together.</li>
 *   <li><b>1.20.5 – 1.21.5</b>: inline for-loop. Hook the per-line
 *       {@code GuiGraphics.fill} (bg ord 0, tag bar ord 1) and
 *       {@code GuiGraphics.drawString} via {@code @WrapOperation};
 *       {@code @Local} picks up the loop's {@code GuiMessage.Line} variable.</li>
 *   <li><b>1.20.1 – 1.20.4</b>: same draws as 1.20.5+, just a 4-arg {@code render}
 *       (no {@code boolean focused}).</li>
 *   <li><b>1.19.4</b>: {@code render(PoseStack,…)} — per-line bg/tag fills route
 *       through the static {@code GuiComponent.fill(PoseStack,…)} and text via
 *       {@code Font.drawShadow(PoseStack,…)}.</li>
 *   <li><b>1.16.5 / 1.17.1 / 1.18.2</b>: 2-arg {@code render(PoseStack,int)}.
 *       {@code trimmedMessages} holds {@code GuiMessage<FormattedCharSequence>} directly,
 *       and {@code @Local} on the loop variable proved too brittle on this older LVT,
 *       so we hint-capture the per-iteration line on the {@code List.get(int)} call and
 *       read it back from {@link ChatAnimator#currentRenderLine()} inside the fill /
 *       drawShadow wraps.</li>
 * </ul>
 *
 * @author 70CentsApple
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixinForAnimation {

    @Shadow
    @Final
    private List<
//? if >=1.19 {
            GuiMessage.Line
//?} else {
            /*GuiMessage<net.minecraft.util.FormattedCharSequence>
*///?}
            > trimmedMessages;

    /**
     * Per-line vertical step (one chat row), in chat-space pixels. Read via a
     * typed call so the {@code chatLineSpacing} reference goes through
     * mappings — {@code Options.chatLineSpacing} switched from a primitive
     * field (≤1.17) to an {@code OptionInstance} method (1.18+).
     */
    @Unique
    private double chatTools$entryHeight() {
        double spacing =
//? if >=1.19 {
                Minecraft.getInstance().options.chatLineSpacing().get();
//?} else {
                /*Minecraft.getInstance().options.chatLineSpacing;
*///?}
        return 9.0 * (spacing + 1.0);
    }

    /**
     * Ask the animator for {@code line}'s current offset and round to pixels.
     * Returns {@code null} when this line has no offset to apply (already at rest
     * or animation disabled) so callers can short-circuit straight to the
     * untouched original call.
     */
    @Unique
    private int[] chatTools$intDelta(Object line) {
        float[] off = ChatAnimationProxy.offsetFor(line, this.trimmedMessages, chatTools$entryHeight());
        if (off == null) return null;
        return new int[] { Math.round(off[0]), Math.round(off[1]) };
    }

//? if >=26.1 {
    @WrapOperation(
            method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_modern(
            ChatComponent self,
            @Coerce Object alphaCalculator,
            @Coerce Object lineConsumer,
            Operation<Integer> original,
            @Local(argsOnly = true) ChatComponent.ChatGraphicsAccess graphics) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, alphaCalculator, lineConsumer);
        }
        final double eh = chatTools$entryHeight();
        Function<Object, float[]> offsetQuery = line -> ChatAnimationProxy.offsetFor(line, this.trimmedMessages, eh);
        BiConsumer<Float, Float> poseTranslate = (dx, dy) -> graphics.updatePose(p -> p.translate(dx, dy));
        return original.call(self, alphaCalculator,
                ChatAnimationProxy.decorate(lineConsumer, offsetQuery, poseTranslate));
    }
//?} elif >=1.21.11 {
    /*// 1.21.11: same LineConsumer shape as 26.1+, only the outer is render(...).
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_1_21_11(
            ChatComponent self,
            @Coerce Object alphaCalculator,
            @Coerce Object lineConsumer,
            Operation<Integer> original,
            @Local(argsOnly = true) ChatComponent.ChatGraphicsAccess graphics) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, alphaCalculator, lineConsumer);
        }
        final double eh = chatTools$entryHeight();
        Function<Object, float[]> offsetQuery = line -> ChatAnimationProxy.offsetFor(line, this.trimmedMessages, eh);
        BiConsumer<Float, Float> poseTranslate = (dx, dy) -> graphics.updatePose(p -> p.translate(dx, dy));
        return original.call(self, alphaCalculator,
                ChatAnimationProxy.decorate(lineConsumer, offsetQuery, poseTranslate));
    }
*///?} elif >=1.21.6 {
    /*// 1.21.6 – 1.21.10: 6-arg LineConsumer.accept(x, startY, endY, Line, idx, fade).
    // The decorator mutates the int coords in-place — both background and text inside
    // the lambda read from them, so they slide together for free. No pose translate
    // needed for this flavour.
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(IIZILnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_six(
            ChatComponent self, int linesPerPage, int tickCount, boolean focused, int bottomY,
            @Coerce Object lineConsumer,
            Operation<Integer> original) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, linesPerPage, tickCount, focused, bottomY, lineConsumer);
        }
        final double eh = chatTools$entryHeight();
        Function<Object, float[]> offsetQuery = line -> ChatAnimationProxy.offsetFor(line, this.trimmedMessages, eh);
        return original.call(self, linesPerPage, tickCount, focused, bottomY,
                ChatAnimationProxy.decorate(lineConsumer, offsetQuery, null));
    }
*///?} elif >=1.20.5 {
    /*// 1.20.5 – 1.21.5: render() is an inline for-loop with no LineConsumer to wrap.
    // Hook the per-line draw calls directly — fill (bg ordinal 0, tag bar ordinal 1)
    // and drawString (ordinal 0). @Local picks up the loop's GuiMessage.Line so we
    // can ask the animator for its current offset.
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill(
            GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        original.call(gg, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1),
            require = 0
    )
    private void chatTools$wrapTagBarFill(
            GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        original.call(gg, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawString(
            GuiGraphics gg, Font font, FormattedCharSequence text, int x, int y, int color,
            Operation<Integer> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) return original.call(gg, font, text, x, y, color);
        return original.call(gg, font, text, x + d[0], y + d[1], color);
    }
*///?} elif >=1.20 {
    /*// 1.20.1 – 1.20.4: same GuiGraphics-based draws as 1.20.5+, only render() lost
    // its boolean focused parameter (4-arg signature).
    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_4arg(
            GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        original.call(gg, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1),
            require = 0
    )
    private void chatTools$wrapTagBarFill_4arg(
            GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        original.call(gg, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawString_4arg(
            GuiGraphics gg, Font font, FormattedCharSequence text, int x, int y, int color,
            Operation<Integer> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) return original.call(gg, font, text, x, y, color);
        return original.call(gg, font, text, x + d[0], y + d[1], color);
    }
*///?} elif >=1.19 {
    /*// 1.19.x: render uses PoseStack instead of GuiGraphics; per-line bg/tag fills go
    // through the static GuiComponent.fill(PoseStack,…), and text via
    // Font.drawShadow(PoseStack,…).
    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_pose(
            PoseStack pose, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        original.call(pose, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 1),
            require = 0
    )
    private void chatTools$wrapTagBarFill_pose(
            PoseStack pose, int x0, int y0, int x1, int y1, int color,
            Operation<Void> original, @Local GuiMessage.Line line) {
        int[] d = chatTools$intDelta(line);
        if (d == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        original.call(pose, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawShadow(
            Font font, PoseStack pose, FormattedCharSequence text, float x, float y, int color,
            Operation<Integer> original, @Local GuiMessage.Line line) {
        float[] off = ChatAnimationProxy.offsetFor(line, this.trimmedMessages, chatTools$entryHeight());
        if (off == null) return original.call(font, pose, text, x, y, color);
        return original.call(font, pose, text, x + off[0], y + off[1], color);
    }
*///?} else {
    /*// 1.16.5 / 1.17.1 / 1.18.2: 2-arg render(PoseStack, int). trimmedMessages holds
    // GuiMessage<FormattedCharSequence> directly. We capture the per-iteration line
    // via a wrap on trimmedMessages.get(int) into ChatAnimator.setCurrentRenderLine,
    // then the fill / drawShadow wraps below read it back. (@Local on the loop
    // variable was too brittle on this older LVT shape.)
    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0),
            require = 0
    )
    private Object chatTools$captureLine(java.util.List<?> list, int index, Operation<Object> original) {
        Object line = original.call(list, index);
        ChatAnimator.setCurrentRenderLine(line);
        return line;
    }

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_legacy(
            PoseStack pose, int x0, int y0, int x1, int y1, int color, Operation<Void> original) {
        int[] d = chatTools$intDelta(ChatAnimator.currentRenderLine());
        if (d == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        original.call(pose, x0 + d[0], y0 + d[1], x1 + d[0], y1 + d[1], color);
    }

    @WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawShadow_legacy(
            Font font, PoseStack pose, FormattedCharSequence text, float x, float y, int color,
            Operation<Integer> original) {
        float[] off = ChatAnimationProxy.offsetFor(ChatAnimator.currentRenderLine(), this.trimmedMessages, chatTools$entryHeight());
        if (off == null) return original.call(font, pose, text, x, y, color);
        return original.call(font, pose, text, x + off[0], y + off[1], color);
    }
*///?}
}
