package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
}