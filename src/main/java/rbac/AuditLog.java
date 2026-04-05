package rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Система аудита с асинхронным логированием через очередь (Подзадача 3).
 */
public class AuditLog {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    //  Очередь для асинхронного логирования
    private final BlockingQueue<AuditEntry> logQueue = new LinkedBlockingQueue<>(1000);
    private final ExecutorService logWriterExecutor;
    private volatile boolean running = true;

    // Статистика
    private final AtomicInteger loggedCount = new AtomicInteger(0);
    private final AtomicInteger droppedCount = new AtomicInteger(0);

    // Для совместимости с существующим API
    private final List<AuditEntry> inMemoryEntries = new ArrayList<>();

    public AuditLog() {
        // Отдельный поток для записи логов
        this.logWriterExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "AuditLog-Writer");
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        });
        startLogWriter();
    }

    private void startLogWriter() {
        logWriterExecutor.submit(() -> {
            while (running || !logQueue.isEmpty()) {
                try {
                    AuditEntry entry = logQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (entry != null) {
                        // Для совместимости сохраняем в память
                        synchronized (inMemoryEntries) {
                            inMemoryEntries.add(entry);
                        }
                        loggedCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     *  Асинхронное логирование (не блокирует основной поток).
     */
    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);

        if (!logQueue.offer(entry)) {
            droppedCount.incrementAndGet();
            System.err.println("[AuditLog] Переполнение очереди, запись отклонена");
        }
    }

    /**
     * Синхронное логирование для критических записей.
     */
    public void logSync(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        synchronized (inMemoryEntries) {
            inMemoryEntries.add(entry);
        }
        loggedCount.incrementAndGet();
    }

    public List<AuditEntry> getAll() {
        synchronized (inMemoryEntries) {
            return new ArrayList<>(inMemoryEntries);
        }
    }

    public List<AuditEntry> getByPerformer(String performer) {
        synchronized (inMemoryEntries) {
            return inMemoryEntries.stream()
                    .filter(entry -> entry.performer().equals(performer))
                    .collect(Collectors.toList());
        }
    }

    public List<AuditEntry> getByAction(String action) {
        synchronized (inMemoryEntries) {
            return inMemoryEntries.stream()
                    .filter(entry -> entry.action().equals(action))
                    .collect(Collectors.toList());
        }
    }

    public void printLog() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ЖУРНАЛ АУДИТА                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        List<AuditEntry> entries;
        synchronized (inMemoryEntries) {
            entries = new ArrayList<>(inMemoryEntries);
        }

        if (entries.isEmpty()) {
            System.out.println("Нет записей в журнале аудита");
            return;
        }

        System.out.printf("%-20s %-15s %-15s %-20s%n", "Время", "Действие", "Исполнитель", "Цель");
        System.out.println("─────────────────────────────────────────────────────────────────────────────");

        for (AuditEntry entry : entries) {
            System.out.printf("%-20s %-15s %-15s %-20s%n",
                    entry.timestamp(),
                    entry.action(),
                    entry.performer(),
                    entry.target());

            if (entry.details() != null && !entry.details().trim().isEmpty()) {
                System.out.println("  Детали: " + entry.details());
            }
            System.out.println();
        }
    }

    public void saveToFile(String filename) {
        List<AuditEntry> entries;
        synchronized (inMemoryEntries) {
            entries = new ArrayList<>(inMemoryEntries);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Журнал аудита системы RBAC\n");
            writer.write("Всего записей: " + entries.size() + "\n");
            writer.write("═══════════════════════════════════════════════════════════════\n\n");

            for (AuditEntry entry : entries) {
                writer.write(String.format("[%s] %s by %s on %s\n",
                        entry.timestamp(),
                        entry.action(),
                        entry.performer(),
                        entry.target()));

                if (entry.details() != null && !entry.details().trim().isEmpty()) {
                    writer.write("  Детали: " + entry.details() + "\n");
                }
                writer.write("\n");
            }
            System.out.println("✓ Журнал аудита сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("✗ Ошибка при сохранении журнала: " + e.getMessage());
        }
    }

    /**
     * Получить статистику логирования.
     */
    public String getStatistics() {
        return String.format("Записано: %d | Отклонено: %d | В очереди: %d",
                loggedCount.get(),
                droppedCount.get(),
                logQueue.size());
    }

    /**
     * Корректное завершение работы.
     */
    public void shutdown() {
        running = false;
        logWriterExecutor.shutdown();
        try {
            if (!logWriterExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logWriterExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logWriterExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logSync("SHUTDOWN", "system", "AuditLog", "Аудит завершен. " + getStatistics());
    }

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}
}