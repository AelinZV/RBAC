package rbac;


public class RBACSystem {

    private final UserManager userManager;
    private final RoleManager roleManager;
    private final AssignmentManager assignmentManager;
    private String currentUser; // Текущий администратор системы


    public RBACSystem() {
        this.userManager = new UserManager();
        this.roleManager = new RoleManager();
        this.assignmentManager = new AssignmentManager();
        this.currentUser = "system"; // По умолчанию системный пользователь
    }

    // Геттеры
    public UserManager getUserManager() { return userManager; }
    public RoleManager getRoleManager() { return roleManager; }
    public AssignmentManager getAssignmentManager() { return assignmentManager; }


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

        // Создание предустановленных прав доступа
        Permission userRead = new Permission("USER_READ", "users", "Чтение пользователей");
        Permission userWrite = new Permission("USER_WRITE", "users", "Запись пользователей");
        Permission userDelete = new Permission("USER_DELETE", "users", "Удаление пользователей");

        Permission roleRead = new Permission("ROLE_READ", "roles", "Чтение ролей");
        Permission roleWrite = new Permission("ROLE_WRITE", "roles", "Запись ролей");

        Permission assignRead = new Permission("ASSIGN_READ", "assignments", "Чтение назначений");
        Permission assignWrite = new Permission("ASSIGN_WRITE", "assignments", "Запись назначений");

        // Создание ролей
        System.out.println("→ Создание ролей...");

        // Роль Администратора
        Role adminRole = new Role("Администратор", "Полный доступ ко всем функциям системы");
        adminRole.addPermission(userRead);
        adminRole.addPermission(userWrite);
        adminRole.addPermission(userDelete);
        adminRole.addPermission(roleRead);
        adminRole.addPermission(roleWrite);
        adminRole.addPermission(assignRead);
        adminRole.addPermission(assignWrite);
        roleManager.add(adminRole);

        // Роль Менеджера
        Role managerRole = new Role("Менеджер", "Управление пользователями и назначениями");
        managerRole.addPermission(userRead);
        managerRole.addPermission(userWrite);
        managerRole.addPermission(assignRead);
        managerRole.addPermission(assignWrite);
        roleManager.add(managerRole);

        // Роль Наблюдателя
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

            // Назначение роли Администратора
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
}