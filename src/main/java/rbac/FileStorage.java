package rbac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Сохранение и загрузка данных системы RBAC (Подзадача 4/5).
 */
public class FileStorage {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Сохранение статистики в файл.
     */
    public static String saveStatistics(RBACSystem system, String filename) {
        try {
            StringBuilder data = new StringBuilder();
            data.append("╔════════════════════════════════════════════════════════════════╗\n");
            data.append("║                    СТАТИСТИКА RBAC                             ║\n");
            data.append("╚════════════════════════════════════════════════════════════════╝\n\n");
            data.append("Дата экспорта: ").append(LocalDateTime.now().format(FORMATTER)).append("\n\n");
            data.append(system.generateStatistics());
            data.append("\nПользователей: ").append(system.getUserManager().count()).append("\n");
            data.append("Ролей: ").append(system.getRoleManager().count()).append("\n");
            data.append("Назначений: ").append(system.getAssignmentManager().count()).append("\n");

            Files.writeString(Path.of(filename), data.toString());
            return "✓ Данные сохранены в файл: " + filename;
        } catch (IOException e) {
            return "✗ Ошибка сохранения: " + e.getMessage();
        }
    }

    /**
     * Загрузка статистики из файла (для отображения).
     */
    public static String loadStatistics(String filename) {
        try {
            String content = Files.readString(Path.of(filename));
            return "✓ Данные загружены из файла: " + filename + "\n\n" + content;
        } catch (IOException e) {
            return "✗ Ошибка загрузки: " + e.getMessage();
        }
    }

    /**
     * Экспорт отчёта в файл.
     */
    public static String exportReport(String report, String filename) {
        try {
            Files.writeString(Path.of(filename), report);
            return "✓ Отчёт сохранён в файл: " + filename;
        } catch (IOException e) {
            return "✗ Ошибка экспорта: " + e.getMessage();
        }
    }

    /**
     * Проверка существования файла.
     */
    public static boolean fileExists(String filename) {
        return Files.exists(Path.of(filename));
    }

    /**
     * Создание резервной копии.
     */
    public static String createBackup(String sourceFile) {
        if (!fileExists(sourceFile)) {
            return "✗ Файл не найден: " + sourceFile;
        }

        String backupFile = sourceFile + ".backup." +
                LocalDateTime.now().format(FORMATTER);

        try {
            Files.copy(Path.of(sourceFile), Path.of(backupFile));
            return "✓ Резервная копия создана: " + backupFile;
        } catch (IOException e) {
            return "✗ Ошибка создания копии: " + e.getMessage();
        }
    }
}