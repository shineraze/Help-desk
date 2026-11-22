package helpdesk.service;

import helpdesk.model.Customer;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;

import java.util.Objects;

/**
 * Обычное правило: приоритет берётся из категории проблемы
 * (творческое задание, вариант D).
 */
public class DefaultPriorityCalculator implements PriorityCalculator {

    @Override
    public TicketPriority calculate(Customer customer, TicketCategory category) {
        Objects.requireNonNull(category, "Категория не может быть null");
        return category.getDefaultPriority();
    }
}
