package net.apple70cents.chattools.features.general;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Pure-Java runtime helpers for the chat slide-in animation. Lives outside
 * the mixin class because mixin classes are loaded by SpongePowered Mixin's
 * own transformer and cannot reliably be referenced as plain runtime classes
 * from injected code (FML treats such loads as "invalid").
 *
 * <p>Intentionally contains no reflection against Minecraft types — those
 * names get remapped (intermediary on Fabric, SRG/etc on older Forge) so
 * looking them up by string would silently fail in production. All MC-typed
 * access happens in the mixin (where {@code @Shadow} / direct typed calls go
 * through the refmap properly) and the results are handed in here as plain
 * Java values / lambdas.
 *
 * @author 70CentsApple
 */
public final class ChatAnimationProxy {
    private ChatAnimationProxy() {}

    /**
     * Wraps {@code delegate} (a {@code ChatComponent$LineConsumer}) so that each
     * {@code accept(...)} call applies the per-line offset before delegating.
     *
     * @param delegate       the original LineConsumer (3-arg or 6-arg flavour)
     * @param offsetQuery    {@code line -> {dx, dy}} (chat-space pixels) or {@code null}
     *                       for "no offset" — supplied by the mixin so all MC-typed
     *                       field access goes through {@code @Shadow}
     * @param poseTranslate  {@code (dx, dy) -> graphics.updatePose(p -> p.translate(dx, dy))};
     *                       only used by the 3-arg LineConsumer flavour, which has no
     *                       coords to mutate. {@code null} is fine for the 6-arg flavour.
     */
    public static Object decorate(
            Object delegate,
            Function<Object, float[]> offsetQuery,
            BiConsumer<Float, Float> poseTranslate) {
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
                    // Only accept(...) needs offsetting; toString/equals/hashCode etc.
                    // go straight through. Two LineConsumer flavours exist across versions:
                    //   3-arg  accept(Line, int lineIndex, float alpha)               [1.21.11+ / 26.1+]
                    //   6-arg  accept(int x, int startY, int endY, Line, idx, fade)   [1.21.6 – 1.21.10]
                    // The 3-arg flavour gives us no coords to mutate, so we sandwich it
                    // with the supplied {@code poseTranslate}. The 6-arg flavour lets us
                    // shift x / startY / endY directly — much cleaner.
                    if ("accept".equals(method.getName()) && args != null) {
                        if (args.length == 3) {
                            return invokeWithPoseOffset(delegate, method, args, offsetQuery, poseTranslate);
                        }
                        if (args.length == 6) {
                            return invokeWithCoordOffset(delegate, method, args, offsetQuery);
                        }
                    }
                    return method.invoke(delegate, args);
                });
    }

    /**
     * 6-arg LineConsumer: {@code accept(int x, int startY, int endY, Line, idx, fade)}.
     * The lambda body uses x/startY/endY to position both the background fill and the
     * text draw, so a single arg edit moves everything for that line.
     */
    private static Object invokeWithCoordOffset(
            Object delegate, Method method, Object[] args,
            Function<Object, float[]> offsetQuery) throws Throwable {
        Object line = args[3];
        float[] off = offsetQuery == null ? null : offsetQuery.apply(line);
        if (off == null) return method.invoke(delegate, args);
        int dx = Math.round(off[0]);
        int dy = Math.round(off[1]);
        Object[] shifted = args.clone();
        shifted[0] = ((Integer) args[0]) + dx;
        shifted[1] = ((Integer) args[1]) + dy;
        shifted[2] = ((Integer) args[2]) + dy;
        return method.invoke(delegate, shifted);
    }

    /**
     * 3-arg LineConsumer: {@code accept(Line, int lineIndex, float alpha)}.
     * No coords to mutate — sandwich the call with a pose translate that the mixin
     * supplies (so we never touch {@code ChatGraphicsAccess} or {@code Matrix3x2f}
     * by name from here).
     */
    private static Object invokeWithPoseOffset(
            Object delegate, Method method, Object[] args,
            Function<Object, float[]> offsetQuery,
            BiConsumer<Float, Float> poseTranslate) throws Throwable {
        Object line = args[0];
        float[] off = offsetQuery == null ? null : offsetQuery.apply(line);
        if (off == null || poseTranslate == null) {
            return method.invoke(delegate, args);
        }
        final float dx = off[0];
        final float dy = off[1];
        poseTranslate.accept(dx, dy);
        try {
            return method.invoke(delegate, args);
        } finally {
            poseTranslate.accept(-dx, -dy);
        }
    }

    /**
     * Per-line offset math used by both the LineConsumer Proxy (above) and the
     * inline-render {@code @WrapOperation} branches in the mixin (1.20.x / 1.19.x /
     * 1.16-1.18). Caller supplies the current {@code trimmedMessages} list and the
     * entry-height in chat-space pixels, all obtained via {@code @Shadow} so no
     * remapped string lookup is involved.
     *
     * @return {@code {dx, dy}} chat-space offset, or {@code null} for a line that
     *         has no offset to apply right now.
     */
    public static float[] offsetFor(Object target, List<?> trimmedMessages, double entryHeight) {
        if (target == null || trimmedMessages == null || trimmedMessages.isEmpty()) return null;

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
            // Chat Compactor replacements don't change the stack height, so they must
            // not push older lines around even though they themselves slide in.
            if (ChatAnimator.isNoPush(line)) continue;
            belowIncomingSum += 1.0F - ChatAnimator.easedProgress(line);
        }
        if (!found) return null;
        if (belowIncomingSum <= 0.0F && selfIncoming <= 0.0F) return null;

        float dx = -selfIncoming * ChatAnimator.SLIDE_DISTANCE;
        float dy = belowIncomingSum * (float) entryHeight;
        return new float[] { dx, dy };
    }
}
