package rbac;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Тесты для менеджера ролей
 */
class RoleManagerTest {

    private RoleManager manager;

    @BeforeEach
    void setUp() {
        manager = new RoleManager();
    }

    @Test
    void testAddRole() {
        Role role = new Role("Admin", "Administrator role");
        manager.add(role);

        assertEquals(1, manager.count());
        assertTrue(manager.exists("Admin"));
    }

    @Test
    void testAddDuplicateRole() {
        Role role1 = new Role("Viewer", "Viewer role");
        Role role2 = new Role("Viewer", "Another viewer role");

        manager.add(role1);

        assertThrows(IllegalArgumentException.class, () -> manager.add(role2));
    }

    @Test
    void testFindRoleByName() {
        Role role = new Role("Editor", "Editor role");
        manager.add(role);

        assertTrue(manager.findByName("Editor").isPresent());
        assertEquals("Editor", manager.findByName("Editor").get().name());
    }

    @Test
    void testAddPermissionToRole() {
        Role role = new Role("TestRole", "Test role");
        manager.add(role);

        Permission permission = new Permission("READ", "data", "Read data");
        manager.addPermissionToRole("TestRole", permission);

        assertTrue(role.hasPermission("READ", "data"));
    }

    @Test
    void testFindRolesWithPermission() {
        Role role1 = new Role("Role1", "Role 1");
        role1.addPermission(new Permission("READ", "users", "Read users"));
        manager.add(role1);

        Role role2 = new Role("Role2", "Role 2");
        role2.addPermission(new Permission("WRITE", "users", "Write users"));
        manager.add(role2);

        List<Role> roles = manager.findRolesWithPermission("READ", "users");

        assertEquals(1, roles.size());
        assertEquals("Role1", roles.get(0).name());
    }

    @Test
    void testRemoveRole() {
        Role role = new Role("ToDelete", "Role to delete");
        manager.add(role);

        assertTrue(manager.remove(role));
        assertEquals(0, manager.count());
        assertFalse(manager.exists("ToDelete"));
    }

    @Test
    void testFilterRoles() {
        Role admin = new Role("Admin", "Full access");
        admin.addPermission(new Permission("READ", "all", "Read all"));
        admin.addPermission(new Permission("WRITE", "all", "Write all"));
        manager.add(admin);

        Role viewer = new Role("Viewer", "Read only");
        viewer.addPermission(new Permission("READ", "data", "Read data"));
        manager.add(viewer);

        RoleFilter filter = RoleFilters.hasAtLeastNPermissions(2);
        List<Role> filtered = manager.findByFilter(filter);

        assertEquals(1, filtered.size());
        assertEquals("Admin", filtered.get(0).name());
    }

    @Test
    void testSortRoles() {
        Role roleC = new Role("RoleC", "Role C");
        roleC.addPermission(new Permission("READ", "x", "X"));
        manager.add(roleC);

        Role roleA = new Role("RoleA", "Role A");
        roleA.addPermission(new Permission("READ", "y", "Y"));
        manager.add(roleA);

        Role roleB = new Role("RoleB", "Role B");
        roleB.addPermission(new Permission("READ", "z", "Z"));
        roleB.addPermission(new Permission("WRITE", "z", "Z"));
        manager.add(roleB);

        List<Role> sorted = manager.findAll(null, RoleSorters.byPermissionCount().reversed());

        assertEquals(2, sorted.get(0).getPermissions().size());
        assertEquals(1, sorted.get(1).getPermissions().size());
        assertEquals(1, sorted.get(2).getPermissions().size());
    }

    @Test
    void testClear() {
        manager.add(new Role("Role1", "Role 1"));
        manager.add(new Role("Role2", "Role 2"));

        manager.clear();

        assertEquals(0, manager.count());
    }
}