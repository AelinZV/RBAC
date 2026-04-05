package rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Генератор отчётов с использованием parallelStream (Подзадача 2).
 */
public class ReportGenerator {

    /**
     *  Подзадача 2: Отчёт по пользователям с parallelStream.
     */
    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ                      ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        List<User> users = userManager.findAll();
        sb.append("Всего пользователей: ").append(users.size()).append("\n\n");

        //  parallelStream для параллельной обработки пользователей
        List<String> userReports = users.parallelStream()
                .map(user -> {
                    StringBuilder userBlock = new StringBuilder();
                    userBlock.append("Пользователь: ").append(user.username()).append("\n");
                    userBlock.append("  Полное имя: ").append(user.fullName()).append("\n");
                    userBlock.append("  Email: ").append(user.email()).append("\n");

                    // Получение назначений пользователя
                    List<RoleAssignment> assignments = assignmentManager.findByUser(user);
                    userBlock.append("  Назначено ролей: ").append(assignments.size()).append("\n");

                    if (!assignments.isEmpty()) {
                        userBlock.append("  Роли:\n");
                        for (RoleAssignment assignment : assignments) {
                            userBlock.append(String.format("    • %s [%s] - %s\n",
                                    assignment.role().name(),
                                    assignment.assignmentType(),
                                    assignment.isActive() ? "Активно" : "Неактивно"));
                        }
                    }

                    // Получение всех прав пользователя
                    Set<Permission> permissions = assignmentManager.getUserPermissions(user);
                    userBlock.append("  Всего прав: ").append(permissions.size()).append("\n");

                    if (!permissions.isEmpty()) {
                        Map<String, List<Permission>> byResource = permissions.parallelStream()
                                .collect(Collectors.groupingBy(Permission::resource));

                        userBlock.append("  Права по ресурсам:\n");
                        byResource.forEach((resource, perms) -> {
                            userBlock.append("    ").append(resource).append(":\n");
                            perms.forEach(perm ->
                                    userBlock.append("      - ").append(perm.name())
                                            .append(": ").append(perm.description()).append("\n"));
                        });
                    }
                    userBlock.append("\n");
                    return userBlock.toString();
                })
                .collect(Collectors.toList());

        userReports.forEach(sb::append);
        return sb.toString();
    }

    /**
     *  Подзадача 2: Отчёт по ролям с parallelStream.
     */
    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ОТЧЁТ ПО РОЛЯМ                              ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        List<Role> roles = roleManager.findAll();
        sb.append("Всего ролей: ").append(roles.size()).append("\n\n");

        // ✏️ parallelStream для параллельной обработки ролей
        List<String> roleReports = roles.parallelStream()
                .map(role -> {
                    StringBuilder roleBlock = new StringBuilder();
                    roleBlock.append("Роль: ").append(role.name()).append(" [ID: ").append(role.id()).append("]\n");
                    roleBlock.append("  Описание: ").append(role.description()).append("\n");
                    roleBlock.append("  Прав доступа: ").append(role.getPermissions().size()).append("\n");

                    if (!role.getPermissions().isEmpty()) {
                        roleBlock.append("  Права:\n");
                        role.getPermissions().forEach(perm ->
                                roleBlock.append(String.format("    • %s на %s: %s\n",
                                        perm.name(), perm.resource(), perm.description())));
                    }

                    // Подсчёт пользователей с этой ролью (параллельно)
                    long userCount = assignmentManager.findAll().parallelStream()
                            .filter(a -> a.role().id().equals(role.id()) && a.isActive())
                            .map(RoleAssignment::user)
                            .distinct()
                            .count();

                    roleBlock.append("  Назначена пользователям: ").append(userCount).append("\n");
                    roleBlock.append("\n");
                    return roleBlock.toString();
                })
                .collect(Collectors.toList());

        roleReports.forEach(sb::append);
        return sb.toString();
    }

    /**
     * ️ Подзадача 2: Матрица прав с parallelStream.
     */
    public String generatePermissionMatrix(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    МАТРИЦА ПРАВ                                ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        List<User> users = userManager.findAll();
        if (users.isEmpty()) {
            sb.append("Нет пользователей для отображения матрицы\n");
            return sb.toString();
        }

        // ✏️ parallelStream для сбора ресурсов
        Set<String> resources = users.parallelStream()
                .flatMap(user -> assignmentManager.getUserPermissions(user).stream())
                .map(Permission::resource)
                .collect(Collectors.toCollection(TreeSet::new));

        if (resources.isEmpty()) {
            sb.append("Нет прав доступа для отображения\n");
            return sb.toString();
        }

        sb.append("Матрица прав (Пользователи × Ресурсы)\n\n");
        sb.append(String.format("%-20s", "Пользователь"));

        List<String> resourceList = new ArrayList<>(resources);
        resourceList.forEach(resource -> sb.append(String.format("%-15s", resource)));
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────────────────────\n");

        // ✏️ parallelStream для построения строк матрицы
        List<String> matrixRows = users.parallelStream()
                .map(user -> {
                    StringBuilder row = new StringBuilder(String.format("%-20s", user.username()));
                    Set<String> userResources = assignmentManager.getUserPermissions(user).parallelStream()
                            .map(Permission::resource)
                            .collect(Collectors.toSet());

                    for (String resource : resourceList) {
                        row.append(String.format("%-15s", userResources.contains(resource) ? "✓" : "✗"));
                    }
                    return row.toString();
                })
                .collect(Collectors.toList());

        matrixRows.forEach(row -> sb.append(row).append("\n"));
        sb.append("\nЛегенда: ✓ - есть доступ, ✗ - нет доступа\n");

        return sb.toString();
    }

    public void exportToFile(String report, String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(report);
            System.out.println("✓ Отчёт сохранён в файл: " + filename);
        } catch (IOException e) {
            System.out.println("✗ Ошибка при сохранении отчёта: " + e.getMessage());
        }
    }
}