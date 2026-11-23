package ru.example.helpdesk.model;

/**
 * Строка отчёта «количество заявок по категориям» (раздел 35).
 */
public record CategoryTicketCount(String categoryName, long ticketCount) {
}
