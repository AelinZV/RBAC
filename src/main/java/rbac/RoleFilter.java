package rbac;

import java.util.function.Predicate;

@FunctionalInterface
public interface RoleFilter extends Predicate<Role> {

    default RoleFilter and(RoleFilter other) {
        return role -> this.test(role) && other.test(role);
    }


    default RoleFilter or(RoleFilter other) {
        return role -> this.test(role) || other.test(role);
    }
}