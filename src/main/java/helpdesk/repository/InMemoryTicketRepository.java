package helpdesk.repository;

import helpdesk.model.Ticket;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Хранилище заявок в оперативной памяти.
 *
 * <p>LinkedHashMap выбран, чтобы поиск по id был быстрым, а порядок
 * вывода заявок оставался порядком добавления.</p>
 */
public class InMemoryTicketRepository implements TicketRepository {

    private final Map<Long, Ticket> ticketsById = new LinkedHashMap<>();

    @Override
    public void add(Ticket ticket) {
        Objects.requireNonNull(ticket, "Заявка не может быть null");
        if (ticketsById.containsKey(ticket.getId())) {
            throw new IllegalArgumentException("Заявка #" + ticket.getId() + " уже есть в хранилище");
        }
        ticketsById.put(ticket.getId(), ticket);
    }

    @Override
    public Optional<Ticket> findById(long id) {
        return Optional.ofNullable(ticketsById.get(id));
    }

    @Override
    public List<Ticket> findAll() {
        return new ArrayList<>(ticketsById.values());
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        Objects.requireNonNull(status, "Статус не может быть null");
        return ticketsById.values().stream()
                .filter(ticket -> ticket.getStatus() == status)
                .toList();
    }

    @Override
    public List<Ticket> findByPriority(TicketPriority priority) {
        Objects.requireNonNull(priority, "Приоритет не может быть null");
        return ticketsById.values().stream()
                .filter(ticket -> ticket.getPriority() == priority)
                .toList();
    }
}
