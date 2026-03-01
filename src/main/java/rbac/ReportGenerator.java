package rbac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class ReportGenerator {


    public String generateUserReport(UserManager userManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ОТЧЁТ ПО ПОЛЬЗОВАТЕЛЯМ                      ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        List<User> users = userManager.findAll();

        sb.append("Всего пользователей: ").append(users.size()).append("\n\n");

        for (User user : users) {
            sb.append("Пользователь: ").append(user.username()).append("\n");
            sb.append("  Полное имя: ").append(user.fullName()).append("\n");
            sb.append("  Email: ").append(user.email()).append("\n");

            // Получение назначений пользователя
            List<RoleAssignment> assignments = assignmentManager.findByUser(user);
            sb.append("  Назначено ролей: ").append(assignments.size()).append("\n");

            if (!assignments.isEmpty()) {
                sb.append("  Роли:\n");
                for (RoleAssignment assignment : assignments) {
                    sb.append(String.format("    • %s [%s] - %s\n",
                            assignment.role().name(),
                            assignment.assignmentType(),
                            assignment.isActive() ? "Активно" : "Неактивно"));
                }
            }

            // Получение всех прав пользователя
            Set<Permission> permissions = assignmentManager.getUserPermissions(user);
            sb.append("  Всего прав: ").append(permissions.size()).append("\n");

            if (!permissions.isEmpty()) {
                // Группировка прав по ресурсам
                Map<String, List<Permission>> byResource = permissions.stream()
                        .collect(Collectors.groupingBy(Permission::resource));

                sb.append("  Права по ресурсам:\n");
                for (Map.Entry<String, List<Permission>> entry : byResource.entrySet()) {
                    sb.append("    ").append(entry.getKey()).append(":\n");
                    for (Permission perm : entry.getValue()) {
                        sb.append("      - ").append(perm.name()).append(": ").append(perm.description()).append("\n");
                    }
                }
            }

            sb.append("\n");
        }

        return sb.toString();
    }


    public String generateRoleReport(RoleManager roleManager, AssignmentManager assignmentManager) {
        StringBuilder sb = new StringBuilder();
        sb.append("╔════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                    ОТЧЁТ ПО РОЛЯМ                              ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════╝\n\n");

        List<Role> roles = roleManager.findAll();

        sb.append("Всего ролей: ").append(roles.size()).append("\n\n");

        for (Role role : roles) {
            sb.append("Роль: ").append(role.name()).append(" [ID: ").append(role.id()).append("]\n");
            sb.append("  Описание: ").append(role.description()).append("\n");
            sb.append("  Прав доступа: ").append(role.getPermissions().size()).append("\n");

            if (!role.getPermissions().isEmpty()) {
                sb.append("  Права:\n");
                for (Permission perm : role.getPermissions()) {
                    sb.append(String.format("    • %s на %s: %s\n",
                            perm.name(), perm.resource(), perm.description()));
                }
            }

            // Подсчёт пользователей с этой ролью
            long userCount = assignmentManager.findAll().stream()
                    .filter(a -> a.role().id().equals(role.id()) && a.isActive())
                    .map(RoleAssignment::user)
                    .distinct()
                    .count();

            sb.append("  Назначена пользователям: ").append(userCount).append("\n");

            sb.append("\n");
        }

        return sb.toString();
    }


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

        // Сбор всех уникальных ресурсов
        Set<String> resources = new TreeSet<>();
        for (User user : users) {
            Set<Permission> perms = assignmentManager.getUserPermissions(user);
            perms.stream().map(Permission::resource).forEach(resources::add);
        }

        if (resources.isEmpty()) {
            sb.append("Нет прав доступа для отображения\n");
            return sb.toString();
        }

        // Заголовок таблицы
        sb.append("Матрица прав (Пользователи × Ресурсы)\n\n");
        sb.append(String.format("%-20s", "Пользователь"));

        List<String> resourceList = new ArrayList<>(resources);
        for (String resource : resourceList) {
            sb.append(String.format("%-15s", resource));
        }
        sb.append("\n");
        sb.append("─────────────────────────────────────────────────────────────────────────────\n");

        // Строки матрицы
        for (User user : users) {
            sb.append(String.format("%-20s", user.username()));

            Set<Permission> userPerms = assignmentManager.getUserPermissions(user);
            Set<String> userResources = userPerms.stream()
                    .map(Permission::resource)
                    .collect(Collectors.toSet());

            for (String resource : resourceList) {
                sb.append(String.format("%-15s", userResources.contains(resource) ? "✓" : "✗"));
            }
            sb.append("\n");
        }

        sb.append("\n");
        sb.append("Легенда: ✓ - есть доступ, ✗ - нет доступа\n");

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