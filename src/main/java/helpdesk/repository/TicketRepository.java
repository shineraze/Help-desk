package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;

import java.util.List;
import java.util.Optional;

/**
 * Абстракция хранилища заявок (задание 15).
 *
 * <p>TicketService зависит от этого интерфейса, а не от конкретного
 * класса. Сегодня заявки лежат в памяти, завтра — в файле или базе:
 * сервис при этом не меняется.</p>
 */
public interface TicketRepository {

    /** Сохранить заявку. */
    void add(Ticket ticket);

    /** Найти заявку по идентификатору. Пустой Optional, если заявки нет. */
    Optional<Ticket> findById(long id);

    /** Все заявки в порядке добавления. */
    List<Ticket> findAll();

    /** Заявки выбранного статуса (задание 8). */
    List<Ticket> findByStatus(TicketStatus status);

    /** Заявки выбранного приоритета (задание 9). */
    List<Ticket> findByPriority(TicketPriority priority);
}
