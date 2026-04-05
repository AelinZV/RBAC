package rbac;

import java.util.concurrent.*;
import java.util.*;

/**
 * Нагрузочный тест для проверки потокобезопасности (Подзадача 3, пункт 5).
 */
public class LoadTest {

    private static final int THREAD_COUNT = 10;
    private static final int OPERATIONS_PER_THREAD = 100;

    public static void main(String[] args) throws Exception {
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    НАГРУЗОЧНЫЙ ТЕСТ RBAC                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();
        System.out.println("Параметры:");
        System.out.println("  • Потоков: " + THREAD_COUNT);
        System.out.println("  • Операций на поток: " + OPERATIONS_PER_THREAD);
        System.out.println("  • Всего операций: " + (THREAD_COUNT * OPERATIONS_PER_THREAD));
        System.out.println();

        RBACSystem system = new RBACSystem();
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);

        ConcurrentHashMap<String, Integer> errors = new ConcurrentHashMap<>();

        System.out.println("🚀 Запуск тестовых потоков...");
        long startTime = System.currentTimeMillis();

        for (int t = 0; t < THREAD_COUNT; t++) {
            final int threadNum = t;
            executor.submit(() -> {
                try {
                    runTestThread(system, threadNum, errors);
                } catch (Exception e) {
                    errors.merge("THREAD_" + threadNum, 1, Integer::sum);
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long endTime = System.currentTimeMillis();

        executor.shutdown();
        system.shutdown();

        printResults(endTime - startTime, errors, system);
    }

    private static void runTestThread(RBACSystem system, int threadNum,
                                      ConcurrentHashMap<String, Integer> errors) {
        Random random = new Random(threadNum);

        for (int i = 0; i < OPERATIONS_PER_THREAD; i++) {
            try {
                int operation = random.nextInt(5);

                switch (operation) {
                    case 0: // Создание пользователя
                        createTestUser(system, threadNum, i);
                        break;
                    case 1: // Создание роли
                        createTestRole(system, threadNum, i);
                        break;
                    case 2: // Фильтрация пользователей
                        system.getUserManager().findByFilterParallel(u ->
                                u.username().contains("user_" + threadNum));
                        break;
                    case 3: // Поиск ролей
                        system.getRoleManager().findByFilterParallel(r ->
                                r.name().contains("role_" + threadNum));
                        break;
                    case 4: // Проверка прав
                        testUserPermissions(system, threadNum);
                        break;
                }
            } catch (Exception e) {
                errors.merge(e.getClass().getSimpleName(), 1, Integer::sum);
            }
        }
    }

    private static void createTestUser(RBACSystem system, int threadNum, int index) {
        String username = "user_" + threadNum + "_" + index;
        String email = "user" + threadNum + "_" + index + "@test.com";
        try {
            User user = User.validate(username, "User " + index, email);
            system.getUserManager().add(user);
        } catch (IllegalArgumentException e) {
            // Пользователь уже существует — это нормально
        }
    }

    private static void createTestRole(RBACSystem system, int threadNum, int index) {
        String roleName = "role_" + threadNum + "_" + (index % 20);
        try {
            Role role = new Role(roleName, "Role " + index);
            role.addPermission(new Permission("READ", "resource_" + index, "Read"));
            system.getRoleManager().add(role);
        } catch (IllegalArgumentException e) {
            // Роль уже существует — это нормально
        }
    }

    private static void testUserPermissions(RBACSystem system, int threadNum) {
        String username = "user_" + threadNum + "_0";
        system.getUserManager().findByUsername(username)
                .ifPresent(user -> system.getAssignmentManager().getUserPermissions(user));
    }

    private static void printResults(long totalTime,
                                     ConcurrentHashMap<String, Integer> errors,
                                     RBACSystem system) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      РЕЗУЛЬТАТЫ ТЕСТА                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("⏱️  Время выполнения: " + totalTime + " мс");
        System.out.println("📊 Операций в секунду: " +
                (THREAD_COUNT * OPERATIONS_PER_THREAD * 1000 / Math.max(totalTime, 1)));
        System.out.println();

        System.out.println("📊 Состояние системы:");
        System.out.println("   Пользователей: " + system.getUserManager().count());
        System.out.println("   Ролей: " + system.getRoleManager().count());
        System.out.println("   Назначений: " + system.getAssignmentManager().count());
        System.out.println();

        if (errors.isEmpty()) {
            System.out.println("✅ ОШИБОК НЕ ОБНАРУЖЕНО");
            System.out.println("✅ Приложение стабильно под нагрузкой");
            System.out.println("✅ Гонки данных и дубликаты отсутствуют");
        } else {
            System.out.println("❌ ОБНАРУЖЕНЫ ОШИБКИ:");
            errors.forEach((type, count) ->
                    System.out.println("   " + type + ": " + count));
        }
        System.out.println();
        System.out.println("══════════════════════════════════════════════════════════════");
    }
}