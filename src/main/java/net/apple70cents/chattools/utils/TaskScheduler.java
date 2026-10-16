package net.apple70cents.chattools.utils;

import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unified task scheduler replacing raw threads and Thread.sleep.
 * Provides tracking and mass cancellation of pending tasks on disconnect/leave world.
 */
public final class TaskScheduler {
    private static final AtomicInteger THREAD_COUNTER = new AtomicInteger(1);
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(
            2,
            runnable -> {
                Thread thread = new Thread(runnable, "ChatTools-Scheduler-" + THREAD_COUNTER.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
    );

    private static final Set<ScheduledFuture<?>> PENDING_TASKS = ConcurrentHashMap.newKeySet();

    private TaskScheduler() {}

    /**
     * Schedules a task to be executed after the specified delay in milliseconds.
     * The task is automatically registered and removed from tracking upon completion.
     *
     * @param command the task to execute
     * @param delayMs delay in milliseconds
     */
    public static void schedule(Runnable command, long delayMs) {
        long delay = Math.max(0, delayMs);
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        ScheduledFuture<?> future = EXECUTOR.schedule(() -> {
            try {
                command.run();
            } catch (Throwable t) {
                LoggerUtils.error("[ChatTools] Scheduled task failed", t);
            } finally {
                if (futureHolder[0] != null) {
                    PENDING_TASKS.remove(futureHolder[0]);
                }
            }
        }, delay, TimeUnit.MILLISECONDS);
        futureHolder[0] = future;
        PENDING_TASKS.add(future);
    }

    /**
     * Cancels all pending scheduled tasks and clears the queue.
     * Called when the player disconnects from a server or leaves a singleplayer world.
     */
    public static void cancelAll() {
        int count = 0;
        for (ScheduledFuture<?> future : PENDING_TASKS) {
            if (future != null && !future.isDone() && !future.isCancelled()) {
                future.cancel(true);
                count++;
            }
        }
        PENDING_TASKS.clear();
        if (count > 0) {
            LoggerUtils.info("[ChatTools] Cancelled " + count + " pending scheduled chat tasks upon disconnection.");
        }
    }
}
