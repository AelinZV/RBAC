package rbac;

// ✅ ДОБАВЛЕНЫ НЕОБХОДИМЫЕ ИМПОРТЫ
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;  // ✅ Этот импорт отсутствовал!
import java.util.concurrent.TimeUnit;

/**
 * Основная система RBAC с асинхронными командами (Подзадача 3).
 */
public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private final BackgroundExecutor backgroundExecutor;
    private final AuditLog auditLog;

    private String currentUser;
    private ScheduledFuture<?> periodicTask;  // Теперь компилируется ✅

    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.backgroundExecutor = new BackgroundExecutor();
        this.auditLog = new AuditLog();
        this.currentUser = "system";

        auditLog.log("SYSTEM_START", currentUser, "RBAC", "Система запущена");
    }

    // Геттеры
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }
    public AuditLog getAuditLog() { return auditLog; }

    public void setCurrentUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }
        this.currentUser = username.trim();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    public void initialize() {
        System.out.println("=== Инициализация системы RBAC ===");

        // Создание предустановленных прав
        Permission userRead = new Permission("USER_READ", "users", "Чтение пользователей");
        Permission userWrite = new Permission("USER_WRITE", "users", "Запись пользователей");
        Permission userDelete = new Permission("USER_DELETE", "users", "Удаление пользователей");
        Permission roleRead = new Permission("ROLE_READ", "roles", "Чтение ролей");
        Permission roleWrite = new Permission("ROLE_WRITE", "roles", "Запись ролей");
        Permission assignRead = new Permission("ASSIGN_READ", "assignments", "Чтение назначений");
        Permission assignWrite = new Permission("ASSIGN_WRITE", "assignments", "Запись назначений");

        // Создание ролей
        System.out.println("→ Создание ролей...");

        Role adminRole = new Role("Администратор", "Полный доступ ко всем функциям системы");
        adminRole.addPermission(userRead);
        adminRole.addPermission(userWrite);
        adminRole.addPermission(userDelete);
        adminRole.addPermission(roleRead);
        adminRole.addPermission(roleWrite);
        adminRole.addPermission(assignRead);
        adminRole.addPermission(assignWrite);
        roleManager.add(adminRole);

        Role managerRole = new Role("Менеджер", "Управление пользователями и назначениями");
        managerRole.addPermission(userRead);
        managerRole.addPermission(userWrite);
        managerRole.addPermission(assignRead);
        managerRole.addPermission(assignWrite);
        roleManager.add(managerRole);

        Role viewerRole = new Role("Наблюдатель", "Только просмотр данных");
        viewerRole.addPermission(userRead);
        viewerRole.addPermission(roleRead);
        viewerRole.addPermission(assignRead);
        roleManager.add(viewerRole);

        // Создание тестового администратора
        System.out.println("→ Создание администратора...");
        try {
            User admin = User.validate("admin", "Администратор Системы", "admin@rbac.local");
            userManager.add(admin);

            AssignmentMetadata meta = AssignmentMetadata.now("system", "Инициализация системы");
            PermanentAssignment adminAssignment = new PermanentAssignment(admin, adminRole, meta);
            assignmentManager.add(adminAssignment);

            System.out.println("✓ Администратор создан и назначен роль 'Администратор'");
        } catch (Exception e) {
            System.out.println("✗ Ошибка при создании администратора: " + e.getMessage());
        }

        System.out.println("=== Инициализация завершена ===");
        System.out.println("Создано ролей: " + roleManager.count());
        System.out.println("Создано пользователей: " + userManager.count());
        System.out.println("Создано назначений: " + assignmentManager.count());
    }

    // ==================== Async команды (Подзадача 3) ====================

    /**
     * report-users-async: генерация отчёта в отдельном потоке.
     */
    public CompletableFuture<String> reportUsersAsync() {
        auditLog.log("REPORT_ASYNC", currentUser, "ReportGenerator", "Запущена генерация отчёта");

        return backgroundExecutor.submitAsync(() -> {
            long start = System.currentTimeMillis();
            ReportGenerator generator = new ReportGenerator();
            String report = generator.generateUserReport(userManager, assignmentManager);
            long elapsed = System.currentTimeMillis() - start;

            auditLog.log("REPORT_COMPLETE", currentUser, "ReportGenerator",
                    String.format("Отчёт сгенерирован за %d мс", elapsed));

            return report;
        });
    }

    /**
     * save-async: сохранение данных в файл в фоне.
     */
    public CompletableFuture<Void> saveAsync(String filename) {
        auditLog.log("SAVE_ASYNC", currentUser, "FileSystem", "Начато сохранение в " + filename);

        return backgroundExecutor.runAsync(() -> {
            try {
                long start = System.currentTimeMillis();

                StringBuilder data = new StringBuilder();
                data.append("=== RBAC Data Export ===\n");
                data.append("Users: ").append(userManager.count()).append("\n");
                data.append("Roles: ").append(roleManager.count()).append("\n");
                data.append("Assignments: ").append(assignmentManager.count()).append("\n");
                data.append("Timestamp: ").append(java.time.LocalDateTime.now()).append("\n");

                Files.writeString(Path.of(filename), data.toString());

                long elapsed = System.currentTimeMillis() - start;
                auditLog.log("SAVE_COMPLETE", currentUser, "FileSystem",
                        String.format("Данные сохранены в %s за %d мс", filename, elapsed));

            } catch (IOException e) {
                auditLog.log("SAVE_ERROR", currentUser, "FileSystem", "Ошибка: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Периодическая задача: деактивация истёкших назначений.
     */
    public void startPeriodicTasks(int intervalSeconds) {
        auditLog.log("PERIODIC_START", currentUser, "Scheduler",
                String.format("Запущена периодическая задача (интервал: %d сек)", intervalSeconds));

        periodicTask = backgroundExecutor.scheduleAtFixedRate(() -> {
            try {
                int expiredCount = assignmentManager.deactivateExpired();
                int userCount = userManager.count();
                int roleCount = roleManager.count();
                int assignmentCount = assignmentManager.count();
                int activeCount = assignmentManager.getActiveAssignments().size();

                if (expiredCount > 0) {
                    auditLog.log("EXPIRED_ASSIGNMENTS", "system", "AssignmentManager",
                            String.format("Деактивировано назначений: %d", expiredCount));
                }

                auditLog.log("STATISTICS", "system", "RBACSystem",
                        String.format("Статистика: Пользователей=%d, Ролей=%d, Назначений=%d (активных: %d)",
                                userCount, roleCount, assignmentCount, activeCount));

            } catch (Exception e) {
                auditLog.log("PERIODIC_ERROR", "system", "Scheduler", "Ошибка: " + e.getMessage());
            }
        }, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public void stopPeriodicTasks() {
        if (periodicTask != null) {
            periodicTask.cancel(false);
            auditLog.log("PERIODIC_STOP", currentUser, "Scheduler", "Периодическая задача остановлена");
        }
    }

    public String generateStatistics() {
        int userCount = userManager.count();
        int roleCount = roleManager.count();
        int assignmentCount = assignmentManager.count();
        int activeAssignments = assignmentManager.getActiveAssignments().size();

        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    СТАТИСТИКА СИСТЕМЫ RBAC                     ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");
        sb.append(" Основные метрики:\n");
        sb.append("  • Пользователей: ").append(userCount).append("\n");
        sb.append("  • Ролей: ").append(roleCount).append("\n");
        sb.append("  • Назначений всего: ").append(assignmentCount).append("\n");
        sb.append("    - Активных: ").append(activeAssignments).append("\n");
        sb.append("    - Истёкших: ").append(assignmentCount - activeAssignments).append("\n\n");
        sb.append("══════════════════════════════════════════════════════════════════\n");
        return sb.toString();
    }

    /**
     * Корректное завершение работы системы.
     */
    public void shutdown() {
        auditLog.log("SYSTEM_SHUTDOWN", currentUser, "RBAC", "Завершение работы");
        stopPeriodicTasks();
        auditLog.shutdown();
        backgroundExecutor.shutdown();
    }
}