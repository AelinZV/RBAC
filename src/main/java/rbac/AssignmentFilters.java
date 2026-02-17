package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Predicate;

public class AssignmentFilters {

    public static AssignmentFilter byUser(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        Objects.requireNonNull(role, "Role cannot be null");
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        Objects.requireNonNull(roleName, "Role name cannot be null");
        return assignment -> assignment.role().name().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return assignment -> assignment.isActive();
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        Objects.requireNonNull(type, "Type cannot be null");
        return assignment -> assignment.assignmentType().equalsIgnoreCase(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        Objects.requireNonNull(username, "Username cannot be null");
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        Objects.requireNonNull(date, "Date cannot be null");
        LocalDateTime afterDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return assignment -> LocalDateTime.parse(assignment.metadata().assignedAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .isAfter(afterDate);
    }

    public static AssignmentFilter expiringBefore(String date) {
        Objects.requireNonNull(date, "Date cannot be null");
        LocalDateTime beforeDate = LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return assignment -> assignment instanceof TemporaryAssignment &&
                LocalDateTime.parse(((TemporaryAssignment) assignment).expiresAt(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        .isBefore(beforeDate);
    }
}