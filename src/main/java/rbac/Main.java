package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Демонстрационный класс для тестирования всех подзадач RBAC:
 * 1. Базовые структуры данных
 * 2. Фильтры и сортировка
 * 3. Менеджеры данных
 * 4. Система команд и меню
 * 5. Дополнительные утилиты
 */
public class Main {
    // Форматер для создания метаданных с кастомной датой
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Вспомогательный метод для создания метаданных с заданной датой
    private static AssignmentMetadata createMetadata(String assignedBy, String reason, String date) {
        return new AssignmentMetadata(assignedBy, date, reason);
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║          ДЕМОНСТРАЦИЯ ВСЕХ ПОДЗАДАЧ СИСТЕМЫ RBAC              ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // ==================== ПОДЗАДАЧА 1: БАЗОВЫЕ СТРУКТУРЫ ====================
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ПОДЗАДАЧА 1: БАЗОВЫЕ СТРУКТУРЫ ДАННЫХ                        ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        testBaseStructures();

        // ==================== ПОДЗАДАЧА 2: ФИЛЬТРЫ И СОРТИРОВКА ====================
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ПОДЗАДАЧА 2: ФИЛЬТРЫ И СОРТИРОВКА                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        testUserFilters();
        testRoleFilters();
        testAssignmentFilters();
        testSorting();

        // ==================== ПОДЗАДАЧА 3: МЕНЕДЖЕРЫ ДАННЫХ ====================
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ПОДЗАДАЧА 3: МЕНЕДЖЕРЫ ДАННЫХ                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        testManagers();

        // ==================== ПОДЗАДАЧА 5: УТИЛИТЫ ====================
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║  ПОДЗАДАЧА 5: ДОПОЛНИТЕЛЬНЫЕ УТИЛИТЫ                          ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");
        testUtilities();

        // ==================== ИТОГИ ДЕМОНСТРАЦИИ ====================
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        // ==================== ПЕРЕХОД В ИНТЕРАКТИВНЫЙ РЕЖИМ (ПОДЗАДАЧА 4) ====================
        System.out.print("\nПерейти в интерактивный режим управления системой (подзадача 4)? (да/нет): ");
        Scanner scanner = new Scanner(System.in);
        String response = scanner.nextLine().trim().toLowerCase();

        if (response.equals("да") || response.equals("yes") || response.equals("y")) {
            startInteractiveMode(scanner);
        } else {
            System.out.println("\nВыход из программы. Спасибо за использование RBAC!");
            scanner.close();
        }
    }

    // ==================== ПОДЗАДАЧА 1: БАЗОВЫЕ СТРУКТУРЫ ====================
    private static void testBaseStructures() {
        System.out.println("=== ТЕСТ 1.1: Валидация пользователя ===");
        try {
            User u1 = User.validate("john_doe", "Иван Иванов", "ivan@example.com");
            System.out.println("✓ Валидный: " + u1.format());
        } catch (Exception e) {
            System.out.println("✗ Ошибка: " + e.getMessage());
        }

        try {
            User u2 = User.validate("ab", "Короткое", "неправильный-email");
        } catch (Exception e) {
            System.out.println("✓ Поймана ошибка: " + e.getMessage());
        }

        System.out.println("\n=== ТЕСТ 1.2: Права доступа ===");
        Permission p1 = new Permission("read", "USERS", "Просмотр списка пользователей");
        Permission p2 = new Permission("WRITE", "reports", "Редактирование отчётов");
        System.out.println("✓ " + p1.format());
        System.out.println("✓ " + p2.format());
        System.out.println("✓ Поиск 'READ': " + p1.matches("READ", null));

        System.out.println("\n=== ТЕСТ 1.3: Роль ===");
        Role viewer = new Role("Наблюдатель", "Только для чтения");
        viewer.addPermission(p1);
        viewer.addPermission(new Permission("VIEW", "reports", "Просмотр отчётов"));
        System.out.println(viewer.format());

        System.out.println("\n=== ТЕСТ 1.4: Метаданные ===");
        AssignmentMetadata meta = AssignmentMetadata.now("администратор", "Первоначальная настройка");
        System.out.println(meta.format());

        System.out.println("\n=== ТЕСТ 1.7: Постоянное назначение ===");
        User admin = User.validate("admin", "Администратор Системы", "admin@company.com");
        PermanentAssignment pa = new PermanentAssignment(admin, viewer, meta);
        System.out.println(pa.summary());

        System.out.println("\n=== ТЕСТ 1.8: Временное назначение ===");
        AssignmentMetadata tempMeta = AssignmentMetadata.now("менеджер", "Временный доступ для аудита");
        TemporaryAssignment ta = new TemporaryAssignment(
                admin,
                viewer,
                tempMeta,
                "2026-12-31 23:59"
        );
        System.out.println(ta.summary());
    }

