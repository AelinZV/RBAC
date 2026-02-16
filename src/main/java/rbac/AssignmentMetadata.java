package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record AssignmentMetadata(String assignedBy, String assignedAt, String reason) {

    // Форматер для даты и времени
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    
    public static AssignmentMetadata now(String assignedBy, String reason) {
        String now = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, now, reason);
    }

    
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("Назначен администратором: ").append(assignedBy)
                .append(" в ").append(assignedAt);
        if (reason != null && !reason.trim().isEmpty()) {
            sb.append("\nПричина: ").append(reason.trim());
        }
        return sb.toString();
    }
}
