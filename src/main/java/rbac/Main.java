package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Демонстрационный класс для тестирования базовых структур данных и фильтров RBAC
 */
public class Main {
    // Форматер для создания метаданных с кастомной датой (только для демонстрации)
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // Вспомогательный метод для создания метаданных с заданной датой (для тестов фильтров по дате)
    private static AssignmentMetadata createMetadata(String assignedBy, String reason, String date) {
        return new AssignmentMetadata(assignedBy, date, reason);
    }

    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║        ДЕМОНСТРАЦИЯ RBAC: БАЗОВЫЕ СТРУКТУРЫ И ФИЛЬТРЫ         ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝\n");

        // ==================== ТЕСТЫ ПОДЗАДАЧИ 1 ====================
        testBaseStructures();

        // ==================== ТЕСТЫ ПОДЗАДАЧИ 2 ====================
        testUserFilters();
        testRoleFilters();
        testAssignmentFilters();
        testSorting();

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    ДЕМОНСТРАЦИЯ ЗАВЕРШЕНА                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
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

        // Создаём тестовые данные
        List<User> users = Arrays.asList(
                User.validate("alex_smith", "Александр Смит", "alex@company.com"),
                User.validate("anna_jones", "Анна Джонс", "anna@company.com"),
                User.validate("bob_wilson", "Боб Уилсон", "bob@external.org"),
                User.validate("admin", "Администратор", "admin@company.com")
        );

        System.out.println("Все пользователи:");
        users.forEach(u -> System.out.println("  • " + u.username()));

        // Тест фильтра по домену
        System.out.println("\n→ Фильтр: email заканчивается на '@company.com'");
        UserFilter companyFilter = UserFilters.byEmailDomain("@company.com");
        users.stream().filter(companyFilter).forEach(u ->
                System.out.println("  ✓ " + u.format())
        );

        // Тест комбинирования фильтров
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

        // Создаём тестовые роли
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

        // Тест фильтра по количеству прав
        System.out.println("\n→ Фильтр: роли с минимум 2 правами");
        RoleFilter min2Permissions = RoleFilters.hasAtLeastNPermissions(2);
        roles.stream().filter(min2Permissions).forEach(r ->
                System.out.println("  ✓ " + r.name() + " (" + r.getPermissions().size() + " прав)")
        );

        // Тест фильтра по наличию права
        System.out.println("\n→ Фильтр: роли с правом WRITE на users");
        RoleFilter hasWriteUsers = RoleFilters.hasPermission("WRITE", "users");
        roles.stream().filter(hasWriteUsers).forEach(r ->
                System.out.println("  ✓ " + r.name())
        );
    }

    // ==================== ПОДЗАДАЧА 2.3: ФИЛЬТРЫ НАЗНАЧЕНИЙ ====================
    private static void testAssignmentFilters() {
        System.out.println("\n=== ТЕСТ 2.3: Фильтрация назначений ===");

        // Создаём пользователей и роли
        User user1 = User.validate("user1", "Пользователь 1", "user1@company.com");
        User user2 = User.validate("user2", "Пользователь 2", "user2@company.com");

        Role role1 = new Role("Роль1", "Тестовая роль 1");
        role1.addPermission(new Permission("READ", "data", "Чтение данных"));

        Role role2 = new Role("Роль2", "Тестовая роль 2");
        role2.addPermission(new Permission("WRITE", "data", "Запись данных"));

        // Создаём назначения с разными датами
        AssignmentMetadata meta1 = createMetadata("admin", "Тест", "2026-01-15 10:00");
        AssignmentMetadata meta2 = createMetadata("manager", "Тест", "2026-02-20 14:30");
        AssignmentMetadata meta3 = createMetadata("admin", "Тест", "2026-03-10 09:15");

        PermanentAssignment pa1 = new PermanentAssignment(user1, role1, meta1);
        PermanentAssignment pa2 = new PermanentAssignment(user2, role2, meta2);

        // Временное назначение, истекающее скоро
        TemporaryAssignment ta1 = new TemporaryAssignment(
                user1,
                role2,
                meta3,
                "2026-02-18 23:59" // Истекает до текущей даты в тесте
        );

        // Временное назначение с будущим сроком
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

        // Тест фильтра активных назначений
        System.out.println("\n→ Фильтр: только активные назначения");
        AssignmentFilter activeFilter = AssignmentFilters.activeOnly();
        assignments.stream().filter(activeFilter).forEach(a ->
                System.out.println("  ✓ [" + a.assignmentType() + "] " + a.summary().split("\n")[0])
        );

        // Тест фильтра по типу
        System.out.println("\n→ Фильтр: только временные назначения");
        AssignmentFilter tempFilter = AssignmentFilters.byType("TEMPORARY");
        assignments.stream().filter(tempFilter).forEach(a ->
                System.out.println("  ✓ " + a.summary().split("\n")[0] + " | Истекает: " +
                        ((TemporaryAssignment) a).expiresAt())
        );

        // Тест фильтра по назначившему
        System.out.println("\n→ Фильтр: назначенные администратором");
        AssignmentFilter adminFilter = AssignmentFilters.assignedBy("admin");
        assignments.stream().filter(adminFilter).forEach(a ->
                System.out.println("  ✓ " + a.user().username() + " → " + a.role().name())
        );
    }

    // ==================== ПОДЗАДАЧА 2.4: СОРТИРОВКА ====================
    private static void testSorting() {
        System.out.println("\n===  ТЕСТ 2.4: Сортировка ===");

        // Пользователи для сортировки
        List<User> users = Arrays.asList(
                User.validate("zebra", "Зебра", "z@company.com"),
                User.validate("alpha", "Альфа", "a@company.com"),
                User.validate("mike", "Майк", "m@company.com")
        );

        System.out.println("→ Сортировка пользователей по username:");
        users.stream()
                .sorted(UserSorters.byUsername())
                .forEach(u -> System.out.println("  ✓ " + u.username()));

        // Роли для сортировки
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

        // Назначения для сортировки
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
}