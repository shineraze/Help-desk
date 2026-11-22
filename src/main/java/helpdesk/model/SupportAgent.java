package helpdesk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Специалист поддержки — пользователь, который обрабатывает заявки.
 *
 * <p>Специалист хранит список назначенных ему заявок (задание 6 уровня 2).
 * Назначение переводит заявку NEW -> IN_PROGRESS; допустимость перехода
 * проверяет сама заявка.</p>
 */
public class SupportAgent extends User {

    private final List<Ticket> assignedTickets = new ArrayList<>();

    public SupportAgent(long id, String name, String email, Department department) {
        super(id, name, email, department);
    }

    /**
     * Назначить заявку на себя. Если заявка не в состоянии NEW,
     * {@link Ticket#startProcessing()} выбросит исключение, и список
     * назначенных заявок останется корректным.
     */
    public void assignTicket(Ticket ticket) {
        ticket.startProcessing();
        assignedTickets.add(ticket);
    }

    /** Копия списка: внешний код не может изменить внутреннюю коллекцию. */
    public List<Ticket> getAssignedTickets() {
        return List.copyOf(assignedTickets);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " обрабатывает заявку");
    }
}
