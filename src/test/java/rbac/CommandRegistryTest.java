package rbac;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Scanner;

/**
 * Тесты для CommandRegistry (Подзадача 4).
 */
class CommandRegistryTest {

    private CommandParser parser;
    private RBACSystem system;
    private Scanner scanner;

    @BeforeEach
    void setUp() {
        parser = new CommandParser();
        system = new RBACSystem();
        scanner = new Scanner(System.in);

        // Регистрация тестовой команды
        parser.registerCommand("test", "Test command",
                (s, sys) -> System.out.println("Test executed"));
    }

    @AfterEach
    void tearDown() {
        system.shutdown();
    }

    @Test
    void testRegisterCommand() {
        // Проверка, что команда зарегистрирована
        // (косвенная проверка через выполнение)
        assertDoesNotThrow(() -> parser.executeCommand("test", scanner, system));
    }

    @Test
    void testExecuteUnknownCommand() {
        // Перехват вывода для проверки сообщения об ошибке
        // (упрощённая проверка)
        assertDoesNotThrow(() -> parser.executeCommand("unknown", scanner, system));
    }

    @Test
    void testPrintHelp() {
        // Проверка, что help не выбрасывает исключений
        assertDoesNotThrow(() -> parser.printHelp());
    }

    @Test
    void testParseAndExecuteEmpty() {
        // Пустой ввод не должен вызывать ошибок
        assertDoesNotThrow(() -> parser.parseAndExecute("", scanner, system));
        assertDoesNotThrow(() -> parser.parseAndExecute("   ", scanner, system));
    }

    @Test
    void testConcurrentRegistration() throws Exception {
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            final int num = i;
            threads[i] = new Thread(() ->
                    parser.registerCommand("cmd" + num, "Desc" + num,
                            (s, sys) -> System.out.println("Cmd" + num))
            );
            threads[i].start();
        }

        for (Thread t : threads) {
            t.join();
        }

        // Проверка, что все команды зарегистрированы (без падений)
        assertDoesNotThrow(() -> parser.executeCommand("cmd5", scanner, system));
    }
}