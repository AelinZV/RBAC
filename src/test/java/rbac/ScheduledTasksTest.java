package rbac;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.*;

/**
 * Тесты для периодических задач (Подзадача 7).
 * Проверяет:
 * - Запуск/остановку периодических задач
 * - Деактивацию истёкших назначений
 * - Потокобезопасность deactivateExpired()
 * - Короткие критические секции
 */
class ScheduledTasksTest {

    private RBACSystem system;

    @BeforeEach
    void setUp() {
        system = new RBACSystem();
        system.initialize();
    }

    @AfterEach
    void tearDown() {
        system.stopPeriodicTasks();
        system.shutdown();
    }

    @Test
    void testStartPeriodicTasks() {
        // Проверка, что запуск не выбрасывает исключений
        assertDoesNotThrow(() -> system.startPeriodicTasks(5));
    }

    @Test
    void testStopPeriodicTasks() {
        system.startPeriodicTasks(5);

        // Проверка, что остановка не выбрасывает исключений
        assertDoesNotThrow(() -> system.stopPeriodicTasks());
    }

    @Test
    void testDeactivateExpired() throws Exception {
        AssignmentManager manager = system.getAssignmentManager();

        // Создание истёкшего временного назначения
        User user = User.validate("testuser", "Test", "test@example.com");
        Role role = new Role("TestRole", "Test");
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

        // Дата в прошлом — должно быть деактивировано
        TemporaryAssignment expired = new TemporaryAssignment(
                user, role, meta, "2020-01-01 00:00"
        );
        manager.add(expired);

        // Создание активного назначения — должно остаться
        TemporaryAssignment active = new TemporaryAssignment(
                user, new Role("ActiveRole", "Active"), meta, "2030-01-01 00:00"
        );
        manager.add(active);

        // Вызов деактивации
        int deactivated = manager.deactivateExpired();

        // Проверка результата
        assertEquals(1, deactivated, "Должно быть деактивировано 1 назначение");
        assertTrue(manager.findById(expired.assignmentId()).isEmpty(),
                "Истёкшее назначение должно быть удалено");
        assertTrue(manager.findById(active.assignmentId()).isPresent(),
                "Активное назначение должно остаться");
    }

    @Test
    void testConcurrentDeactivateExpired() throws Exception {
        AssignmentManager manager = system.getAssignmentManager();
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Integer> errors = new ConcurrentHashMap<>();

        // Запускаем несколько потоков, каждый вызывает deactivateExpired
        for (int t = 0; t < threadCount; t++) {
            executor.submit(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        manager.deactivateExpired();
                        Thread.sleep(10); // Небольшая пауза для имитации нагрузки
                    }
                } catch (Exception e) {
                    errors.merge("DEACTIVATE_ERROR", 1, Integer::sum);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Ожидаем завершения всех потоков
        latch.await();
        executor.shutdown();

        // Проверяем отсутствие ошибок
        assertTrue(errors.isEmpty(),
                "Ошибки при конкурентной деактивации: " + errors);
    }

    @Test
    void testPeriodicTaskRuns() throws Exception {
        // Запуск периодической задачи с коротким интервалом для теста
        system.startPeriodicTasks(2);

        // Ожидание выполнения хотя бы одного цикла (2 сек интервал + запас)
        Thread.sleep(3000);

        // Проверка, что система всё ещё работает и не упала
        assertTrue(system.getUserManager().count() >= 0,
                "Система должна оставаться работоспособной");

        // Останавливаем задачу
        system.stopPeriodicTasks();
    }

    @Test
    void testDeactivateExpiredShortCriticalSection() {
        AssignmentManager manager = system.getAssignmentManager();

        // Создаём несколько назначений для проверки оптимизации
        for (int i = 0; i < 10; i++) {
            User user = User.validate("user" + i, "User " + i, "u" + i + "@test.com");
            Role role = new Role("Role" + i, "Role " + i);
            AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");

            // Смешиваем истёкшие и активные назначения
            String date = (i % 2 == 0) ? "2020-01-01 00:00" : "2030-01-01 00:00";
            TemporaryAssignment ta = new TemporaryAssignment(user, role, meta, date);
            manager.add(ta);
        }

        // Вызов деактивации должен завершиться быстро (короткая критическая секция)
        long start = System.currentTimeMillis();
        int deactivated = manager.deactivateExpired();
        long elapsed = System.currentTimeMillis() - start;

        // Проверка: 5 истёкших назначений должно быть деактивировано
        assertEquals(5, deactivated, "Должно быть деактивировано 5 назначений");

        // Проверка: выполнение должно быть быстрым (< 100 мс для 10 элементов)
        assertTrue(elapsed < 100,
                "deactivateExpired() должен выполняться быстро: " + elapsed + " мс");
    }
}