    // ==================== ПОДЗАДАЧА 2.1: ФИЛЬТРЫ ПОЛЬЗОВАТЕЛЕЙ ====================
    private static void testUserFilters() {
        System.out.println("\n=== ТЕСТ 2.1: Фильтрация пользователей ===");

        List<User> users = Arrays.asList(
                User.validate("alex_smith", "Александр Смит", "alex@company.com"),
                User.validate("anna_jones", "Анна Джонс", "anna@company.com"),
                User.validate("bob_wilson", "Боб Уилсон", "bob@external.org"),
                User.validate("admin", "Администратор", "admin@company.com")
        );

        System.out.println("Все пользователи:");
        users.forEach(u -> System.out.println("  • " + u.username()));

        System.out.println("\n→ Фильтр: email заканчивается на '@company.com'");
        UserFilter companyFilter = UserFilters.byEmailDomain("@company.com");
        users.stream().filter(companyFilter).forEach(u ->
                System.out.println("  ✓ " + u.format())
        );

        System.out.println("\n→ Комбинированный фильтр: имя содержит 'ан' И email в @company.com");
        UserFilter combined = UserFilters.byFullNameContains("ан")
                .and(UserFilters.byEmailDomain("@company.com"));
        users.stream().filter(combined).forEach(u ->
                System.out.println("  ✓ " + u.fullName())
        );
    }

    // ==================== ПОДЗАДАЧА 2.2: ФИЛЬТРЫ РОЛЕЙ ====================
    private static void testRoleFilters() {
        System.out.println("\n=== ТЕСТ 2.2: Фильтрация ролей ===");

        Role adminRole = new Role("Администратор", "Полный доступ");
        adminRole.addPermission(new Permission("READ", "users", "Чтение пользователей"));
        adminRole.addPermission(new Permission("WRITE", "users", "Запись пользователей"));
        adminRole.addPermission(new Permission("DELETE", "users", "Удаление пользователей"));

        Role editorRole = new Role("Редактор", "Редактирование контента");
        editorRole.addPermission(new Permission("WRITE", "articles", "Редактирование статей"));
        editorRole.addPermission(new Permission("PUBLISH", "articles", "Публикация статей"));

        Role viewerRole = new Role("Наблюдатель", "Только просмотр");
        viewerRole.addPermission(new Permission("READ", "reports", "Просмотр отчётов"));

        List<Role> roles = Arrays.asList(adminRole, editorRole, viewerRole);

        System.out.println("Все роли:");
        roles.forEach(r -> System.out.println("  • " + r.name() + " (" + r.getPermissions().size() + " прав)"));

        System.out.println("\n→ Фильтр: роли с минимум 2 правами");
        RoleFilter min2Permissions = RoleFilters.hasAtLeastNPermissions(2);
        roles.stream().filter(min2Permissions).forEach(r ->
                System.out.println("  ✓ " + r.name() + " (" + r.getPermissions().size() + " прав)")
        );

        System.out.println("\n→ Фильтр: роли с правом WRITE на users");
        RoleFilter hasWriteUsers = RoleFilters.hasPermission("WRITE", "users");
        roles.stream().filter(hasWriteUsers).forEach(r ->
                System.out.println("  ✓ " + r.name())
        );
    }

