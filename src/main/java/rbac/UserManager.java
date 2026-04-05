package rbac;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Потокoбезопасный менеджер пользователей.
 */
public class UserManager {

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void add(User user) {
        lock.writeLock().lock();
        try {
            if (users.containsKey(user.username())) {
                throw new IllegalArgumentException("User already exists: " + user.username());
            }
            users.put(user.username(), user);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean exists(String username) {
        lock.readLock().lock();
        try {
            return users.containsKey(username);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<User> findByUsername(String username) {
        lock.readLock().lock();
        try {
            return Optional.ofNullable(users.get(username));
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<User> findByEmail(String email) {
        lock.readLock().lock();
        try {
            return users.values().stream()
                    .filter(u -> u.email().equalsIgnoreCase(email))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    // ✅ Оригинал: с параметрами (для обратной совместимости)
    public List<User> findAll(UserFilter filter, Comparator<User> sorter) {
        lock.readLock().lock();
        try {
            List<User> result = new ArrayList<>(users.values());
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

    // ✅ НОВЫЙ: без параметров (для ReportGenerator и других)
    public List<User> findAll() {
        return findAll(null, null);
    }

    // ✅ НОВЫЙ: только фильтр (для Main.java:313)
    public List<User> findByFilter(UserFilter filter) {
        return findAll(filter, null);
    }

    // ✅ НОВЫЙ: параллельная фильтрация (требование задания)
    public List<User> findByFilterParallel(UserFilter filter) {
        return users.values().parallelStream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public void update(String username, String newFullName, String newEmail) {
        lock.writeLock().lock();
        try {
            User existing = users.get(username);
            if (existing != null) {
                User updated = User.validate(username, newFullName, newEmail);
                users.put(username, updated);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(User user) {
        lock.writeLock().lock();
        try {
            return users.remove(user.username()) != null;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int count() {
        return users.size();
    }

    public void clear() {
        lock.writeLock().lock();
        try {
            users.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
}