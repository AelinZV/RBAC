# проверка ветки
git checkout feature/filters
git branch --show-current  # Должно быть: feature/filters

# === КОММИТ 1: Исправление интерфейса (требуется для компиляции) ===
git add src/main/java/rbac/RoleAssignment.java
git commit -m "fix(filters): add summary() method to RoleAssignment interface"

# === КОММИТ 2: Подпункт 2.1 — Фильтрация пользователей ===
git add src/main/java/rbac/UserFilter.java
git add src/main/java/rbac/UserFilters.java
git commit -m "feat(filters): implement UserFilter interface and UserFilters factory methods (2.1)"

# === КОММИТ 3: Подпункт 2.2 — Фильтрация ролей ===
git add src/main/java/rbac/RoleFilter.java
git add src/main/java/rbac/RoleFilters.java
git commit -m "feat(filters): implement RoleFilter interface and RoleFilters factory methods (2.2)"

# === КОММИТ 4: Подпункт 2.3 — Фильтрация назначений ===
git add src/main/java/rbac/AssignmentFilter.java
git add src/main/java/rbac/AssignmentFilters.java
git commit -m "feat(filters): implement AssignmentFilter interface and AssignmentFilters factory methods (2.3)"

# === КОММИТ 5: Подпункт 2.4 — Сортировка ===
git add src/main/java/rbac/UserSorters.java
git add src/main/java/rbac/RoleSorters.java
git add src/main/java/rbac/AssignmentSorters.java
git commit -m "feat(filters): implement UserSorters, RoleSorters, AssignmentSorters (2.4)"

# === КОММИТ 6: Демонстрация ===
git add src/main/java/rbac/Main.java
git commit -m "feat(filters): update Main.java with comprehensive filter/sort demonstrations"
