package ru.example.helpdesk.repository;

import ru.example.helpdesk.model.TicketDetails;

import java.util.Optional;

/**
 * JOIN-запрос карточки заявки (раздел 34).
 */
public interface TicketDetailsRepository {

    Optional<TicketDetails> findDetails(long ticketId);
}
