package helpdesk.service;

import helpdesk.model.Customer;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;

/**
 * Стратегия выбора приоритета новой заявки (задание 17).
 *
 * <p>Приоритет зависит от правил компании, поэтому вынесен в отдельный
 * интерфейс: TicketService не знает, по какой формуле он посчитан.</p>
 */
public interface PriorityCalculator {

    TicketPriority calculate(Customer customer, TicketCategory category);
}
