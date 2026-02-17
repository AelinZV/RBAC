package rbac;

/**
 * Интерфейс назначения роли пользователю
 */
public interface RoleAssignment {
    /**
     * Уникальный идентификатор назначения
     */
    String assignmentId();

    /**
     * Пользователь, которому назначена роль
     */
    User user();

    /**
     * Назначенная роль
     */
    Role role();

    /**
     * Метаданные о назначении
     */
    AssignmentMetadata metadata();

    /**
     * Проверка активности назначения
     * @return true если назначение активно
     */
    boolean isActive();

    /**
     * Тип назначения (ПОСТОЯННОЕ или ВРЕМЕННОЕ)
     */
    String assignmentType();

    /**
     * Краткое резюме назначения
     * @return форматированная строка с информацией о назначении
     */
    String summary(); // ← ДОБАВЛЕНО
}