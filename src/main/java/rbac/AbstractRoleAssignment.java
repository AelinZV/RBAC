package rbac;

import java.util.Objects;
import java.util.UUID;

/**
 * Абстрактный класс для реализации назначений ролей
 */
public abstract class AbstractRoleAssignment implements RoleAssignment {
    private final String assignmentId;
    private final User user;
    private final Role role;
    private final AssignmentMetadata metadata;

   
    public AbstractRoleAssignment(User user, Role role, AssignmentMetadata metadata) {
        if (user == null) throw new IllegalArgumentException("Пользователь не может быть null");
        if (role == null) throw new IllegalArgumentException("Роль не может быть null");
        if (metadata == null) throw new IllegalArgumentException("Метаданные не могут быть null");
        this.assignmentId = "assign_" + UUID.randomUUID().toString().substring(0, 8);
        this.user = user;
        this.role = role;
        this.metadata = metadata;
    }

    @Override public String assignmentId() { return assignmentId; }
    @Override public User user() { return user; }
    @Override public Role role() { return role; }
    @Override public AssignmentMetadata metadata() { return metadata; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractRoleAssignment that)) return false;
        return Objects.equals(assignmentId, that.assignmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(assignmentId);
    }


    public String summary() {
        return String.format("[%s] Роль %s назначена пользователю %s администратором %s в %s\nПричина: %s\nСтатус: %s",
                assignmentType(),
                role.name(),
                user.username(),
                metadata.assignedBy(),
                metadata.assignedAt(),
                metadata.reason() != null && !metadata.reason().trim().isEmpty()
                        ? metadata.reason().trim() : "Не указана",
                isActive() ? "АКТИВНО" : "НЕАКТИВНО"
        );
    }
}