    // ==================== ПОДЗАДАЧА 2.3: ФИЛЬТРЫ НАЗНАЧЕНИЙ ====================
    private static void testAssignmentFilters() {
        System.out.println("\n=== ТЕСТ 2.3: Фильтрация назначений ===");

        User user1 = User.validate("user1", "Пользователь 1", "user1@company.com");
        User user2 = User.validate("user2", "Пользователь 2", "user2@company.com");

        Role role1 = new Role("Роль1", "Тестовая роль 1");
        role1.addPermission(new Permission("READ", "data", "Чтение данных"));

        Role role2 = new Role("Роль2", "Тестовая роль 2");
        role2.addPermission(new Permission("WRITE", "data", "Запись данных"));

        AssignmentMetadata meta1 = createMetadata("admin", "Тест", "2026-01-15 10:00");
        AssignmentMetadata meta2 = createMetadata("manager", "Тест", "2026-02-20 14:30");
        AssignmentMetadata meta3 = createMetadata("admin", "Тест", "2026-03-10 09:15");

        PermanentAssignment pa1 = new PermanentAssignment(user1, role1, meta1);
        PermanentAssignment pa2 = new PermanentAssignment(user2, role2, meta2);

        TemporaryAssignment ta1 = new TemporaryAssignment(
                user1,
                role2,
                meta3,
                "2026-02-18 23:59"
        );

        TemporaryAssignment ta2 = new TemporaryAssignment(
                user2,
                role1,
                AssignmentMetadata.now("admin", "Будущее назначение"),
                "2027-12-31 23:59"
        );

        List<RoleAssignment> assignments = Arrays.asList(pa1, pa2, ta1, ta2);

        System.out.println("Все назначения:");
        assignments.forEach(a -> System.out.println("  • [" + a.assignmentType() + "] " +
                a.user().username() + " → " + a.role().name()));

        System.out.println("\n→ Фильтр: только активные назначения");
        AssignmentFilter activeFilter = AssignmentFilters.activeOnly();
        assignments.stream().filter(activeFilter).forEach(a ->
                System.out.println("  ✓ [" + a.assignmentType() + "] " + a.summary().split("\n")[0])
        );

        System.out.println("\n→ Фильтр: только временные назначения");
        AssignmentFilter tempFilter = AssignmentFilters.byType("TEMPORARY");
        assignments.stream().filter(tempFilter).forEach(a ->
                System.out.println("  ✓ " + a.summary().split("\n")[0] + " | Истекает: " +
                        ((TemporaryAssignment) a).expiresAt())
        );

        System.out.println("\n→ Фильтр: назначенные администратором");
        AssignmentFilter adminFilter = AssignmentFilters.assignedBy("admin");
        assignments.stream().filter(adminFilter).forEach(a ->
                System.out.println("  ✓ " + a.user().username() + " → " + a.role().name())
        );
    }

    // ==================== ПОДЗАДАЧА 2.4: СОРТИРОВКА ====================
    private static void testSorting() {
        System.out.println("\n=== ТЕСТ 2.4: Сортировка ===");

        List<User> users = Arrays.asList(
                User.validate("zebra", "Зебра", "z@company.com"),
                User.validate("alpha", "Альфа", "a@company.com"),
                User.validate("mike", "Майк", "m@company.com")
        );

        System.out.println("→ Сортировка пользователей по username:");
        users.stream()
                .sorted(UserSorters.byUsername())
                .forEach(u -> System.out.println("  ✓ " + u.username()));

        Role r1 = new Role("Роль C", "Описание");
        r1.addPermission(new Permission("READ", "x", "X"));
        r1.addPermission(new Permission("WRITE", "x", "X"));

        Role r2 = new Role("Роль A", "Описание");
        r2.addPermission(new Permission("READ", "y", "Y"));

        Role r3 = new Role("Роль B", "Описание");
        r3.addPermission(new Permission("READ", "z", "Z"));
        r3.addPermission(new Permission("WRITE", "z", "Z"));
        r3.addPermission(new Permission("DELETE", "z", "Z"));

        List<Role> roles = Arrays.asList(r1, r2, r3);

        System.out.println("\n→ Сортировка ролей по количеству прав (убывание):");
        roles.stream()
                .sorted(RoleSorters.byPermissionCount().reversed())
                .forEach(r -> System.out.println("  ✓ " + r.name() + " (" + r.getPermissions().size() + " прав)"));

        AssignmentMetadata m1 = createMetadata("admin", "1", "2026-01-10 08:00");
        AssignmentMetadata m2 = createMetadata("admin", "2", "2026-03-15 12:00");
        AssignmentMetadata m3 = createMetadata("admin", "3", "2026-02-20 10:30");

        Role dummyRole = new Role("Dummy", "Dummy");
        User dummyUser = User.validate("dummy", "Dummy", "dummy@x.com");

        List<RoleAssignment> assignments = Arrays.asList(
                new PermanentAssignment(dummyUser, dummyRole, m1),
                new PermanentAssignment(dummyUser, dummyRole, m2),
                new PermanentAssignment(dummyUser, dummyRole, m3)
        );

        System.out.println("\n→ Сортировка назначений по дате назначения:");
        assignments.stream()
                .sorted(AssignmentSorters.byAssignmentDate())
                .forEach(a -> System.out.println("  ✓ " + a.metadata().assignedAt()));
    }

