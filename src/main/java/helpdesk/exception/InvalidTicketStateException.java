package helpdesk.exception;

/**
 * Исключение бизнес-правила: попытка выполнить действие, недопустимое
 * для текущего состояния заявки (например, решить заявку, которую ещё
 * не взяли в работу).
 */
public class InvalidTicketStateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTicketStateException(String message) {
        super(message);
    }
}
