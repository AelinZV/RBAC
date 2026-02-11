package rbac;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Класс роли с управлением правами доступа
 */
public class Role {
    // Счётчик для генерации уникальных ID
    private static final AtomicLong ID_COUNTER = new AtomicLong(0);

    private final String id;
    private final String name;
    private final String description;
    private final Set<Permission> permissions = new HashSet<>();

    /**
     * Конструктор роли с автоматической генерацией ID
     */
    public Role(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым или null");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание роли не может быть пустым или null");
        }
        this.id = "role_" + ID_COUNTER.incrementAndGet();
        this.name = name.trim();
        this.description = description.trim();
    }

    // Геттеры
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String description() {
        return description;
    }

    /**
     * Добавление права доступа к роли
     */
    public void addPermission(Permission permission) {
        if (permission == null) {
            throw new IllegalArgumentException("Право доступа не может быть null");
        }
        permissions.add(permission);
    }

    /**
     * Удаление права доступа из роли
     */
    public void removePermission(Permission permission) {
        permissions.remove(permission);
    }

    /**
     * Проверка наличия конкретного права
     */
    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    /**
     * Проверка наличия права по имени и ресурсу
     */
    public boolean hasPermission(String permissionName, String resource) {
        if (permissionName == null || resource == null) return false;
        String nameUpper = permissionName.trim().toUpperCase();
        String resourceLower = resource.trim().toLowerCase();
        return permissions.stream()
                .anyMatch(p -> p.name().equals(nameUpper) && p.resource().equals(resourceLower));
    }

    /**
     * Получение неизменяемой копии прав доступа
     */
    public Set<Permission> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(id, role.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Role{id='" + id + "', name='" + name + "', permissions=" + permissions.size() + "}";
    }

    /**
     * Форматированный вывод роли со всеми правами
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Роль: ").append(name).append(" [ID: ").append(id).append("]\n");
        sb.append("Описание: ").append(description).append("\n");
        sb.append("Права(").append(permissions.size()).append("):");
        if (permissions.isEmpty()) {
            sb.append(" (отсутствуют)");
        } else {
            for (Permission p : permissions) {
                sb.append("\n- ").append(p.format());
            }
        }
        return sb.toString();
    }
}