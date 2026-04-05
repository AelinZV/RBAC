package rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Потокoбезопасный менеджер ролей.
 */
public class RoleManager {

    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(Role role) {
        lock.writeLock().lock();
        try {
            if (roles.containsKey(role.name())) {
                throw new IllegalArgumentException("Role already exists: " + role.name());
            }
            roles.put(role.name(), role);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean exists(String roleName) {
        lock.readLock().lock();
        try {
            return roles.containsKey(roleName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Role> findByName(String roleName) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(roles.get(roleName));
        } finally {
            lock.readLock().unlock();
        }
    }

    // ✅ Оригинал: с параметрами
    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        lock.readLock().lock();
        try {
            List<Role> result = new ArrayList<>(roles.values());
            if (filter != null) {
                result = result.stream().filter(filter).collect(Collectors.toList());
            }
            if (sorter != null) {
                result.sort(sorter);
            }
            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    // ✅ НОВЫЙ: без параметров
    public List<Role> findAll() {
        return findAll(null, null);
    }

    // ✅ НОВЫЙ: только фильтр
    public List<Role> findByFilter(RoleFilter filter) {
        return findAll(filter, null);
    }

    // ✅ НОВЫЙ: параллельная фильтрация
    public List<Role> findByFilterParallel(RoleFilter filter) {
        return roles.values().parallelStream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public void addPermissionToRole(String roleName, Permission permission) {
        lock.writeLock().lock();
        try {
            Role role = roles.get(roleName);
            if (role != null) {
                role.addPermission(permission);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Role> findRolesWithPermission(String action, String resource) {
        lock.readLock().lock();
        try {
            return roles.values().stream()
                    .filter(role -> role.hasPermission(action, resource))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean remove(Role role) {
        lock.writeLock().lock();
        try {
            return roles.remove(role.name()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int count() {
        return roles.size();
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            roles.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}