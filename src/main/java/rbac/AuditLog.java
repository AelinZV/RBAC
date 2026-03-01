package rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class AuditLog {

    // Форматер для даты и времени
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Записи аудита
    private final List<AuditEntry> entries = new ArrayList<>();


    public void log(String action, String performer, String target, String details) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        AuditEntry entry = new AuditEntry(timestamp, action, performer, target, details);
        entries.add(entry);
    }


    public List<AuditEntry> getAll() {
        return new ArrayList<>(entries);
    }


    public List<AuditEntry> getByPerformer(String performer) {
        return entries.stream()
                .filter(entry -> entry.performer().equals(performer))
                .collect(Collectors.toList());
    }


    public List<AuditEntry> getByAction(String action) {
        return entries.stream()
                .filter(entry -> entry.action().equals(action))
                .collect(Collectors.toList());
    }


    public void printLog() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ЖУРНАЛ АУДИТА                              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

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

    public record AuditEntry(
            String timestamp,
            String action,
            String performer,
            String target,
            String details
    ) {}
}