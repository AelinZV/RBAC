package rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Потокoбезопасный менеджер назначений ролей.
 */
public class AssignmentManager {

    private final Map<String, RoleAssignment> assignments = new ConcurrentHashMap<>();
    private final Map<String, String> userRoleIndex = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String makeUserRoleKey(String username, String roleName) {
        return username + ":" + roleName;
    }

    public void add(RoleAssignment assignment) {
        lock.writeLock().lock();
        try {
            String userRoleKey = makeUserRoleKey(
                    assignment.user().username(),
                    assignment.role().name()
            );
            if (userRoleIndex.containsKey(userRoleKey)) {
                throw new IllegalArgumentException(
                        "User " + assignment.user().username() +
                                " already has role " + assignment.role().name());
            }
            assignments.put(assignment.assignmentId(), assignment);
            userRoleIndex.put(userRoleKey, assignment.assignmentId());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<RoleAssignment> findById(String assignmentId) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(assignments.get(assignmentId));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findByUser(User user) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().username().equals(user.username()))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findAll(AssignmentFilter filter, Comparator<RoleAssignment> sorter) {
        lock.readLock().lock();
        try {
            List<RoleAssignment> result = new ArrayList<>(assignments.values());
            if (filter != null) result = result.stream().filter(filter).collect(Collectors.toList());
            if (sorter != null) result.sort(sorter);
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<RoleAssignment> findAll() { return findAll(null, null); }
    public List<RoleAssignment> findByFilter(AssignmentFilter filter) { return findAll(filter, null); }

    public List<RoleAssignment> findByFilterParallel(AssignmentFilter filter) {
        return assignments.values().parallelStream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<RoleAssignment> getActiveAssignments() {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(RoleAssignment::isActive)
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasRole(User user, Role role) {
        lock.readLock().lock();
        try {
            String key = makeUserRoleKey(user.username(), role.name());
            String id = userRoleIndex.get(key);
            if (id != null) {
                RoleAssignment a = assignments.get(id);
                return a != null && a.isActive();
            }
            return false;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean userHasPermission(User user, String action, String resource) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().username().equals(user.username()) && a.isActive())
                    .anyMatch(a -> a.role().hasPermission(action, resource));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Set<Permission> getUserPermissions(User user) {
        lock.readLock().lock();
        try {
            return assignments.values().stream()
                    .filter(a -> a.user().username().equals(user.username()) && a.isActive())
                    .flatMap(a -> a.role().getPermissions().stream())
                    .collect(Collectors.toSet());
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Деактивация истёкших временных назначений.
     * Использует метод isExpired() из TemporaryAssignment.
     *
     * Важно: isExpired() возвращает true, когда назначение истекло,
     * а isActive() возвращает false в этом случае.
     * Поэтому проверяем только isExpired(), без isActive().
     */
    public int deactivateExpired() {
        lock.writeLock().lock();
        try {
            List<String> toRemove = new ArrayList<>();

            for (Map.Entry<String, RoleAssignment> entry : assignments.entrySet()) {
                RoleAssignment a = entry.getValue();

                // Проверяем, что это временное назначение
                if (!isTemporaryAssignment(a)) continue;

                // ✅ Вызываем isExpired() — если true, значит назначение истекло
                Boolean expired = callIsExpired(a);
                if (Boolean.TRUE.equals(expired)) {
                    toRemove.add(entry.getKey());
                }
            }

            // Удаляем помеченные назначения
            for (String id : toRemove) {
                RoleAssignment removed = assignments.remove(id);
                if (removed != null) {
                    String key = makeUserRoleKey(
                            removed.user().username(),
                            removed.role().name()
                    );
                    userRoleIndex.remove(key);
                }
            }

            return toRemove.size();

        } finally {
            lock.writeLock().unlock();
        }
    }

    private boolean isTemporaryAssignment(RoleAssignment a) {
        String name = a.getClass().getSimpleName();
        return name.contains("Temporary") || a.getClass().getName().contains("TemporaryAssignment");
    }

    /**
     * Вызов метода isExpired() через рефлексию.
     */
    private Boolean callIsExpired(RoleAssignment a) {
        try {
            java.lang.reflect.Method method = a.getClass().getMethod("isExpired");
            Object result = method.invoke(a);
            return result instanceof Boolean ? (Boolean) result : null;
        } catch (Exception e) {
            return null;
        }
    }

    public void revokeAssignment(String assignmentId) {
        lock.writeLock().lock();
        try {
            RoleAssignment removed = assignments.remove(assignmentId);
            if (removed != null) {
                String key = makeUserRoleKey(removed.user().username(), removed.role().name());
                userRoleIndex.remove(key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void extendTemporaryAssignment(String assignmentId, String newExpiresAt) {}

    public boolean remove(RoleAssignment assignment) {
        lock.writeLock().lock();
        try {
            String id = assignment.assignmentId();
            RoleAssignment removed = assignments.remove(id);
            if (removed != null) {
                String key = makeUserRoleKey(removed.user().username(), removed.role().name());
                userRoleIndex.remove(key);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int count() { return assignments.size(); }

    public void clear() {
        lock.writeLock().lock();
        try {
            assignments.clear();
            userRoleIndex.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}