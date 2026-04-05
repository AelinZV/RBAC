package rbac;

import java.util.concurrent.*;

/**
 * Менеджер фоновых задач на основе ExecutorService (Подзадача 3).
 */
public class BackgroundExecutor {

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running = true;

    public BackgroundExecutor() {
        // Пул из 4 потоков для фоновых задач
        this.executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "BackgroundExecutor-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });

        // 2 потока для периодических задач
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "ScheduledExecutor-" + System.nanoTime());
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Асинхронное выполнение задачи с возвратом результата.
     */
    public <T> CompletableFuture<T> submitAsync(Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        }, executor);
    }

    /**
     * Асинхронное выполнение задачи без результата.
     */
    public CompletableFuture<Void> runAsync(Runnable task) {
        return CompletableFuture.runAsync(task, executor);
    }

    /**
     * Периодическое выполнение задачи.
     */
    public ScheduledFuture<?> scheduleAtFixedRate(
            Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(task, initialDelay, period, unit);
    }

    /**
     * Отложенное выполнение задачи.
     */
    public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(task, delay, unit);
    }

    public boolean isRunning() {
        return running && !executor.isShutdown();
    }

    /**
     * Корректное завершение работы.
     */
    public void shutdown() {
        running = false;
        executor.shutdown();
        scheduler.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}