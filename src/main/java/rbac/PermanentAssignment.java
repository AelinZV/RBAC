package rbac;

/**
 * Класс постоянного назначения роли
 * Может быть отменён (revoked), но не имеет срока действия
 */
public class PermanentAssignment extends AbstractRoleAssignment {
    // Флаг отмены назначения
    private boolean revoked = false;

    /**
     * Конструктор постоянного назначения
     */
    public PermanentAssignment(User user, Role role, AssignmentMetadata metadata) {
        super(user, role, metadata);
    }

    @Override public boolean isActive() { return !revoked; }
    @Override public String assignmentType() { return "ПОСТОЯННОЕ"; }

    /**
     * Отмена постоянного назначения
     */
    public void revoke() {
        this.revoked = true;
    }

    /**
     * Проверка, отменено ли назначение
     */
    public boolean isRevoked() {
        return revoked;
    }
}