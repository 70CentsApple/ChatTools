package net.apple70cents.chattools.features.general;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Tracks slide-in animations for newly-inserted chat lines so they enter
 * the HUD smoothly instead of popping in. Each tracked line carries its
 * own start time, so multiple simultaneous insertions ease in correctly.
 *
 * <p>Detection of "new" vs "replayed" lines is delegated to the caller via
 * {@link #replaying()}: when {@link #pushReplaying()} is active (e.g. during
 * Minecraft's {@code refreshTrimmedMessages()}), newly-added lines are not
 * registered, which prevents the Chat Compactor's full list refresh from
 * re-playing the animation for every existing message.
 *
 * @author 70CentsApple
 */
public final class ChatAnimator {
    // ~250ms feels snappy without overstaying its welcome
    public static final long DURATION_NANOS = 250_000_000L;
    // Horizontal slide distance, in (pre-scale) chat-coordinate pixels
    public static final float SLIDE_DISTANCE = 24.0F;

    // Weak so deleted lines drop out without an explicit cleanup pass
    private static final Map<Object, Long> START_TIMES = new WeakHashMap<>();
    // Lines we've already inspected at least once — distinguishes "freshly inserted"
    // from "older, already-on-screen" lines without depending on whether the animation
    // is still in progress.
    private static final Set<Object> SEEN = Collections.newSetFromMap(new WeakHashMap<>());
    // Lines that should NOT push older lines upward — set when a Chat Compactor
    // "replace previous duplicate" happens, because the net effect on the chat stack
    // height is zero (1 line removed + 1 line added) so older content stays put.
    private static final Set<Object> NO_PUSH = Collections.newSetFromMap(new WeakHashMap<>());

    private static int replayDepth = 0;
    private static boolean nextLineNoPush = false;
    /**
     * Per-render-iteration hint set by a mixin {@code @WrapOperation} on the
     * {@code trimmedMessages.get(int)} call in older Minecraft versions, where
     * {@code @Local} capture of the loop variable inside subsequent {@code fill}
     * / {@code drawShadow} wraps is too fragile. The fill/drawShadow wrappers
     * read this back instead of trying to pick the line out of the local table.
     */
    private static Object currentRenderLine = null;
    public static void setCurrentRenderLine(Object line) { currentRenderLine = line; }
    public static Object currentRenderLine() { return currentRenderLine; }

    private ChatAnimator() {}

    public static void pushReplaying() {
        replayDepth++;
    }

    public static void popReplaying() {
        if (replayDepth > 0) replayDepth--;
    }

    public static boolean replaying() {
        return replayDepth > 0;
    }

    /**
     * Marks the given line as freshly inserted. Safe to call on the
     * render thread; no-ops if the line has already been seen or if a
     * replay is in progress. During a replay the line is still recorded
     * in {@link #SEEN} (without starting an animation) so it won't be
     * treated as "new" by a later non-replay insertion either.
     */
    public static void onNewLine(Object line) {
        if (line == null) return;
        if (SEEN.contains(line)) return;
        SEEN.add(line);
        if (nextLineNoPush) NO_PUSH.add(line);
        if (replaying()) return;
        START_TIMES.put(line, System.nanoTime());
    }

    /** True iff {@link #onNewLine} has already been called for this line. */
    public static boolean isTracked(Object line) {
        return line != null && SEEN.contains(line);
    }

    /** True iff this line should NOT push older lines upward (Chat Compactor replacement). */
    public static boolean isNoPush(Object line) {
        return line != null && NO_PUSH.contains(line);
    }

    /**
     * Mark "the next freshly-added line(s) should not contribute Y-push to older
     * lines". Set by the Chat Compactor path right after it removes the prior
     * duplicate and rescales — the upcoming line is a replacement, so the visible
     * stack height does not change and older content must stay put.
     */
    public static void markNextLineNoPush() {
        nextLineNoPush = true;
    }

    /** Clear the {@link #markNextLineNoPush()} flag after the addMessage chain processes it. */
    public static void clearNextLineNoPush() {
        nextLineNoPush = false;
    }

    /**
     * Eased animation progress for {@code line}, in [0, 1].
     * Returns 1 for untracked or finished lines (so callers can treat
     * "not animating" and "fully arrived" uniformly).
     */
    public static float easedProgress(Object line) {
        Long start = START_TIMES.get(line);
        if (start == null) return 1.0F;
        long elapsed = System.nanoTime() - start.longValue();
        if (elapsed >= DURATION_NANOS) {
            START_TIMES.remove(line);
            return 1.0F;
        }
        float t = (float) elapsed / (float) DURATION_NANOS;
        return easeOutCubic(t);
    }

    /** Non-linear ease — fast start, gentle settle. */
    private static float easeOutCubic(float t) {
        float inv = 1.0F - t;
        return 1.0F - inv * inv * inv;
    }
}
