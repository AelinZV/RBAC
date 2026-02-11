Лаба 1

# Создаём правильную структуру папок
mkdir -p src/main/java/rbac

# Коммит 0: Maven конфигурация
git add pom.xml
git commit -m "chore: добавить структуру Maven проекта"

# Коммит 1: Пользователь
git add src/main/java/rbac/User.java
git commit -m "feat(bds): реализовать запись User с валидацией (1.1)"

# Коммит 2: Права доступа
git add src/main/java/rbac/Permission.java
git commit -m "feat(bds): реализовать запись Permission с нормализацией (1.2)"

# Коммит 3: Роль
git add src/main/java/rbac/Role.java
git commit -m "feat(bds): реализовать класс Role с управлением правами (1.3)"

# Коммит 4: Метаданные
git add src/main/java/rbac/AssignmentMetadata.java
git commit -m "feat(bds): реализовать запись AssignmentMetadata (1.4)"

# Коммит 5: Интерфейс назначения
git add src/main/java/rbac/RoleAssignment.java
git commit -m "feat(bds): определить интерфейс RoleAssignment (1.5)"

# Коммит 6: Абстрактный класс
git add src/main/java/rbac/AbstractRoleAssignment.java
git commit -m "feat(bds): реализовать абстрактный класс AbstractRoleAssignment (1.6)"

# Коммит 7: Постоянное назначение
git add src/main/java/rbac/PermanentAssignment.java
git commit -m "feat(bds): реализовать класс PermanentAssignment (1.7)"

# Коммит 8: Временное назначение
git add src/main/java/rbac/TemporaryAssignment.java
git commit -m "feat(bds): реализовать класс TemporaryAssignment (1.8)"

# Коммит 9: Демонстрация
git add src/main/java/rbac/Main.java
git commit -m "feat(bds): добавить Main класс с демонстрацией работы"

# Слияние в ветку dev
git checkout dev
git merge --no-ff feature/bds -m "feat: завершить реализацию базовых структур данных (подзадача 1)"
git push origin dev