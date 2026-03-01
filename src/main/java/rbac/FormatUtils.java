package rbac;

import java.util.List;


public class FormatUtils {


    public static String formatTable(String[] headers, List<String[]> rows) {
        if (headers == null || headers.length == 0) {
            return "Нет данных для отображения";
        }

        // Определение максимальной ширины для каждого столбца
        int[] columnWidths = new int[headers.length];

        // Ширина заголовков
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = Math.max(columnWidths[i], headers[i].length() + 2);
        }

        // Ширина данных
        for (String[] row : rows) {
            for (int i = 0; i < Math.min(row.length, headers.length); i++) {
                columnWidths[i] = Math.max(columnWidths[i], row[i].length() + 2);
            }
        }

        StringBuilder sb = new StringBuilder();

        // Верхняя граница
        sb.append("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        // Заголовки
        sb.append("|");
        for (int i = 0; i < headers.length; i++) {
            sb.append(" ").append(padRight(headers[i], columnWidths[i])).append(" |");
        }
        sb.append("\n");

        // Разделитель заголовков
        sb.append("+");
        for (int width : columnWidths) {
            sb.append("=".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        // Данные
        for (String[] row : rows) {
            sb.append("|");
            for (int i = 0; i < headers.length; i++) {
                String cell = (i < row.length) ? row[i] : "";
                sb.append(" ").append(padRight(cell, columnWidths[i])).append(" |");
            }
            sb.append("\n");
        }

        // Нижняя граница
        sb.append("+");
        for (int width : columnWidths) {
            sb.append("-".repeat(width + 2)).append("+");
        }
        sb.append("\n");

        return sb.toString();
    }


    public static String formatBox(String text) {
        String[] lines = text.split("\n");
        int maxWidth = 0;

        for (String line : lines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        StringBuilder sb = new StringBuilder();
        String border = "═".repeat(maxWidth + 4);

        sb.append("╔").append(border).append("╗\n");

        for (String line : lines) {
            sb.append("║ ").append(padRight(line, maxWidth)).append(" ║\n");
        }

        sb.append("╚").append(border).append("╝\n");

        return sb.toString();
    }


    public static String formatHeader(String text) {
        return "\n" + "═".repeat(70) + "\n" +
                "  " + text + "\n" +
                "═".repeat(70) + "\n\n";
    }


    public static String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength - 3) + "...";
    }


    public static String padRight(String text, int length) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= length) {
            return text;
        }

        return text + " ".repeat(length - text.length());
    }

    public static String padLeft(String text, int length) {
        if (text == null) {
            text = "";
        }

        if (text.length() >= length) {
            return text;
        }

        return " ".repeat(length - text.length()) + text;
    }


    public static String formatNumber(int number) {
        return String.format("%,d", number);
    }
}