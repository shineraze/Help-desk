package helpdesk.model;

import helpdesk.exception.InvalidTicketStateException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Заявка в службу поддержки.
 *
 * <p>Класс сам защищает своё состояние: поля закрыты, статус меняется
 * только через бизнес-методы, которые проверяют допустимость перехода.
 * Именно Ticket — а не Main и не TicketService — отвечает на вопрос
 * «можно ли сейчас перевести заявку в другой статус».</p>
 *
 * <p>Допустимые переходы:</p>
 * <pre>
 * NEW         -> IN_PROGRESS, CANCELLED
 * IN_PROGRESS -> RESOLVED, CANCELLED
 * RESOLVED    -> CLOSED, CANCELLED
 * CLOSED      -> (конечное состояние)
 * CANCELLED   -> (конечное состояние)
 * </pre>
 */
public class Ticket {

    private final long id;
    private final String title;
    private final String description;
    private final TicketPriority priority;
    private final TicketCategory category;
    private final LocalDateTime createdAt;
    private final LocalDateTime deadline;
    private final List<StatusChange> history = new ArrayList<>();

    private TicketStatus status;

    public Ticket(long id, String title, String description, TicketPriority priority,
                  TicketCategory category) {
        this(id, title, description, priority, category, LocalDateTime.now());
    }

    /**
     * Конструктор с явным временем создания: используется в тестах и для
     * демонстрации просроченной заявки, когда нельзя ждать несколько часов.
     */
    public Ticket(long id, String title, String description, TicketPriority priority,
                  TicketCategory category, LocalDateTime createdAt) {
        this.id = id;
        this.title = requireText(title, "Название заявки");
        this.description = description == null ? "" : description.trim();
        this.priority = Objects.requireNonNull(priority, "Приоритет заявки не может быть null");
        this.category = Objects.requireNonNull(category, "Категория заявки не может быть null");
        this.createdAt = Objects.requireNonNull(createdAt, "Время создания заявки не может быть null");
        this.deadline = createdAt.plus(this.priority.getSla());
        this.status = TicketStatus.NEW;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TicketPriority getPriority() {
        return priority;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Крайний срок решения, рассчитанный по SLA приоритета. */
    public LocalDateTime getDeadline() {
        return deadline;
    }

    /** История изменений статуса: старый статус, новый статус, время. */
    public List<StatusChange> getHistory() {
        return List.copyOf(history);
    }

    /**
     * Взять заявку в работу.
     * Разрешено только из NEW.
     */
    public void startProcessing() {
        if (status != TicketStatus.NEW) {
            throw new InvalidTicketStateException(
                    "Заявку #" + id + " нельзя взять в работу из статуса " + status
                            + ": это возможно только для NEW");
        }
        transitionTo(TicketStatus.IN_PROGRESS);
    }

    /**
     * Отметить проблему решённой.
     * Разрешено только из IN_PROGRESS.
     */
    public void resolve() {
        if (status != TicketStatus.IN_PROGRESS) {
            throw new InvalidTicketStateException(
                    "Заявку #" + id + " нельзя решить из статуса " + status
                            + ": сначала её нужно взять в работу");
        }
        transitionTo(TicketStatus.RESOLVED);
    }

    /**
     * Закрыть заявку.
     * Разрешено только из RESOLVED: нельзя закрыть то, что не решено.
     */
    public void close() {
        if (status != TicketStatus.RESOLVED) {
            throw new InvalidTicketStateException(
                    "Заявку #" + id + " нельзя закрыть из статуса " + status
                            + ": закрывать можно только решённую заявку");
        }
        transitionTo(TicketStatus.CLOSED);
    }

    /**
     * Отменить заявку.
     * Запрещено для уже закрытой и уже отменённой заявки.
     */
    public void cancel() {
        if (status == TicketStatus.CLOSED) {
            throw new InvalidTicketStateException("Заявку #" + id + " нельзя отменить: она уже закрыта");
        }
        if (status == TicketStatus.CANCELLED) {
            throw new InvalidTicketStateException("Заявка #" + id + " уже отменена");
        }
        transitionTo(TicketStatus.CANCELLED);
    }

    /**
     * Просрочена ли заявка на указанный момент: срок вышел, а заявка
     * ещё не решена и не закрыта (задание 18 уровня 3).
     */
    public boolean isOverdue(LocalDateTime moment) {
        return moment.isAfter(deadline)
                && status != TicketStatus.RESOLVED
                && status != TicketStatus.CLOSED;
    }

    /**
     * Краткое представление для списков: «#1 Не работает Wi-Fi | NEW»
     * (задание 5 уровня 1 — переопределение toString()).
     */
    @Override
    public String toString() {
        return "#" + id + " " + title + " | " + status;
    }

    /**
     * Единственное место, где меняется статус. Здесь же фиксируется
     * история изменений, поэтому забыть записать переход невозможно.
     */
    private void transitionTo(TicketStatus target) {
        TicketStatus previous = status;
        status = target;
        history.add(new StatusChange(previous, target, LocalDateTime.now()));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value.trim();
    }
}
