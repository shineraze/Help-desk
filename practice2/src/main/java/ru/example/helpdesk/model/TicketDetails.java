package ru.example.helpdesk.model;

import java.time.LocalDateTime;

/**
 * Результат JOIN-запроса карточки заявки (раздел 34).
 */
public record TicketDetails(
        long id,
        String title,
        TicketStatus status,
        TicketPriority priority,
        String categoryName,
        String customerName,
        String assigneeName,
        LocalDateTime createdAt
) {
}
