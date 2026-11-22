package helpdesk;

import helpdesk.exception.InvalidTicketStateException;
import helpdesk.model.Ticket;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;
import helpdesk.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Unit-тесты модели Ticket (задание 20 уровня 3): корректный жизненный
 * цикл и запрещённые переходы. Запуск: {@code java -cp ... helpdesk.TicketTest}.
 */
public final class TicketTest {

    private static int passed;
    private static int failed;

    private TicketTest() {
    }

    public static void main(String[] args) {
        run("новая заявка создаётся в статусе NEW с дедлайном по SLA", TicketTest::newTicketIsNew);
        run("полный жизненный цикл NEW -> IN_PROGRESS -> RESOLVED -> CLOSED", TicketTest::fullLifecycle);
        run("нельзя решить заявку в статусе NEW", TicketTest::cannotResolveNewTicket);
        run("нельзя закрыть нерешённую заявку", TicketTest::cannotCloseNotResolvedTicket);
        run("нельзя отменить закрытую заявку", TicketTest::cannotCancelClosedTicket);

        System.out.println("Пройдено: " + passed + ", провалено: " + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void newTicketIsNew() {
        LocalDateTime created = LocalDateTime.of(2026, 1, 10, 9, 0);
        Ticket ticket = new Ticket(1, "Не работает Wi-Fi", "нет сети",
                TicketPriority.HIGH, TicketCategory.NETWORK, created);

        assertEquals(TicketStatus.NEW, ticket.getStatus(), "начальный статус");
        assertEquals(created, ticket.getCreatedAt(), "время создания");
        assertEquals(created.plusMinutes(120), ticket.getDeadline(), "дедлайн HIGH = 2 часа");
    }

    private static void fullLifecycle() {
        Ticket ticket = ticket();

        ticket.startProcessing();
        assertEquals(TicketStatus.IN_PROGRESS, ticket.getStatus(), "после взятия в работу");

        ticket.resolve();
        assertEquals(TicketStatus.RESOLVED, ticket.getStatus(), "после решения");

        ticket.close();
        assertEquals(TicketStatus.CLOSED, ticket.getStatus(), "после закрытия");
        assertEquals(3, ticket.getHistory().size(), "в истории три перехода");
    }

    private static void cannotResolveNewTicket() {
        Ticket ticket = ticket();
        assertThrows(InvalidTicketStateException.class, ticket::resolve,
                "решить заявку в статусе NEW нельзя");
        assertEquals(TicketStatus.NEW, ticket.getStatus(), "статус не изменился");
    }

    private static void cannotCloseNotResolvedTicket() {
        Ticket ticket = ticket();
        assertThrows(InvalidTicketStateException.class, ticket::close,
                "закрыть заявку в статусе NEW нельзя");

        ticket.startProcessing();
        assertThrows(InvalidTicketStateException.class, ticket::close,
                "закрыть заявку в статусе IN_PROGRESS нельзя");
    }

    private static void cannotCancelClosedTicket() {
        Ticket ticket = ticket();
        ticket.startProcessing();
        ticket.resolve();
        ticket.close();

        assertThrows(InvalidTicketStateException.class, ticket::cancel,
                "отменить закрытую заявку нельзя");
    }

    private static Ticket ticket() {
        return new Ticket(1, "Не работает Wi-Fi", "нет сети",
                TicketPriority.HIGH, TicketCategory.NETWORK);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("  [OK]   " + name);
        } catch (Throwable failure) {
            failed++;
            System.out.println("  [FAIL] " + name + " -> " + failure.getMessage());
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + ": ожидалось <" + expected + ">, получено <" + actual + ">");
        }
    }

    private static void assertThrows(Class<? extends Throwable> expected, Runnable action, String message) {
        try {
            action.run();
        } catch (Throwable actual) {
            if (expected.isInstance(actual)) {
                return;
            }
            throw new AssertionError(message + ": ожидалось " + expected.getSimpleName()
                    + ", получено " + actual.getClass().getSimpleName(), actual);
        }
        throw new AssertionError(message + ": ожидалось " + expected.getSimpleName()
                + ", но исключение не было выброшено");
    }
}
