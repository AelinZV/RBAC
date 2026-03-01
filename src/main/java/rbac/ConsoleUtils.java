package rbac;

import java.util.List;
import java.util.Scanner;


public class ConsoleUtils {


    public static String promptString(Scanner scanner, String message, boolean required) {
        while (true) {
            System.out.print(message + (required ? " (обязательно): " : " (или оставить пустым): "));
            String input = scanner.nextLine().trim();

            if (!required || !input.isEmpty()) {
                return input;
            }

            System.out.println("✗ Это поле обязательно для заполнения");
        }
    }

    /**
     * Запрос целого числа в диапазоне
     */
    public static int promptInt(Scanner scanner, String message, int min, int max) {
        while (true) {
            System.out.print(message + String.format(" [%d-%d]: ", min, max));

            try {
                int value = Integer.parseInt(scanner.nextLine().trim());

                if (value >= min && value <= max) {
                    return value;
                }

                System.out.println(String.format("✗ Число должно быть в диапазоне [%d-%d]", min, max));
            } catch (NumberFormatException e) {
                System.out.println("✗ Введите корректное целое число");
            }
        }
    }

    /**
     * Запрос подтверждения (да/нет)
     */
    public static boolean promptYesNo(Scanner scanner, String message) {
        while (true) {
            System.out.print(message + " (да/нет): ");
            String input = scanner.nextLine().trim().toLowerCase();

            if (input.equals("да") || input.equals("yes") || input.equals("y")) {
                return true;
            }

            if (input.equals("нет") || input.equals("no") || input.equals("n")) {
                return false;
            }

            System.out.println("✗ Введите 'да' или 'нет'");
        }
    }


    public static <T> T promptChoice(Scanner scanner, String message, List<T> options) {
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("Список опций не может быть пустым");
        }

        System.out.println(message);

        // Вывод опций с номерами
        for (int i = 0; i < options.size(); i++) {
            System.out.println(String.format("  %d. %s", i + 1, options.get(i).toString()));
        }

        int choice = promptInt(scanner, "Выберите номер", 1, options.size());
        return options.get(choice - 1);
    }

    /**
     * Очистка экрана консоли
     */
    public static void clearScreen() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }


    public static void printSectionHeader(String title) {
        System.out.println("\n" + "═".repeat(70));
        System.out.println("  " + title);
        System.out.println("═".repeat(70) + "\n");
    }


    public static void printSuccess(String message) {
        System.out.println("✓ " + message);
    }


    public static void printError(String message) {
        System.out.println("✗ " + message);
    }
}