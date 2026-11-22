package helpdesk.console;

import helpdesk.model.Ticket;
import helpdesk.service.TicketService;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Консольное меню с командой «показать просроченные заявки»
 * (задание 18 уровня 3).
 */
public class ConsoleMenu {

    private final TicketService ticketService;
    private final Scanner scanner;

    public ConsoleMenu(TicketService ticketService, Scanner scanner) {
        this.ticketService = ticketService;
        this.scanner = scanner;
    }

    public void start() {
        System.out.println();
        System.out.println("Введите команду: overdue — просроченные заявки, exit — выход.");
        while (scanner.hasNextLine()) {
            System.out.print("> ");
            String command = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
            if (command.equals("exit") || command.equals("quit")) {
                return;
            }
            if (command.equals("overdue")) {
                printOverdueTickets();
            } else if (!command.isEmpty()) {
                System.out.println("Неизвестная команда: " + command);
            }
        }
    }

    private void printOverdueTickets() {
        List<Ticket> overdue = ticketService.findOverdueTickets();
        if (overdue.isEmpty()) {
            System.out.println("Просроченных заявок нет.");
            return;
        }
        System.out.println("Просроченные заявки (" + overdue.size() + "):");
        overdue.forEach(ticket -> System.out.println("  " + ticket));
    }
}
