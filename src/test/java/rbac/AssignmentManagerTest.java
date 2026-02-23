package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;

/**
 * Тесты для менеджера назначений
 */
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
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test assignment");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);

        manager.add(assignment);

        assertEquals(1, manager.count());
        assertTrue(manager.findById(assignment.assignmentId()).isPresent());
    }

    @Test
    void testAddDuplicateRoleToUser() {
        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "First assignment");
        PermanentAssignment assignment1 = new PermanentAssignment(user, role, meta1);
        manager.add(assignment1);

        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Second assignment");
        PermanentAssignment assignment2 = new PermanentAssignment(user, role, meta2);

        assertThrows(IllegalArgumentException.class, () -> manager.add(assignment2));
    }

    @Test
    void testFindAssignmentsByUser() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        manager.add(assignment);

        List<RoleAssignment> assignments = manager.findByUser(user);

        assertEquals(1, assignments.size());
        assertEquals(assignment, assignments.get(0));
    }

    @Test
    void testGetActiveAssignments() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Active");
        PermanentAssignment active = new PermanentAssignment(user, role, meta);
        manager.add(active);

        List<RoleAssignment> activeAssignments = manager.getActiveAssignments();

        assertEquals(1, activeAssignments.size());
    }

    @Test
    void testUserHasRole() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        manager.add(assignment);

        assertTrue(manager.userHasRole(user, role));
    }

    @Test
    void testUserHasPermission() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        manager.add(assignment);

        assertTrue(manager.userHasPermission(user, "READ", "data"));
        assertFalse(manager.userHasPermission(user, "WRITE", "data"));
    }

    @Test
    void testGetUserPermissions() {
        Role role2 = new Role("Role2", "Second role");
        role2.addPermission(new Permission("WRITE", "data", "Write data"));

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Role 1");
        manager.add(new PermanentAssignment(user, role, meta1));

        AssignmentMetadata meta2 = AssignmentMetadata.now("admin", "Role 2");
        manager.add(new PermanentAssignment(user, role2, meta2));

        Set<Permission> permissions = manager.getUserPermissions(user);

        assertEquals(2, permissions.size());
        assertTrue(permissions.stream().anyMatch(p -> p.name().equals("READ")));
        assertTrue(permissions.stream().anyMatch(p -> p.name().equals("WRITE")));
    }

    @Test
    void testRevokePermanentAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        manager.add(assignment);

        manager.revokeAssignment(assignment.assignmentId());

        assertFalse(assignment.isActive());
    }

    @Test
    void testExtendTemporaryAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        TemporaryAssignment assignment = new TemporaryAssignment(
                user, role, meta, "2026-12-31 23:59"
        );
        manager.add(assignment);

        manager.extendTemporaryAssignment(assignment.assignmentId(), "2027-12-31 23:59");

        // Проверяем, что назначение всё ещё активно
        assertTrue(assignment.isActive());
    }

    @Test
    void testFilterAssignments() {
        User user2 = User.validate("user2", "User 2", "user2@example.com");

        AssignmentMetadata meta1 = AssignmentMetadata.now("admin", "Admin assigned");
        manager.add(new PermanentAssignment(user, role, meta1));

        AssignmentMetadata meta2 = AssignmentMetadata.now("manager", "Manager assigned");
        manager.add(new PermanentAssignment(user2, role, meta2));

        AssignmentFilter filter = AssignmentFilters.assignedBy("admin");
        List<RoleAssignment> filtered = manager.findByFilter(filter);

        assertEquals(1, filtered.size());
    }

    @Test
    void testRemoveAssignment() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        PermanentAssignment assignment = new PermanentAssignment(user, role, meta);
        manager.add(assignment);

        assertTrue(manager.remove(assignment));
        assertEquals(0, manager.count());
    }

    @Test
    void testClear() {
        AssignmentMetadata meta = AssignmentMetadata.now("admin", "Test");
        manager.add(new PermanentAssignment(user, role, meta));

        manager.clear();

        assertEquals(0, manager.count());
    }
}