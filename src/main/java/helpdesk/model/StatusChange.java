package helpdesk.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Одна запись истории изменения статуса (задание 14):
 * старый статус, новый статус и время изменения.
 *
 * <p>record выбран потому, что запись неизменяема по смыслу: после
 * создания её нельзя переписать, можно только прочитать.</p>
 */
public record StatusChange(TicketStatus from, TicketStatus to, LocalDateTime at) {

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public StatusChange {
        Objects.requireNonNull(from, "Старый статус не может быть null");
        Objects.requireNonNull(to, "Новый статус не может быть null");
        Objects.requireNonNull(at, "Время изменения не может быть null");
    }

    @Override
    public String toString() {
        return FORMAT.format(at) + ": " + from + " -> " + to;
    }
}
