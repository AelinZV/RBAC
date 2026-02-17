package rbac;

import java.util.Objects;
import java.util.function.Predicate;

public class RoleFilters {

    public static RoleFilter byName(String name) {
        Objects.requireNonNull(name, "Role name cannot be null");
        return role -> role.name().equals(name);
    }


    public static RoleFilter byNameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        return role -> role.name().toLowerCase().contains(substring.toLowerCase());
    }

    public static RoleFilter hasPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission cannot be null");
        return role -> role.hasPermission(permission);
    }

    public static RoleFilter hasPermission(String permissionName, String resource) {
        Objects.requireNonNull(permissionName, "Permission name cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        return role -> role.hasPermission(permissionName, resource);
    }

    public static RoleFilter hasAtLeastNPermissions(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Number of permissions must be non-negative");
        }
        return role -> role.getPermissions().size() >= n;
    }
}