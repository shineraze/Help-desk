package helpdesk.exception;

/**
 * Исключение: заявка с указанным идентификатором не найдена в хранилище.
 */
public class TicketNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final long ticketId;

    public TicketNotFoundException(long ticketId) {
        super("Заявка с идентификатором " + ticketId + " не найдена");
        this.ticketId = ticketId;
    }

    public long getTicketId() {
        return ticketId;
    }
}
