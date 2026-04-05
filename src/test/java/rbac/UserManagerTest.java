package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.concurrent.*;

/**
 * Тесты для менеджера пользователей
 */
class UserManagerTest {

    private UserManager manager;

    @BeforeEach
    void setUp() {
        manager = new UserManager();
    }

    @Test
    void testAddUser() {
        User user = User.validate("john_doe", "John Doe", "john@example.com");
        manager.add(user);

        assertEquals(1, manager.count());
        assertTrue(manager.exists("john_doe"));
    }

    @Test
    void testAddDuplicateUser() {
        User user1 = User.validate("john", "John", "john@example.com");
        User user2 = User.validate("john", "John2", "john2@example.com");

        manager.add(user1);

        assertThrows(IllegalArgumentException.class, () -> manager.add(user2));
    }

    @Test
    void testFindUserByUsername() {
        User user = User.validate("alice", "Alice Smith", "alice@example.com");
        manager.add(user);

        assertTrue(manager.findByUsername("alice").isPresent());
        assertEquals("alice", manager.findByUsername("alice").get().username());
    }

    @Test
    void testFindUserByEmail() {
        User user = User.validate("bob", "Bob Wilson", "bob@company.com");
        manager.add(user);

        assertTrue(manager.findByEmail("bob@company.com").isPresent());
        assertEquals("bob", manager.findByEmail("bob@company.com").get().username());
    }

    @Test
    void testUpdateUser() {
        User user = User.validate("charlie", "Charlie Brown", "charlie@example.com");
        manager.add(user);

        manager.update("charlie", "Charlie Updated", "charlie@new.com");

        User updated = manager.findByUsername("charlie").get();
        assertEquals("Charlie Updated", updated.fullName());
        assertEquals("charlie@new.com", updated.email());
    }

    @Test
    void testFilterUsers() {
        manager.add(User.validate("user1", "User One", "user1@company.com"));
        manager.add(User.validate("user2", "User Two", "user2@external.org"));
        manager.add(User.validate("admin", "Admin", "admin@company.com"));

        UserFilter filter = UserFilters.byEmailDomain("@company.com");
        List<User> filtered = manager.findByFilter(filter);

        assertEquals(2, filtered.size());
    }

    @Test
    void testSortUsers() {
        manager.add(User.validate("zebra", "Zebra", "z@example.com"));
        manager.add(User.validate("alpha", "Alpha", "a@example.com"));
        manager.add(User.validate("mike", "Mike", "m@example.com"));

        List<User> sorted = manager.findAll(null, UserSorters.byUsername());

        assertEquals("alpha", sorted.get(0).username());
        assertEquals("mike", sorted.get(1).username());
        assertEquals("zebra", sorted.get(2).username());
    }

    @Test
    void testRemoveUser() {
        User user = User.validate("test", "Test User", "test@example.com");
        manager.add(user);

        assertTrue(manager.remove(user));
        assertEquals(0, manager.count());
        assertFalse(manager.exists("test"));
    }

    @Test
    void testClear() {
        manager.add(User.validate("user1", "User 1", "u1@example.com"));
        manager.add(User.validate("user2", "User 2", "u2@example.com"));

        manager.clear();

        assertEquals(0, manager.count());
    }

    // ✏️ НОВЫЕ ТЕСТЫ НА ПОТОКОБЕЗОПАСНОСТЬ

    @Test
    void testConcurrentAddUsers() throws Exception {
        int threadCount = 10;
        int usersPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        ConcurrentHashMap<String, Throwable> errors = new ConcurrentHashMap<>();

        for (int t = 0; t < threadCount; t++) {
            final int threadNum = t;
            executor.submit(() -> {
                try {
                    for (int i = 0; i < usersPerThread; i++) {
                        User user = User.validate(
                                "user_" + threadNum + "_" + i,
                                "User " + threadNum + "_" + i,
                                "user" + threadNum + "_" + i + "@test.com"
                        );
                        manager.add(user);
                    }
                } catch (Throwable e) {
                    errors.put("thread-" + threadNum, e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Errors: " + errors);
        assertEquals(threadCount * usersPerThread, manager.count());
    }

    @Test
    void testConcurrentReadAndWrite() throws Exception {
        // Создаём начальных пользователей
        for (int i = 0; i < 20; i++) {
            manager.add(User.validate("init_" + i, "Init " + i, "init" + i + "@test.com"));
        }

        ExecutorService executor = Executors.newFixedThreadPool(8);
        CountDownLatch latch = new CountDownLatch(8);
        ConcurrentHashMap<String, Integer> errors = new ConcurrentHashMap<>();

        // Потоки на чтение
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        manager.findAll(null, null);
                        manager.findByFilterParallel(u -> u.username().contains("init"));
                        manager.findByUsername("init_5");
                    }
                } catch (Exception e) {
                    errors.merge("READ", 1, Integer::sum);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Потоки на запись
        for (int i = 0; i < 4; i++) {
            final int threadNum = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 10; j++) {
                        User user = User.validate(
                                "concurrent_" + threadNum + "_" + j,
                                "Concurrent " + j,
                                "conc" + threadNum + "_" + j + "@test.com"
                        );
                        manager.add(user);
                    }
                } catch (Exception e) {
                    errors.merge("WRITE", 1, Integer::sum);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(errors.isEmpty(), "Errors: " + errors);
        assertEquals(60, manager.count());
    }

    @Test
    void testFindByFilterParallel() {
        manager.add(User.validate("alice", "Alice", "alice@test.com"));
        manager.add(User.validate("bob", "Bob", "bob@test.com"));
        manager.add(User.validate("charlie", "Charlie", "charlie@test.com"));

        List<User> filtered = manager.findByFilterParallel(
                u -> u.username().startsWith("a") || u.username().startsWith("b")
        );

        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("alice")));
        assertTrue(filtered.stream().anyMatch(u -> u.username().equals("bob")));
    }
}