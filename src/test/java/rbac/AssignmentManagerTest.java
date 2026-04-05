package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

class AssignmentManagerTest {

    private AssignmentManager manager;
    private User user;
    private Role role;

    @BeforeEach
    void setUp() {
        manager = new AssignmentManager();
        user = User.validate("testuser", "Test User", "test@example.com");
        role = new Role("TestRole", "Test role");
        role.addPermission(new Permission("READ", "data", "Read data"));
    }

    @Test
    void testAddAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        manager.add(a);
        assertEquals(1, manager.count());
        assertTrue(manager.findById(a.assignmentId()).isPresent());
    }

    @Test
    void testAddDuplicateRoleToUser() {
        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "First");
        PermanentAssignment a1 = new PermanentAssignment(user, role, m1);
        manager.add(a1);
        AssignmentMetadata m2 = AssignmentMetadata.now("admin", "Second");
        PermanentAssignment a2 = new PermanentAssignment(user, role, m2);
        assertThrows(IllegalArgumentException.class, () -> manager.add(a2));
    }

    @Test
    void testFindAssignmentsByUser() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        manager.add(a);
        List<RoleAssignment> list = manager.findByUser(user);
        assertEquals(1, list.size());
        assertEquals(a, list.get(0));
    }

    @Test
    void testGetActiveAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Active");
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        manager.add(a);
        assertEquals(1, manager.getActiveAssignments().size());
    }

    @Test
    void testUserHasRole() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        manager.add(new PermanentAssignment(user, role, meta));
        assertTrue(manager.userHasRole(user, role));
    }

    @Test
    void testUserHasPermission() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        manager.add(new PermanentAssignment(user, role, meta));
        assertTrue(manager.userHasPermission(user, "READ", "data"));
        assertFalse(manager.userHasPermission(user, "WRITE", "data"));
    }

    @Test
    void testGetUserPermissions() {
        Role r2 = new Role("Role2", "R2");
        r2.addPermission(new Permission("WRITE", "data", "W"));
        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "M1");
        AssignmentMetadata m2 = AssignmentMetadata.now("admin", "M2");
        manager.add(new PermanentAssignment(user, role, m1));
        manager.add(new PermanentAssignment(user, r2, m2));
        Set<Permission> perms = manager.getUserPermissions(user);
        assertEquals(2, perms.size());
    }

    @Test
    void testRevokePermanentAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        manager.add(a);
        manager.revokeAssignment(a.assignmentId());
        assertTrue(manager.findById(a.assignmentId()).isEmpty());
    }

    @Test
    void testExtendTemporaryAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        TemporaryAssignment a = new TemporaryAssignment(user, role, meta, "2026-12-31 23:59");
        manager.add(a);
        manager.extendTemporaryAssignment(a.assignmentId(), "2027-12-31 23:59");
        assertTrue(manager.findById(a.assignmentId()).isPresent());
    }

    @Test
    void testFilterAssignments() {
        User u2 = User.validate("user_two", "U2", "u2@test.com");
        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "M1");
        AssignmentMetadata m2 = AssignmentMetadata.now("manager", "M2");
        manager.add(new PermanentAssignment(user, role, m1));
        manager.add(new PermanentAssignment(u2, role, m2));
        List<RoleAssignment> f = manager.findByFilter(AssignmentFilters.assignedBy("admin"));
        assertEquals(1, f.size());
    }

    @Test
    void testRemoveAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment a = new PermanentAssignment(user, role, meta);
        manager.add(a);
        assertTrue(manager.remove(a));
        assertEquals(0, manager.count());
    }

    @Test
    void testClear() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        manager.add(new PermanentAssignment(user, role, meta));
        manager.clear();
        assertEquals(0, manager.count());
    }

    @Test
    void testConcurrentAddAssignments() throws Exception {
        int threads = 10, perThread = 10;
        ExecutorService exec = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final int tn = t;
            exec.submit(() -> {
                try {
                    for (int i = 0; i < perThread; i++) {
                        User u = User.validate("usr_" + tn + "_" + i, "U", "u@test.com");
                        Role r = new Role("ROL_" + tn + "_" + i, "R");
                        AssignmentMetadata m = AssignmentMetadata.now("admin", "M");
                        manager.add(new PermanentAssignment(u, r, m));
                    }
                } finally { latch.countDown(); }
            });
        }
        latch.await();
        exec.shutdown();
        assertEquals(threads * perThread, manager.count());
    }

    @Test
    void testFindByFilterParallel() {
        User u2 = User.validate("user_two", "U2", "u2@test.com");
        AssignmentMetadata m1 = AssignmentMetadata.now("admin", "M1");
        AssignmentMetadata m2 = AssignmentMetadata.now("manager", "M2");
        manager.add(new PermanentAssignment(user, role, m1));
        manager.add(new PermanentAssignment(u2, role, m2));
        List<RoleAssignment> f = manager.findByFilterParallel(a -> a.metadata().assignedBy().equals("admin"));
        assertEquals(1, f.size());
    }

    @Test
    void testDeactivateExpired() {
        User u1 = User.validate("usr_one", "User1", "u1@test.com");
        User u2 = User.validate("usr_two", "User2", "u2@test.com");
        Role testRole = new Role("TestRole", "Test");

        // Истёкшее (2020 год)
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Expired");
        TemporaryAssignment expired = new TemporaryAssignment(u1, testRole, meta1, "2020-01-01 00:00");
        manager.add(expired);

        // Активное (2030 год)
        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Active");
        TemporaryAssignment active = new TemporaryAssignment(u2, testRole, meta2, "2030-01-01 00:00");
        manager.add(active);

        int deactivated = manager.deactivateExpired();

        assertEquals(1, deactivated, "Должно быть деактивировано 1 назначение");
        assertTrue(manager.findById(expired.assignmentId()).isEmpty(), "Истёкшее должно быть удалено");
        assertTrue(manager.findById(active.assignmentId()).isPresent(), "Активное должно остаться");
    }
}