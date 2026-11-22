package helpdesk.service;

import helpdesk.exception.TicketNotFoundException;
import helpdesk.model.Customer;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;
import helpdesk.repository.TicketRepository;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Сервисный слой: координирует работу с заявками.
 *
 * <p>Сервис не хранит заявки сам (это делает репозиторий), не решает,
 * допустим ли переход статуса (это делает Ticket) и не знает, как
 * доставляется уведомление (это делает NotificationService).</p>
 */
public class TicketService {

    private final TicketRepository repository;
    private final NotificationService notificationService;
    private final PriorityCalculator priorityCalculator;
    private final AuditService auditService;

    public TicketService(TicketRepository repository,
                         NotificationService notificationService,
                         PriorityCalculator priorityCalculator,
                         AuditService auditService) {
        this.repository = Objects.requireNonNull(repository, "Репозиторий не может быть null");
        this.notificationService = Objects.requireNonNull(notificationService, "Сервис уведомлений не может быть null");
        this.priorityCalculator = Objects.requireNonNull(priorityCalculator, "Калькулятор приоритета не может быть null");
        this.auditService = Objects.requireNonNull(auditService, "Сервис аудита не может быть null");
    }

    /**
     * Создать заявку от имени клиента. Приоритет вычисляется стратегией
     * по категории проблемы (вариант D творческого задания).
     */
    public Ticket createTicket(Customer author, String title, String description, TicketCategory category) {
        Objects.requireNonNull(author, "Автор заявки не может быть null");
        Objects.requireNonNull(category, "Категория заявки не может быть null");

        TicketPriority priority = priorityCalculator.calculate(author, category);
        Ticket ticket = new Ticket(nextId(), title, description, priority, category);
        repository.add(ticket);

        auditService.log(author.getName(), "создание заявки #" + ticket.getId()
                + " (категория: " + category.getLabel() + ", приоритет: " + priority.getLabel() + ")");
        return ticket;
    }

    /**
     * Взять заявку в работу. Роль закреплена типом: клиент не может
     * случайно вызвать этот метод, потому что он требует SupportAgent
     * (задание 12 уровня 2).
     */
    public void startTicket(SupportAgent agent, Ticket ticket) {
        Objects.requireNonNull(agent, "Заявку может взять в работу только специалист");
        agent.assignTicket(ticket);
        notificationService.send("Заявка №" + ticket.getId() + " принята в работу");
        auditService.log(agent.getName(), "взятие в работу заявки #" + ticket.getId());
    }

    /** Отметить проблему решённой (действие специалиста). */
    public void resolveTicket(SupportAgent agent, Ticket ticket) {
        Objects.requireNonNull(agent, "Решить заявку может только специалист");
        ticket.resolve();
        notificationService.send("По заявке №" + ticket.getId() + " найдено решение");
        auditService.log(agent.getName(), "решение по заявке #" + ticket.getId());
    }

    /** Закрыть заявку (действие специалиста). */
    public void closeTicket(SupportAgent agent, Ticket ticket) {
        Objects.requireNonNull(agent, "Закрыть заявку может только специалист");
        ticket.close();
        notificationService.send("Заявка №" + ticket.getId() + " закрыта");
        auditService.log(agent.getName(), "закрытие заявки #" + ticket.getId());
    }

    /** Найти заявку по id или выбросить TicketNotFoundException (задание 16). */
    public Ticket findById(long id) {
        return repository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    public List<Ticket> findAll() {
        return repository.findAll();
    }

    /**
     * Просроченные заявки (задание 18): срок вышел, а заявка ещё не
     * решена и не закрыта.
     */
    public List<Ticket> findOverdueTickets() {
        LocalDateTime now = LocalDateTime.now();
        return repository.findAll().stream()
                .filter(ticket -> ticket.isOverdue(now))
                .toList();
    }

    /**
     * Статистика по статусам (задание 11): сколько заявок находится
     * в каждом состоянии. Статусы без заявок тоже присутствуют со
     * значением 0.
     */
    public Map<TicketStatus, Long> getStatistics() {
        Map<TicketStatus, Long> statistics = new EnumMap<>(TicketStatus.class);
        for (TicketStatus status : TicketStatus.values()) {
            statistics.put(status, 0L);
        }
        for (Ticket ticket : repository.findAll()) {
            statistics.merge(ticket.getStatus(), 1L, Long::sum);
        }
        return statistics;
    }

    /** Следующий свободный идентификатор заявки. */
    private long nextId() {
        return repository.findAll().stream()
                .mapToLong(Ticket::getId)
                .max()
                .orElse(0L) + 1;
    }
}
