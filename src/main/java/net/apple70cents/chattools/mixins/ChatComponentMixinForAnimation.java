package net.apple70cents.chattools.mixins;

import net.apple70cents.chattools.config.common.ConfigUtils;
import net.apple70cents.chattools.features.general.ChatAnimator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

//? if >=26.1 {
import net.minecraft.client.multiplayer.chat.GuiMessage;
//?} else {
/*import net.minecraft.client.GuiMessage;
*///?}

/**
 * Render-side per-line slide-in / push-up. The "which lines are new"
 * bookkeeping lives in {@link ChatComponentMixin}; we delegate the
 * actual offset math to {@link net.apple70cents.chattools.features.general.ChatAnimationProxy}.
 *
 * <p>Three render-shape eras are handled per-line, selected by stonecutter:
 * <ul>
 *   <li><b>&gt;= 26.1</b>: {@code extractRenderState(ChatGraphicsAccess,…)} with
 *       {@code forEachLine(AlphaCalculator, LineConsumer)} where {@code accept}
 *       takes {@code (Line, lineIndex, alpha)}. We wrap the consumer in a
 *       reflective Proxy that sandwiches each accept with a {@code graphics.updatePose}
 *       translate.</li>
 *   <li><b>1.21.11</b>: same Proxy approach but the outer method is named
 *       {@code render(ChatGraphicsAccess,…)} (renamed in 26.1).</li>
 *   <li><b>1.21.6 – 1.21.10</b>: {@code render(GuiGraphics,…)} with a
 *       {@code forEachLine(int, int, boolean, int, LineConsumer)} whose
 *       {@code accept} takes {@code (int x, int startY, int endY, Line, idx, fade)}.
 *       We wrap the consumer and mutate those three int coords directly — both the
 *       background fill and the text draw inside the lambda are positioned from
 *       them, so they slide together for free.</li>
 *   <li><b>1.20.5 – 1.21.5</b>: render() is a plain for-loop with no LineConsumer
 *       to wrap. We currently fall back to a single chat-wide ease-in (whole HUD
 *       slides as one). Per-line on those versions would need @WrapOperation on
 *       {@code drawString} / {@code fill} with @Local line capture — left as a
 *       follow-up.</li>
 *   <li><b>&lt; 1.20.5</b>: line tracking unavailable, no-op stub mixin.</li>
 * </ul>
 *
 * @author 70CentsApple
 */
@Mixin(ChatComponent.class)
public abstract class ChatComponentMixinForAnimation {

    @org.spongepowered.asm.mixin.Shadow
    @org.spongepowered.asm.mixin.Final
    private List<
//? if >=1.19 {
            GuiMessage.Line
//?} else {
            /*GuiMessage<net.minecraft.util.FormattedCharSequence>
*///?}
            > trimmedMessages;

//? if >=26.1 {
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_modern(
            ChatComponent self,
            @org.spongepowered.asm.mixin.injection.Coerce Object alphaCalculator,
            @org.spongepowered.asm.mixin.injection.Coerce Object lineConsumer,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ChatComponent.ChatGraphicsAccess graphics) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, alphaCalculator, lineConsumer);
        }
        Object decorated = net.apple70cents.chattools.features.general.ChatAnimationProxy.decorate(lineConsumer, graphics);
        return original.call(self, alphaCalculator, decorated);
    }
