package ru.example.helpdesk.model;

/**
 * Строка отчёта «нагрузка сотрудников» (раздел 35).
 */
public record AgentLoad(String agentName, long activeTickets) {
}
