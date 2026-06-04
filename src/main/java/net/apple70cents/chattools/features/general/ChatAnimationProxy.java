package net.apple70cents.chattools.features.general;

import net.minecraft.client.Minecraft;

import java.lang.reflect.Proxy;
import java.util.List;

/**
 * Runtime helpers for the chat slide-in animation. Lives outside the
 * mixin class because mixin classes are loaded by SpongePowered Mixin's
 * own transformer and cannot reliably be referenced as plain runtime
 * classes from injected code (FML treats such loads as "invalid").
 *
 * <p>All work here is reflection-only: we never name the package-private
 * {@code ChatComponent.LineConsumer} interface in our bytecode — we look
 * it up from the delegate's implemented-interfaces list at runtime, and
 * build a JDK {@link Proxy} against the same set.
 *
 * @author 70CentsApple
 */
public final class ChatAnimationProxy {
    private ChatAnimationProxy() {}

    /**
     * Wraps {@code delegate} (a {@code ChatComponent$LineConsumer}) so that
     * each {@code accept(GuiMessage.Line, int, float)} call is bracketed by
     * a {@code graphics.updatePose} translate / un-translate that drags
     * just that line to its current animation offset.
     */
    public static Object decorate(Object delegate, Object graphics) {
        if (delegate == null) return null;
        Class<?>[] interfaces = delegate.getClass().getInterfaces();
        if (interfaces.length == 0) {
            // Defensive: an unexpected non-LineConsumer would defeat the proxy.
            // Leave the call unmolested so chat still renders.
            return delegate;
        }
        return Proxy.newProxyInstance(
                delegate.getClass().getClassLoader(),
                interfaces,
                (proxy, method, args) -> {
                    // LineConsumer is package-private; bypass Java's reflective access
                    // check so we can dispatch its abstract accept(...) cleanly.
                    try { method.setAccessible(true); } catch (Throwable ignored) {}
                    // Only accept(...) needs offsetting; toString/equals/hashCode etc. go through.
                    if ("accept".equals(method.getName()) && args != null) {
                        // Two LineConsumer flavours exist across versions:
                        //   3-arg  accept(Line, int lineIndex, float alpha)               [1.21.11+ / 26.1+]
                        //   6-arg  accept(int x, int startY, int endY, Line, int idx, float fade)  [1.21.6 – 1.21.10]
                        // The 3-arg flavour gives us no coords to mutate, so we sandwich
                        // it with a pose translate via graphics.updatePose. The 6-arg
                        // flavour lets us shift x/startY/endY directly — much cleaner and
                        // doesn't need the graphics reference.
                        if (args.length == 3) {
                            return invokeWithOffset(delegate, method, args, graphics);
                        }
                        if (args.length == 6) {
                            return invokeWithSixArgOffset(delegate, method, args);
                        }
                    }
                    return method.invoke(delegate, args);
                });
    }

    /**
     * 6-arg LineConsumer (1.21.6 – 1.21.10):
     * {@code accept(int x, int startY, int endY, GuiMessage.Line line, int idx, float fade)}.
     * The lambda body uses x/startY/endY to position both the background fill and the
     * text draw, so a single arg edit moves everything for that line.
     */
    private static Object invokeWithSixArgOffset(Object delegate, java.lang.reflect.Method method,
                                                 Object[] args) throws Throwable {
        Object line = args[3];
        List<Object> trimmedMessages = readTrimmedMessages();
        float[] off = lineOffset(trimmedMessages, line);
        if (off == null) return method.invoke(delegate, args);
        int dx = Math.round(off[0]);
        int dy = Math.round(off[1]);
        Object[] shifted = args.clone();
        shifted[0] = ((Integer) args[0]) + dx;
        shifted[1] = ((Integer) args[1]) + dy;
        shifted[2] = ((Integer) args[2]) + dy;
        return method.invoke(delegate, shifted);
    }

    private static Object invokeWithOffset(Object delegate, java.lang.reflect.Method method,
                                           Object[] args, Object graphics) throws Throwable {
        Object line = args[0];
        // Re-read trimmedMessages via reflection; the field is package-private and
        // we already have an AT widening getScale() but not the field itself. A
        // single per-line lookup per render frame is negligible.
        List<Object> trimmedMessages = readTrimmedMessages();
        float[] off = lineOffset(trimmedMessages, line);
        if (off == null) {
            return method.invoke(delegate, args);
        }
        final float dx = off[0];
        final float dy = off[1];
        applyPose(graphics, dx, dy);
        try {
            return method.invoke(delegate, args);
        } finally {
            applyPose(graphics, -dx, -dy);
        }
    }