//?} elif >=1.21.11 {
    /*// 1.21.11: same LineConsumer shape as 26.1+ but the outer is render(...).
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(Lnet/minecraft/client/gui/components/ChatComponent$AlphaCalculator;Lnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_1_21_11(
            ChatComponent self,
            @org.spongepowered.asm.mixin.injection.Coerce Object alphaCalculator,
            @org.spongepowered.asm.mixin.injection.Coerce Object lineConsumer,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) ChatComponent.ChatGraphicsAccess graphics) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, alphaCalculator, lineConsumer);
        }
        Object decorated = net.apple70cents.chattools.features.general.ChatAnimationProxy.decorate(lineConsumer, graphics);
        return original.call(self, alphaCalculator, decorated);
    }
*///?} elif >=1.21.6 {
    /*// 1.21.6 – 1.21.10: 6-arg LineConsumer.accept(x, startY, endY, Line, idx, fade).
    // Wrap and shift the int coords in-place — both background and text draw use them.
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent;forEachLine(IIZILnet/minecraft/client/gui/components/ChatComponent$LineConsumer;)I"
            )
    )
    private int chatTools$wrapForEachLine_six(
            ChatComponent self, int linesPerPage, int tickCount, boolean focused, int bottomY,
            @org.spongepowered.asm.mixin.injection.Coerce Object lineConsumer,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original) {
        if (!ConfigUtils.CHAT_TOOLS_ENABLED || !ConfigUtils.CHAT_ANIMATION_ENABLED) {
            return original.call(self, linesPerPage, tickCount, focused, bottomY, lineConsumer);
        }
        Object decorated = net.apple70cents.chattools.features.general.ChatAnimationProxy.decorate(lineConsumer, null);
        return original.call(self, linesPerPage, tickCount, focused, bottomY, decorated);
    }
*///?} elif >=1.20.5 {
    /*// 1.20.5 – 1.21.5: render() is an inline for-loop with no LineConsumer to wrap.
    // Hook the per-line draw calls directly — guiGraphics.fill (background, ordinal 0;
    // tag indicator bar, ordinal 1) and guiGraphics.drawString (text, ordinal 0).
    // @Local picks up the loop's `line` variable so we can ask the animator for its
    // current offset.
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 0
            ),
            require = 0
    )
    private void chatTools$wrapBgFill(
            net.minecraft.client.gui.GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]);
        int dy = Math.round(off[1]);
        original.call(gg, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V",
                    ordinal = 1
            ),
            require = 0
    )
    private void chatTools$wrapTagBarFill(
            net.minecraft.client.gui.GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]);
        int dy = Math.round(off[1]);
        original.call(gg, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIIZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I",
                    ordinal = 0
            ),
            require = 0
    )
    private int chatTools$wrapDrawString(
            net.minecraft.client.gui.GuiGraphics gg, net.minecraft.client.gui.Font font,
            net.minecraft.util.FormattedCharSequence text, int x, int y, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) return original.call(gg, font, text, x, y, color);
        int dx = Math.round(off[0]);
        int dy = Math.round(off[1]);
        return original.call(gg, font, text, x + dx, y + dy, color);
    }
*///?} elif >=1.20 {
    /*// 1.20.1 – 1.20.4: same GuiGraphics-based draws as 1.20.5+, only render() lost its
    // boolean focused parameter (4-arg signature).
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_4arg(
            net.minecraft.client.gui.GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(gg, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;fill(IIIII)V", ordinal = 1),
            require = 0
    )
    private void chatTools$wrapTagBarFill_4arg(
            net.minecraft.client.gui.GuiGraphics gg, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(gg, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(gg, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lnet/minecraft/client/gui/GuiGraphics;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Lnet/minecraft/util/FormattedCharSequence;III)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawString_4arg(
            net.minecraft.client.gui.GuiGraphics gg, net.minecraft.client.gui.Font font,
            net.minecraft.util.FormattedCharSequence text, int x, int y, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) return original.call(gg, font, text, x, y, color);
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        return original.call(gg, font, text, x + dx, y + dy, color);
    }
*///?} elif >=1.19 {
    /*// 1.19.x: render uses PoseStack instead of GuiGraphics; per-line bg/tag fills go
    // through the static GuiComponent.fill(PoseStack,...), and text via Font.drawShadow(PoseStack,...).
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0),
            require = 0
    )
    private static void chatTools$wrapBgFill_pose(
            com.mojang.blaze3d.vertex.PoseStack pose, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(pose, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 1),
            require = 0
    )
    private static void chatTools$wrapTagBarFill_pose(
            com.mojang.blaze3d.vertex.PoseStack pose, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(pose, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;III)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawShadow(
            net.minecraft.client.gui.Font font, com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.util.FormattedCharSequence text, float x, float y, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original,
            @com.llamalad7.mixinextras.sugar.Local GuiMessage.Line line) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(line);
        if (off == null) return original.call(font, pose, text, x, y, color);
        return original.call(font, pose, text, x + off[0], y + off[1], color);
    }
*///?} elif >=1.17 {
    /*// 1.17.1 / 1.18.2: render(PoseStack, int tickCount) — 2 args. trimmedMessages holds
    // GuiMessage<FormattedCharSequence> directly. We capture the per-iteration line via
    // a wrap on trimmedMessages.get(int) into ChatAnimator.setCurrentRenderLine, then
    // the fill / drawShadow wraps below read it back. (Going through @Local on the loop
    // local turned out to be too brittle on these older versions.)
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0),
            require = 0
    )
    private Object chatTools$captureLine_pose2(java.util.List<?> list, int index,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Object> original) {
        Object line = original.call(list, index);
        ChatAnimator.setCurrentRenderLine(line);
        return line;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_pose2(
            com.mojang.blaze3d.vertex.PoseStack pose, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(ChatAnimator.currentRenderLine());
        if (off == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(pose, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawShadow_2arg(
            net.minecraft.client.gui.Font font, com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.util.FormattedCharSequence text, float x, float y, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(ChatAnimator.currentRenderLine());
        if (off == null) return original.call(font, pose, text, x, y, color);
        return original.call(font, pose, text, x + off[0], y + off[1], color);
    }
*///?} else {
    /*// 1.16.5: same 2-arg render(PoseStack, int) and same GuiComponent.fill/Font.drawShadow
    // signatures as 1.17/1.18. Reuse the trimmedMessages.get hint-capture approach.
    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Ljava/util/List;get(I)Ljava/lang/Object;", ordinal = 0),
            require = 0
    )
    private Object chatTools$captureLine_1_16(java.util.List<?> list, int index,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Object> original) {
        Object line = original.call(list, index);
        ChatAnimator.setCurrentRenderLine(line);
        return line;
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiComponent;fill(Lcom/mojang/blaze3d/vertex/PoseStack;IIIII)V", ordinal = 0),
            require = 0
    )
    private void chatTools$wrapBgFill_1_16(
            com.mojang.blaze3d.vertex.PoseStack pose, int x0, int y0, int x1, int y1, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Void> original) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(ChatAnimator.currentRenderLine());
        if (off == null) { original.call(pose, x0, y0, x1, y1, color); return; }
        int dx = Math.round(off[0]); int dy = Math.round(off[1]);
        original.call(pose, x0 + dx, y0 + dy, x1 + dx, y1 + dy, color);
    }

    @com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Font;drawShadow(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/util/FormattedCharSequence;FFI)I", ordinal = 0),
            require = 0
    )
    private int chatTools$wrapDrawShadow_1_16(
            net.minecraft.client.gui.Font font, com.mojang.blaze3d.vertex.PoseStack pose,
            net.minecraft.util.FormattedCharSequence text, float x, float y, int color,
            com.llamalad7.mixinextras.injector.wrapoperation.Operation<Integer> original) {
        float[] off = net.apple70cents.chattools.features.general.ChatAnimationProxy.offsetFor(ChatAnimator.currentRenderLine());
        if (off == null) return original.call(font, pose, text, x, y, color);
        return original.call(font, pose, text, x + off[0], y + off[1], color);
    }
*///?}
}
