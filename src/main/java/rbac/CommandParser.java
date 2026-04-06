package rbac;

import java.util.*;

public class CommandParser {

    private final Map<String, Command> commands = new HashMap<>();
    private final Map<String, String> commandDescriptions = new HashMap<>();

    public void registerCommand(String name, String description, Command command) {
        String cmd = name.trim().toLowerCase();
        commands.put(cmd, command);
        commandDescriptions.put(cmd, description != null ? description : "");
    }

    public void executeCommand(String commandName, Scanner scanner, RBACSystem system) {
        String cmd = commandName.trim().toLowerCase();
        Command command = commands.get(cmd);

        if (command == null) {
            System.out.println("✗ Неизвестная команда: '" + cmd + "'");
            System.out.println("Введите 'help' для списка доступных команд");
            return;
        }

        try {
            command.execute(scanner, system);
        } catch (Exception e) {
            System.out.println("✗ Ошибка при выполнении команды '" + cmd + "': " + e.getMessage());
        }
    }

    public void printHelp() {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    СПРАВКА ПО КОМАНДАМ                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        System.out.println("=== Управление пользователями ===");
        System.out.println("  user-list        Вывести список всех пользователей");
        System.out.println("  user-create      Создать нового пользователя");
        System.out.println("  user-view        Просмотр информации о пользователе");

        System.out.println("\n=== Управление ролями ===");
        System.out.println("  role-list        Вывести список всех ролей");
        System.out.println("  role-create      Создать новую роль");
        System.out.println("  assign-role      Назначить роль пользователю");

        System.out.println("\n=== Отчёты и файлы ===");
        System.out.println("  stats            Показать статистику системы");
        System.out.println("  report-users-async  Асинхронная генерация отчёта по пользователям");
        System.out.println("  save-async       Асинхронное сохранение данных в файл");
        System.out.println("  file-save        Сохранить статистику в файл (синхронно)");
        System.out.println("  file-load        Загрузить статистику из файла");
        System.out.println("  backup           Создать резервную копию файла");

        System.out.println("\n=== Периодические задачи ===");
        System.out.println("  periodic-status  Показать статус периодических задач");

        System.out.println("\n=== Системные ===");
        System.out.println("  help             Вывести эту справку");
        System.out.println("  exit             Выход из системы");
        System.out.println("\n══════════════════════════════════════════════════════════════════");
    }

    public void parseAndExecute(String input, Scanner scanner, RBACSystem system) {
        if (input == null || input.trim().isEmpty()) return;
        String[] parts = input.trim().split("\\s+", 2);
        executeCommand(parts[0], scanner, system);
    }
}