    // ==================== ПОДЗАДАЧА 3: МЕНЕДЖЕРЫ ДАННЫХ ====================
    private static void testManagers() {
        System.out.println("=== ТЕСТ 3.2: Менеджер пользователей ===");
        UserManager userManager = new UserManager();

        // Добавление пользователей
        userManager.add(User.validate("user1", "Пользователь 1", "user1@company.com"));
        userManager.add(User.validate("user2", "Пользователь 2", "user2@company.com"));
        userManager.add(User.validate("admin", "Администратор", "admin@company.com"));

        System.out.println("✓ Добавлено пользователей: " + userManager.count());

        // Поиск по фильтру
        UserFilter companyFilter = UserFilters.byEmailDomain("@company.com");
        List<User> filtered = userManager.findByFilter(companyFilter);
        System.out.println("✓ Найдено пользователей с доменом @company.com: " + filtered.size());

        // Сортировка
        List<User> sorted = userManager.findAll(null, UserSorters.byUsername());
        System.out.println("✓ Отсортированные пользователи по username:");
        sorted.forEach(u -> System.out.println("  • " + u.username()));

        System.out.println("\n=== ТЕСТ 3.3: Менеджер ролей ===");
        RoleManager roleManager = new RoleManager();

        // Создание ролей
        Role adminRole = new Role("Администратор", "Полный доступ");
        adminRole.addPermission(new Permission("READ", "users", "Чтение пользователей"));
        adminRole.addPermission(new Permission("WRITE", "users", "Запись пользователей"));
        roleManager.add(adminRole);

        Role viewerRole = new Role("Наблюдатель", "Только просмотр");
        viewerRole.addPermission(new Permission("READ", "reports", "Просмотр отчётов"));
        roleManager.add(viewerRole);

        System.out.println("✓ Добавлено ролей: " + roleManager.count());

        // Поиск ролей с правом
        List<Role> rolesWithRead = roleManager.findRolesWithPermission("READ", "users");
        System.out.println("✓ Ролей с правом READ на users: " + rolesWithRead.size());

        System.out.println("\n=== ТЕСТ 3.4: Менеджер назначений ===");
        AssignmentManager assignmentManager = new AssignmentManager();

        // Создание назначений
        AssignmentMetadata meta1 = AssignmentMetadata.now("system", "Тестовое назначение");
        PermanentAssignment pa = new PermanentAssignment(
                User.validate("testuser", "Тестовый Пользователь", "test@company.com"),
                adminRole,
                meta1
        );
        assignmentManager.add(pa);

        System.out.println("✓ Добавлено назначений: " + assignmentManager.count());

        // Проверка прав пользователя
        boolean hasPermission = assignmentManager.userHasPermission(
                User.validate("testuser", "Тестовый Пользователь", "test@company.com"),
                "READ",
                "users"
        );
        System.out.println("✓ Пользователь имеет право READ на users: " + (hasPermission ? "Да" : "Нет"));

        // Получение всех прав пользователя
        Set<Permission> permissions = assignmentManager.getUserPermissions(
                User.validate("testuser", "Тестовый Пользователь", "test@company.com")
        );
        System.out.println("✓ Всего прав у пользователя: " + permissions.size());
    }

