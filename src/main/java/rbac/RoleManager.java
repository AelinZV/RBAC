package rbac;

import java.util.*;
import java.util.stream.Collectors;


public class RoleManager implements Repository<Role> {

    // Хранилище ролей по идентификатору
    private final Map<String, Role> rolesById = new HashMap<>();

    // Индекс ролей по имени для быстрого поиска
    private final Map<String, Role> rolesByName = new HashMap<>();

    /**
     * Добавить роль в менеджер
     * @param role роль для добавления
     * @throws IllegalArgumentException если роль с таким именем уже существует
     */
    @Override
    public void add(Role role) {
        if (role == null) {
            throw new IllegalArgumentException("Роль не может быть null");
        }

        String roleName = role.name().toLowerCase();

        if (rolesByName.containsKey(roleName)) {
            throw new IllegalArgumentException("Роль с именем '" + role.name() + "' уже существует");
        }

        rolesById.put(role.id(), role);
        rolesByName.put(roleName, role);
    }

    /**
     * Удалить роль из менеджера
     * @param role роль для удаления
     * @return true если роль была удалена, иначе false
     */
    @Override
    public boolean remove(Role role) {
        if (role == null) {
            return false;
        }

        Role removedById = rolesById.remove(role.id());
        if (removedById != null) {
            rolesByName.remove(removedById.name().toLowerCase());
            return true;
        }

        return false;
    }

    /**
     * Найти роль по идентификатору
     * @param id идентификатор роли
     * @return Optional с найденной ролью или пустой Optional
     */
    @Override
    public Optional<Role> findById(String id) {
        if (id == null || id.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(rolesById.get(id));
    }


    @Override
    public List<Role> findAll() {
        return new ArrayList<>(rolesById.values());
    }


    @Override
    public int count() {
        return rolesById.size();
    }


    @Override
    public void clear() {
        rolesById.clear();
        rolesByName.clear();
    }


    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(rolesByName.get(name.toLowerCase()));
    }


    public List<Role> findByFilter(RoleFilter filter) {
        if (filter == null) {
            return new ArrayList<>(rolesById.values());
        }

        return rolesById.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<Role> findAll(RoleFilter filter, Comparator<Role> sorter) {
        if (filter == null && sorter == null) {
            return new ArrayList<>(rolesById.values());
        }

        List<Role> result = rolesById.values().stream()
                .filter(filter != null ? filter : role -> true)
                .collect(Collectors.toList());

        if (sorter != null) {
            result.sort(sorter);
        }

        return result;
    }

    /**
     * Проверить существование роли с указанным именем
     * @param name имя роли
     * @return true если роль существует, иначе false
     */
    public boolean exists(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        return rolesByName.containsKey(name.toLowerCase());
    }

    /**
     * Добавить право доступа к роли
     * @param roleName имя роли
     * @param permission право доступа для добавления
     * @throws IllegalArgumentException если роль не найдена или право уже существует
     */
    public void addPermissionToRole(String roleName, Permission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }

        if (permission == null) {
            throw new IllegalArgumentException("Право доступа не может быть null");
        }

        Role role = rolesByName.get(roleName.toLowerCase());

        if (role == null) {
            throw new IllegalArgumentException("Роль с именем '" + roleName + "' не найдена");
        }

        // Добавляем право (внутри роли уже есть проверка на дубликаты)
        role.addPermission(permission);
    }


    public void removePermissionFromRole(String roleName, Permission permission) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя роли не может быть пустым");
        }

        if (permission == null) {
            throw new IllegalArgumentException("Право доступа не может быть null");
        }

        Role role = rolesByName.get(roleName.toLowerCase());

        if (role == null) {
            throw new IllegalArgumentException("Роль с именем '" + roleName + "' не найдена");
        }


        role.removePermission(permission);
    }

    public List<Role> findRolesWithPermission(String permissionName, String resource) {
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя права доступа не может быть пустым");
        }

        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ресурс права доступа не может быть пустым");
        }

        return rolesById.values().stream()
                .filter(role -> role.hasPermission(permissionName, resource))
                .collect(Collectors.toList());
    }

    public Map<String, Role> getRoles() {
        return Collections.unmodifiableMap(rolesById);
    }
}