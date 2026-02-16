package rbac;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;


public class TemporaryAssignment extends AbstractRoleAssignment {
    // Форматер для парсинга даты
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String expiresAt;      // Дата истечения срока
    private boolean autoRenew = false; // Флаг автоматического продления

    /**
     * Конструктор временного назначения
     * @param expiresAt дата истечения в формате "ГГГГ-ММ-ДД" или "ГГГГ-ММ-ДД ЧЧ:ММ"
     */
    public TemporaryAssignment(User user, Role role, AssignmentMetadata metadata,
                               String expiresAt) {
        super(user, role, metadata);
        setExpiresAt(expiresAt);
    }


    private void setExpiresAt(String expiresAt) {
        if (expiresAt == null || expiresAt.trim().isEmpty()) {
            throw new IllegalArgumentException("Дата истечения не может быть пустой или null");
        }
        if (!expiresAt.matches("\\d{4}-\\d{2}-\\d{2}( \\d{2}:\\d{2})?")) {
            throw new IllegalArgumentException(
                    "Дата истечения должна быть в формате 'ГГГГ-ММ-ДД' или 'ГГГГ-ММ-ДД ЧЧ:ММ'"
            );
        }
        this.expiresAt = expiresAt.trim();
    }

    @Override
    public boolean isActive() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = parseExpiryDate();
        return now.isBefore(expiry);
    }

    
    private LocalDateTime parseExpiryDate() {
        String dt = expiresAt.length() == 10 ? expiresAt + " 23:59" : expiresAt;
        return LocalDateTime.parse(dt, FORMATTER);
    }

    @Override public String assignmentType() { return "ВРЕМЕННОЕ"; }

    public void extend(String newExpirationDate) {
        setExpiresAt(newExpirationDate);
    }

        public boolean isExpired() {
        return !isActive();
    }

    public String expiresAt() {
        return expiresAt;
    }

    public boolean autoRenew() {
        return autoRenew;
    }

    public void setAutoRenew(boolean autoRenew) {
        this.autoRenew = autoRenew;
    }

    /**
     * Получение оставшегося времени до истечения
     */
    public String getTimeRemaining() {
        if (isExpired()) return "Истёк";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiry = parseExpiryDate();
        long days = ChronoUnit.DAYS.between(now, expiry);
        if (days > 0) return days + " дн.";
        long hours = ChronoUnit.HOURS.between(now, expiry);
        if (hours > 0) return hours + " ч.";
        long minutes = ChronoUnit.MINUTES.between(now, expiry);
        return minutes + " мин.";
    }

    @Override
    public String summary() {
        return super.summary() +
                "\nИстекает: " + expiresAt +
                (autoRenew ? " (автопродление включено)" : "") +
                "\nОсталось: " + getTimeRemaining();
    }
}
