package rbac;

import java.util.function.Predicate;

/**
 * Функциональный интерфейс для фильтрации пользователей
 */
@FunctionalInterface
public interface UserFilter extends Predicate<User> {

    default UserFilter and(UserFilter other) {
        return user -> this.test(user) && other.test(user);
    }


    default UserFilter or(UserFilter other) {
        return user -> this.test(user) || other.test(user);
    }
}