    /** Reflectively reach {@code ChatComponent#trimmedMessages} on the live HUD. */
    @SuppressWarnings("unchecked")
    private static List<Object> readTrimmedMessages() {
        try {
            Object chat = Minecraft.getInstance().gui.getChat();
            java.lang.reflect.Field f = chat.getClass().getDeclaredField("trimmedMessages");
            f.setAccessible(true);
            return (List<Object>) f.get(chat);
        } catch (Throwable t) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * @return {x, y} per-line offset, or {@code null} for a fully-arrived line
     *         that sits above all in-progress ones.
     */
    private static float[] lineOffset(List<Object> trimmedMessages, Object target) {
        if (trimmedMessages.isEmpty()) return null;
        float belowIncomingSum = 0.0F;
        float selfIncoming = 0.0F;
        boolean found = false;
        // Walk every entry below `target` and accumulate its (1 - eased) — we can't
        // short-circuit on a fully-arrived line in between, because a fresh insertion
        // pushes ALL older lines up by one slot regardless of what is or isn't
        // animating between them. The contribution from a rested line is simply 0.
        for (Object line : trimmedMessages) {
            if (line == target) {
                selfIncoming = 1.0F - ChatAnimator.easedProgress(line);
                found = true;
                break;
            }
            // Chat Compactor replacements don't change the stack height, so they
            // must not push older lines around even though they themselves slide in.
            if (ChatAnimator.isNoPush(line)) continue;
            belowIncomingSum += 1.0F - ChatAnimator.easedProgress(line);
        }
        if (!found) return null;
        if (belowIncomingSum <= 0.0F && selfIncoming <= 0.0F) return null;

        float dx = -selfIncoming * ChatAnimator.SLIDE_DISTANCE;
        float dy = belowIncomingSum * (float) entryHeight();
        return new float[] { dx, dy };
    }

    /**
     * Public single-line offset query for use from mixin call-site wrappers
     * (1.20.5 – 1.21.5 path, where we shift {@code GuiGraphics.fill} /
     * {@code GuiGraphics.drawString} args directly rather than going through
     * a LineConsumer proxy).
     */
    public static float[] offsetFor(Object line) {
        if (line == null) return null;
        return lineOffset(readTrimmedMessages(), line);
    }

    private static double entryHeight() {
        // chatLineSpacing is an OptionInstance<Double> on 1.18+ and a primitive
        // double field on 1.16/1.17 — go through reflection so this helper stays
        // version-agnostic.
        try {
            Object options = Minecraft.getInstance().options;
            try {
                java.lang.reflect.Method m = options.getClass().getMethod("chatLineSpacing");
                Object inst = m.invoke(options);
                Object value = inst.getClass().getMethod("get").invoke(inst);
                return 9.0 * (((Number) value).doubleValue() + 1.0);
            } catch (NoSuchMethodException nsme) {
                java.lang.reflect.Field f = options.getClass().getField("chatLineSpacing");
                return 9.0 * (f.getDouble(options) + 1.0);
            }
        } catch (Throwable t) {
            // Sensible default: 9 px line height (chatLineSpacing = 0).
            return 9.0;
        }
    }

    /**
     * Apply a 2D translation through the {@code graphics.updatePose(Consumer)} hook
     * on {@code ChatComponent$ChatGraphicsAccess}. Reflective so we don't have to
     * name the access interface; the method is invoked once per call site.
     */
    private static void applyPose(Object graphics, float dx, float dy) {
        try {
            java.lang.reflect.Method updatePose = graphics.getClass().getMethod("updatePose", java.util.function.Consumer.class);
            try { updatePose.setAccessible(true); } catch (Throwable ignored) {}
            java.util.function.Consumer<Object> translate = pose -> {
                try {
                    // pose is org.joml.Matrix3x2f; translate(float, float) exists.
                    java.lang.reflect.Method m = pose.getClass().getMethod("translate", float.class, float.class);
                    try { m.setAccessible(true); } catch (Throwable ignored) {}
                    m.invoke(pose, dx, dy);
                } catch (Throwable ignored) {
                }
            };
            updatePose.invoke(graphics, translate);
        } catch (Throwable ignored) {
        }
    }
}
