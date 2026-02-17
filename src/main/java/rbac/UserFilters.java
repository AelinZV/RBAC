package rbac;

import java.util.Objects;
import java.util.function.Predicate;


public class UserFilters {

    /**
     * Фильтр по точному совпадению имени пользователя
     */
    public static UserFilter byUsername(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return user -> user.username().equals(username);
    }

    /**
     * Фильтр по содержанию подстроки в имени пользователя (игнорируя регистр)
     */
    public static UserFilter byUsernameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        return user -> user.username().toLowerCase().contains(substring.toLowerCase());
    }

    /**
     * Фильтр по точному совпадению email
     */
    public static UserFilter byEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");
        return user -> user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        Objects.requireNonNull(domain, "Domain cannot be null");
        return user -> user.email().endsWith(domain);
    }

    public static UserFilter byFullNameContains(String substring) {
        Objects.requireNonNull(substring, "Substring cannot be null");
        return user -> user.fullName().toLowerCase().contains(substring.toLowerCase());
    }
}