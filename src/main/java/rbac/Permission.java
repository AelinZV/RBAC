package rbac;

/**
 * Запись права доступа с нормализацией данных
 */
public record Permission(String name, String resource, String description) {

    /**
     * Канонический конструктор с валидацией и нормализацией
     * ВАЖНО: присваиваем новые значения параметрам напрямую!
     */
    public Permission {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя права доступа не может быть пустым или null");
        }
        if (name.contains(" ")) {
            throw new IllegalArgumentException("Имя права доступа не может содержать пробелы");
        }
        name = name.trim().toUpperCase(); // Нормализация

        if (resource == null || resource.trim().isEmpty()) {
            throw new IllegalArgumentException("Ресурс не может быть пустым или null");
        }
        resource = resource.trim().toLowerCase(); // Нормализация

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Описание не может быть пустым или null");
        }
        description = description.trim(); // Нормализация
    }

    /**
     * Форматированный вывод права доступа
     * @return строка вида "READ on users: Описание текст"
     */
    public String format() {
        return String.format("%s on %s: %s", name, resource, description);
    }

    /**
     * Проверка соответствия шаблонам поиска
     * @param namePattern шаблон для имени (используется contains)
     * @param resourcePattern шаблон для ресурса (используется contains)
     * @return true если совпадает по обоим шаблонам
     */
    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatch = namePattern == null || namePattern.isEmpty() ||
                name.contains(namePattern.toUpperCase());
        boolean resourceMatch = resourcePattern == null || resourcePattern.isEmpty() ||
                resource.contains(resourcePattern.toLowerCase());
        return nameMatch && resourceMatch;
    }
}