    // ==================== ПОДЗАДАЧА 5: УТИЛИТЫ ====================
    private static void testUtilities() {
        System.out.println("=== ТЕСТ 5.1: Система валидации (ValidationUtils) ===");
        System.out.println("✓ Валидный username 'john_doe': " + ValidationUtils.isValidUsername("john_doe"));
        System.out.println("✗ Невалидный username 'ab': " + ValidationUtils.isValidUsername("ab"));
        System.out.println("✓ Валидный email 'test@example.com': " + ValidationUtils.isValidEmail("test@example.com"));
        System.out.println("✓ Нормализация строки '  Тест  ': '" + ValidationUtils.normalizeString("  Тест  ") + "'");

        System.out.println("\n=== ТЕСТ 5.2: Система логирования (AuditLog) ===");
        AuditLog auditLog = new AuditLog();
        auditLog.log("USER_CREATE", "admin", "user1", "Создан новый пользователь");
        auditLog.log("ROLE_ASSIGN", "admin", "user1", "Назначена роль Администратор");
        System.out.println("✓ Записей в журнале аудита: " + auditLog.getAll().size());
        auditLog.printLog();

        System.out.println("\n=== ТЕСТ 5.3: Генерация отчётов (ReportGenerator) ===");
        // Инициализация данных для отчёта
        UserManager userManager = new UserManager();
        RoleManager roleManager = new RoleManager();
        AssignmentManager assignmentManager = new AssignmentManager();

        userManager.add(User.validate("user1", "Пользователь 1", "user1@company.com"));
        userManager.add(User.validate("user2", "Пользователь 2", "user2@company.com"));

        Role role = new Role("Тестовая Роль", "Описание");
        role.addPermission(new Permission("READ", "data", "Чтение данных"));
        roleManager.add(role);

        assignmentManager.add(new PermanentAssignment(
                User.validate("user1", "Пользователь 1", "user1@company.com"),
                role,
                AssignmentMetadata.now("admin", "Тест")
        ));

        ReportGenerator reportGenerator = new ReportGenerator();
        String userReport = reportGenerator.generateUserReport(userManager, assignmentManager);
        System.out.println("✓ Отчёт по пользователям сгенерирован (первые 100 символов):");
        System.out.println(userReport.substring(0, Math.min(100, userReport.length())) + "...");

        System.out.println("\n=== ТЕСТ 5.4: Интерактивные утилиты (ConsoleUtils) ===");
        // Демонстрация методов без реального ввода (для автоматической демонстрации)
        System.out.println("✓ ConsoleUtils поддерживает:");
        System.out.println("  • promptString() — запрос строки");
        System.out.println("  • promptInt() — запрос числа в диапазоне");
        System.out.println("  • promptYesNo() — запрос подтверждения");
        System.out.println("  • promptChoice() — выбор из списка");

        System.out.println("\n=== ТЕСТ 5.5: Форматирование (FormatUtils) ===");
        String[] headers = {"Username", "Email"};
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"admin", "admin@company.com"});
        rows.add(new String[]{"user1", "user1@company.com"});

        String table = FormatUtils.formatTable(headers, rows);
        System.out.println("✓ Сгенерирована таблица:");
        System.out.println(table.substring(0, Math.min(150, table.length())) + "...");
    }

    // ==================== ПОДЗАДАЧА 4: ИНТЕРАКТИВНЫЙ РЕЖИМ ====================
    private static void startInteractiveMode(Scanner scanner) {
        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║    ИНТЕРАКТИВНЫЙ РЕЖИМ УПРАВЛЕНИЯ СИСТЕМОЙ RBAC (ПОДЗАДАЧА 4) ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // Создание системы
        RBACSystem system = new RBACSystem();

        // Инициализация с начальными данными
        system.initialize();

        // Создание парсера команд
        CommandParser parser = new CommandParser();

        // Регистрация всех команд
        CommandRegistry.registerAllCommands(parser);

        // Установка текущего пользователя
        system.setCurrentUser("admin");

        System.out.println("Система готова к работе!\n");
        parser.printHelp();

        // Основной цикл работы системы
        while (true) {
            try {
                System.out.print("\nRBAC> ");
                String input = scanner.nextLine();

                if (input.trim().isEmpty()) {
                    continue;
                }

                // Выход из программы
                if (input.trim().equalsIgnoreCase("exit")) {
                    System.out.print("Подтвердите выход (да/нет): ");
                    String confirmation = scanner.nextLine().trim();
                    if (confirmation.equalsIgnoreCase("да") || confirmation.equalsIgnoreCase("yes") || confirmation.equalsIgnoreCase("y")) {
                        System.out.println("\nВыход из системы. До свидания!");
                        break;
                    }
                    continue;
                }

                // Парсинг и выполнение команды
                parser.parseAndExecute(input, scanner, system);

            } catch (Exception e) {
                System.out.println("✗ Критическая ошибка: " + e.getMessage());
                e.printStackTrace();
            }
        }

        scanner.close();
    }
}