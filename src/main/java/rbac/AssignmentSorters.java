package rbac;

import java.util.Comparator;

public class AssignmentSorters {

    /**
     * Сортировка по имени пользователя
     */
    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(assignment -> assignment.user().username());
    }

    /**
     * Сортировка по имени роли
     */
    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(assignment -> assignment.role().name());
    }

    /**
     * Сортировка по дате назначения
     */
    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(assignment ->
                        assignment.metadata().assignedAt(),
                (date1, date2) -> date1.compareTo(date2));
    }
}