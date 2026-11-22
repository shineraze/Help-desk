package helpdesk.model;

/**
 * Статус заявки. Enum вместо строки: компилятор не даст написать
 * "IN_PROGRES" или "БАНАН" и ограничивает набор допустимых значений.
 */
public enum TicketStatus {
    NEW,
    IN_PROGRESS,
    RESOLVED,
    CLOSED,
    CANCELLED
}
