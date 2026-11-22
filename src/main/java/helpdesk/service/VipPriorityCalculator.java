package helpdesk.service;

import helpdesk.model.Customer;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;

import java.util.Objects;

/**
 * Правило для VIP-клиентов: приоритет категории повышается на одну
 * ступень (LOW -> MEDIUM -> HIGH -> CRITICAL).
 *
 * <p>Тот же интерфейс, другое поведение — классический полиморфизм:
 * TicketService не меняется при подключении нового правила.</p>
 */
public class VipPriorityCalculator implements PriorityCalculator {

    private final PriorityCalculator base;

    public VipPriorityCalculator() {
        this(new DefaultPriorityCalculator());
    }

    public VipPriorityCalculator(PriorityCalculator base) {
        this.base = Objects.requireNonNull(base, "Базовая стратегия не может быть null");
    }

    @Override
    public TicketPriority calculate(Customer customer, TicketCategory category) {
        TicketPriority priority = base.calculate(customer, category);
        return customer != null && customer.isVip() ? priority.bump() : priority;
    }
}
