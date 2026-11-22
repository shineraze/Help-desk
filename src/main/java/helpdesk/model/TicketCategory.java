package helpdesk.model;

/**
 * Категория проблемы. Творческое задание, вариант D "Корпоративный Help Desk":
 * приоритет заявки автоматически выбирается по категории проблемы.
 *
 * <p>Категория инкапсулирует приоритет по умолчанию: снаружи его нельзя
 * подменить, можно только прочитать.</p>
 */
public enum TicketCategory {
    NETWORK("Сеть", TicketPriority.HIGH),
    SOFTWARE("Программное обеспечение", TicketPriority.MEDIUM),
    HARDWARE("Оборудование", TicketPriority.MEDIUM),
    ACCESS("Доступы и учётные записи", TicketPriority.HIGH),
    OTHER("Прочее", TicketPriority.LOW);

    private final String label;
    private final TicketPriority defaultPriority;

    TicketCategory(String label, TicketPriority defaultPriority) {
        this.label = label;
        this.defaultPriority = defaultPriority;
    }

    public String getLabel() {
        return label;
    }

    public TicketPriority getDefaultPriority() {
        return defaultPriority;
    }
}
