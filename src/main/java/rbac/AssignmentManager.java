package rbac;

import java.util.*;
import java.util.stream.Collectors;


public class AssignmentManager implements Repository<RoleAssignment> {

    // Хранилище назначений: ключ - идентификатор назначения
    private final Map<String, RoleAssignment> assignments = new HashMap<>();

    // Индекс назначений по пользователю для быстрого поиска
    private final Map<User, List<RoleAssignment>> assignmentsByUser = new HashMap<>();

    // Индекс назначений по роли для быстрого поиска
    private final Map<Role, List<RoleAssignment>> assignmentsByRole = new HashMap<>();


    @Override
    public void add(RoleAssignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Назначение не может быть null");
        }

        // Проверяем, не существует ли уже такое назначение
        if (assignments.containsKey(assignment.assignmentId())) {
            throw new IllegalArgumentException("Назначение с идентификатором '" +
                    assignment.assignmentId() + "' уже существует");
        }

        // Проверяем, не назначена ли уже эта роль пользователю (если назначение активно)
        if (assignment.isActive()) {
            boolean alreadyAssigned = assignments.values().stream()
                    .anyMatch(existing ->
                            existing.isActive() &&
                                    existing.user().equals(assignment.user()) &&
                                    existing.role().equals(assignment.role())
                    );

            if (alreadyAssigned) {
                throw new IllegalArgumentException("Роль '" + assignment.role().name() +
                        "' уже назначена пользователю '" + assignment.user().username() + "'");
            }
        }

        // Добавляем назначение в основное хранилище
        assignments.put(assignment.assignmentId(), assignment);

        // Обновляем индексы
        assignmentsByUser.computeIfAbsent(assignment.user(), k -> new ArrayList<>())
                .add(assignment);

        assignmentsByRole.computeIfAbsent(assignment.role(), k -> new ArrayList<>())
                .add(assignment);
    }


    @Override
    public boolean remove(RoleAssignment assignment) {
        if (assignment == null) {
            return false;
        }

        RoleAssignment removed = assignments.remove(assignment.assignmentId());
        if (removed != null) {
            // Удаляем из индексов
            List<RoleAssignment> userAssignments = assignmentsByUser.get(removed.user());
            if (userAssignments != null) {
                userAssignments.remove(removed);
                if (userAssignments.isEmpty()) {
                    assignmentsByUser.remove(removed.user());
                }
            }

            List<RoleAssignment> roleAssignments = assignmentsByRole.get(removed.role());
            if (roleAssignments != null) {
                roleAssignments.remove(removed);
                if (roleAssignments.isEmpty()) {
                    assignmentsByRole.remove(removed.role());
                }
            }

            return true;
        }

        return false;
    }


    @Override
    public Optional<RoleAssignment> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(assignments.get(id));
    }

    /**
     * Получить все назначения из менеджера
     * @return список всех назначений
     */
    @Override
    public List<RoleAssignment> findAll() {
        return new ArrayList<>(assignments.values());
    }

    /**
     * Получить количество назначений в менеджере
     * @return количество назначений
     */
    @Override
    public int count() {
        return assignments.size();
    }


    @Override
    public void clear() {
        assignments.clear();
        assignmentsByUser.clear();
        assignmentsByRole.clear();
    }

    public List<RoleAssignment> findByUser(User user) {
        if (user == null) {
            return new ArrayList<>();
        }

        return assignmentsByUser.getOrDefault(user, new ArrayList<>());
    }


    public List<RoleAssignment> findByRole(Role role) {
        if (role == null) {
            return new ArrayList<>();
        }

        return assignmentsByRole.getOrDefault(role, new ArrayList<>());
    }


    public List<RoleAssignment> findByFilter(AssignmentFilter filter) {
        if (filter == null) {
            return new ArrayList<>(assignments.values());
        }

        return assignments.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }


    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        if (filter == null && sorter == null) {
            return new ArrayList<>(assignments.values());
        }

        List<RoleAssignment> result = assignments.values().stream()
                .filter(filter != null ? filter : assignment -> true)
                .collect(Collectors.toList());

        if (sorter != null) {
            result.sort(sorter);
        }

        return result;
    }


    public List<RoleAssignment> getActiveAssignments() {
        return assignments.values().stream()
                .filter(RoleAssignment::isActive)
                .collect(Collectors.toList());
    }


    public List<RoleAssignment> getExpiredAssignments() {
        return assignments.values().stream()
                .filter(assignment -> !assignment.isActive())
                .collect(Collectors.toList());
    }


    public boolean userHasRole(User user, Role role) {
        if (user == null || role == null) {
            return false;
        }

        return assignments.values().stream()
                .anyMatch(assignment ->
                        assignment.isActive() &&
                                assignment.user().equals(user) &&
                                assignment.role().equals(role)
                );
    }


    public boolean userHasPermission(User user, String permissionName, String resource) {
        if (user == null) {
            return false;
        }

        // Получаем все активные роли пользователя
        Set<Role> userRoles = assignments.values().stream()
                .filter(assignment -> assignment.isActive() && assignment.user().equals(user))
                .map(RoleAssignment::role)
                .collect(Collectors.toSet());

        // Проверяем, есть ли у какой-либо роли указанное право
        return userRoles.stream()
                .anyMatch(role -> role.hasPermission(permissionName, resource));
    }


    public Set<Permission> getUserPermissions(User user) {
        if (user == null) {
            return Collections.emptySet();
        }

        // Собираем все права из всех активных ролей пользователя
        return assignments.values().stream()
                .filter(assignment -> assignment.isActive() && assignment.user().equals(user))
                .map(RoleAssignment::role)
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }


    public void revokeAssignment(String assignmentId) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор назначения не может быть пустым");
        }

        RoleAssignment assignment = assignments.get(assignmentId);

        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с идентификатором '" +
                    assignmentId + "' не найдено");
        }

        // Отзываем назначение (если это постоянное назначение)
        if (assignment instanceof PermanentAssignment permanent) {
            permanent.revoke();
        } else {
            throw new IllegalArgumentException("Назначение не является постоянным и не может быть отозвано");
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpirationDate) {
        if (assignmentId == null || assignmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Идентификатор назначения не может быть пустым");
        }

        if (newExpirationDate == null || newExpirationDate.trim().isEmpty()) {
            throw new IllegalArgumentException("Новая дата истечения не может быть пустой");
        }

        RoleAssignment assignment = assignments.get(assignmentId);

        if (assignment == null) {
            throw new IllegalArgumentException("Назначение с идентификатором '" +
                    assignmentId + "' не найдено");
        }

        // Продлеваем только временное назначение
        if (assignment instanceof TemporaryAssignment temporary) {
            temporary.extend(newExpirationDate);
        } else {
            throw new IllegalArgumentException("Назначение не является временным и не может быть продлено");
        }
    }


    public Map<String, RoleAssignment> getAssignments() {
        return Collections.unmodifiableMap(assignments);
    }
}