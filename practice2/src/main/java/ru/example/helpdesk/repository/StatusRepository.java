package ru.example.helpdesk.repository;

import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.model.TicketStatusHistory;

import java.util.List;

/**
 * Смена статуса заявки одной транзакцией (раздел 31):
 * SELECT FOR UPDATE, UPDATE tickets и INSERT в историю либо вместе, либо никак.
 */
public interface StatusRepository {

    void changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId);

    List<TicketStatusHistory> findHistory(long ticketId);
}
