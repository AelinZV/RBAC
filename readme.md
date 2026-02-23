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


# === Подпункт 3===
git add src/main/java/rbac/Repository.java
git commit -m "feat(managers): implement generic Repository interface (3.1)"

git add src/main/java/rbac/UserManager.java
git commit -m "feat(managers): implement UserManager with filter and sort support (3.2)"

git add src/main/java/rbac/RoleManager.java
git commit -m "feat(managers): implement RoleManager with permission management (3.3)"

git add src/main/java/rbac/AssignmentManager.java
git commit -m "feat(managers): implement AssignmentManager with permission aggregation (3.4)"

git add src/test/java/rbac/UserManagerTest.java
git commit -m "test(managers): add UserManager unit tests"

git add src/test/java/rbac/RoleManagerTest.java
git commit -m "test(managers): add RoleManager unit tests"

git add src/test/java/rbac/AssignmentManagerTest.java
git commit -m "test(managers): add AssignmentManager unit tests"
git add pom.xml
git commit -m "fix(managers): configure maven-surefire-plugin for JUnit 5 test execution"
git add .
git commit -m "feat(managers): complete managers implementation with passing tests"
git push origin feature/managers


git checkout dev
git merge --no-ff feature/managers -m "feat: complete managers implementation with tests (subtask 3)"
git push origin dev

# === Запуск Тестов через pom.xml ===
 mvn clean install -U -Dfile.encoding=UTF-8
 mvn test

git add pom.xml
git commit -m "fix(managers): configure maven-surefire-plugin for JUnit 5 test execution"
git add .
git commit -m "feat(managers): complete managers implementation with passing tests"
git push origin feature/managers
 