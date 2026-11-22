package helpdesk.model;

import java.time.Duration;

/**
 * Приоритет заявки. Приоритет задаётся при создании и доступен только
 * для чтения: изменить его через setter нельзя.
 *
 * <p>Здесь же живёт бизнес-правило SLA (задание 13): для каждого
 * приоритета определён срок реакции. Знание "сколько времени даётся"
 * принадлежит самому приоритету, а не сервису.</p>
 */
public enum TicketPriority {
    LOW("Низкий", Duration.ofHours(24)),
    MEDIUM("Средний", Duration.ofHours(8)),
    HIGH("Высокий", Duration.ofHours(2)),
    CRITICAL("Критический", Duration.ofMinutes(30));

    private final String label;
    private final Duration sla;

    TicketPriority(String label, Duration sla) {
        this.label = label;
        this.sla = sla;
    }

    public String getLabel() {
        return label;
    }

    /** Срок, за который заявка должна быть решена. */
    public Duration getSla() {
        return sla;
    }

    /** Следующий, более высокий приоритет (CRITICAL остаётся CRITICAL). */
    public TicketPriority bump() {
        return switch (this) {
            case LOW -> MEDIUM;
            case MEDIUM -> HIGH;
            case HIGH, CRITICAL -> CRITICAL;
        };
    }
}
