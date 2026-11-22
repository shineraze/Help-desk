package helpdesk.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Простой журнал ключевых действий пользователей (задание 19 уровня 3).
 *
 * <p>Аудит — отдельная ответственность: TicketService не занимается
 * форматированием и хранением журнала.</p>
 */
public class AuditService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final List<String> entries = new ArrayList<>();

    public void log(String actor, String action) {
        entries.add(TIME.format(LocalDateTime.now()) + " | " + actor + " | " + action);
    }

    public List<String> getEntries() {
        return List.copyOf(entries);
    }
}
