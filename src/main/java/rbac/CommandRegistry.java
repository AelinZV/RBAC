package rbac;

import java.util.Scanner;


public class CommandRegistry {

    public static void registerAllCommands(CommandParser parser) {
        // user-list
        parser.registerCommand("user-list", "Вывести список всех пользователей",
                (scanner, system) -> {
                    System.out.println("\n=== Список пользователей ===");
                    var users = system.getUserManager().findAll();
                    if (users.isEmpty()) {
                        System.out.println("Нет зарегистрированных пользователей");
                        return;
                    }
                    System.out.printf("%-20s %-30s%n", "Username", "Email");
                    System.out.println("──────────────────────────────────────────────────────");
                    for (var user : users) {
                        System.out.printf("%-20s %-30s%n", user.username(), user.email());
                    }
                });

        // user-create
        parser.registerCommand("user-create", "Создать нового пользователя",
                (scanner, system) -> {
                    System.out.println("\n=== Создание пользователя ===");
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    System.out.print("Full Name: ");
                    String fullName = scanner.nextLine().trim();
                    System.out.print("Email: ");
                    String email = scanner.nextLine().trim();

                    try {
                        var user = User.validate(username, fullName, email);
                        system.getUserManager().add(user);
                        System.out.println("✓ Пользователь '" + username + "' успешно создан");
                    } catch (Exception e) {
                        System.out.println("✗ Ошибка: " + e.getMessage());
                    }
                });

        // user-view
        parser.registerCommand("user-view", "Просмотр информации о пользователе",
                (scanner, system) -> {
                    System.out.println("\n=== Просмотр пользователя ===");
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    var userOpt = system.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("✗ Пользователь '" + username + "' не найден");
                        return;
                    }
                    var user = userOpt.get();
                    System.out.println("\n" + user.format());

                    // Назначенные роли
                    var assignments = system.getAssignmentManager().findByUser(user);
                    System.out.println("\nНазначенные роли (" + assignments.size() + "):");
                    if (assignments.isEmpty()) {
                        System.out.println("  Нет назначенных ролей");
                    } else {
                        for (var a : assignments) {
                            System.out.println("  • " + a.role().name() + " [" + a.assignmentType() + "]");
                        }
                    }
                });

        // role-list
        parser.registerCommand("role-list", "Вывести список всех ролей",
                (scanner, system) -> {
                    System.out.println("\n=== Список ролей ===");
                    var roles = system.getRoleManager().findAll();
                    if (roles.isEmpty()) {
                        System.out.println("Нет зарегистрированных ролей");
                        return;
                    }
                    System.out.printf("%-25s %-10s%n", "Название роли", "Права");
                    System.out.println("──────────────────────────────────────────────────────");
                    for (var role : roles) {
                        System.out.printf("%-25s %-10d%n", role.name(), role.getPermissions().size());
                    }
                });

        // role-create
        parser.registerCommand("role-create", "Создать новую роль",
                (scanner, system) -> {
                    System.out.println("\n=== Создание роли ===");
                    System.out.print("Название роли: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Описание: ");
                    String description = scanner.nextLine().trim();
                    try {
                        var role = new Role(name, description);
                        system.getRoleManager().add(role);
                        System.out.println("✓ Роль '" + name + "' создана");
                    } catch (Exception e) {
                        System.out.println("✗ Ошибка: " + e.getMessage());
                    }
                });

        // assign-role
        parser.registerCommand("assign-role", "Назначить роль пользователю",
                (scanner, system) -> {
                    System.out.println("\n=== Назначение роли ===");
                    System.out.print("Username: ");
                    String username = scanner.nextLine().trim();
                    var userOpt = system.getUserManager().findByUsername(username);
                    if (userOpt.isEmpty()) {
                        System.out.println("✗ Пользователь не найден");
                        return;
                    }

                    System.out.println("Доступные роли:");
                    var roles = system.getRoleManager().findAll();
                    for (int i = 0; i < roles.size(); i++) {
                        System.out.println("  " + (i + 1) + ". " + roles.get(i).name());
                    }
                    System.out.print("Выберите номер роли: ");
                    try {
                        int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                        if (idx < 0 || idx >= roles.size()) {
                            System.out.println("✗ Неверный номер");
                            return;
                        }
                        var role = roles.get(idx);
                        var meta = AssignmentMetadata.now(system.getCurrentUser(), "Ручное назначение");
                        var assignment = new PermanentAssignment(userOpt.get(), role, meta);
                        system.getAssignmentManager().add(assignment);
                        System.out.println("✓ Роль '" + role.name() + "' назначена пользователю '" + username + "'");
                    } catch (NumberFormatException e) {
                        System.out.println("✗ Неверный формат номера");
                    } catch (Exception e) {
                        System.out.println("✗ Ошибка: " + e.getMessage());
                    }
                });

        // stats
        parser.registerCommand("stats", "Показать статистику системы",
                (scanner, system) -> System.out.println(system.generateStatistics()));

        // help
        parser.registerCommand("help", "Вывести справку по командам",
                (scanner, system) -> parser.printHelp());

        // exit
        parser.registerCommand("exit", "Выход из системы",
                (scanner, system) -> {
                    System.out.print("Подтвердите выход (да/нет): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("да")) {
                        System.out.println("\nВыход из системы. До свидания!");
                        System.exit(0);
                    }
                });
    }
}