package helpdesk;

import helpdesk.console.ConsoleMenu;
import helpdesk.model.Administrator;
import helpdesk.model.Customer;
import helpdesk.model.Department;
import helpdesk.model.SupportAgent;
import helpdesk.model.Ticket;
import helpdesk.model.TicketCategory;
import helpdesk.model.TicketPriority;
import helpdesk.model.User;
import helpdesk.repository.InMemoryTicketRepository;
import helpdesk.repository.TicketRepository;
import helpdesk.service.AuditService;
import helpdesk.service.ConsoleNotificationService;
import helpdesk.service.EmailNotificationService;
import helpdesk.service.PriorityCalculator;
import helpdesk.service.TelegramNotificationService;
import helpdesk.service.TicketService;
import helpdesk.service.VipPriorityCalculator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

/**
 * Точка входа в приложение Help Desk.
 *
 * <p>Сценарий из раздела 6 методички выполняется первым, чтобы его вывод
 * было легко сверить. Далее демонстрируются полиморфизм ролей, репозиторий,
 * статистика и смена канала уведомлений, после чего запускается консольное
 * меню с командой «показать просроченные заявки».</p>
 */
public class Main {

    public static void main(String[] args) {
        Department accounting = new Department("Бухгалтерия");
        Department support = new Department("Служба поддержки");
        Department it = new Department("ИТ-отдел");

        Customer customer = new Customer(1, "Анна Петрова", "anna.petrova@mail.ru", accounting, false);
        SupportAgent agent = new SupportAgent(2, "Сергей Иванов", "sergey.ivanov@helpdesk.ru", support);
        Administrator administrator = new Administrator(3, "Олег Смирнов", "admin@helpdesk.ru", it);

        TicketRepository repository = new InMemoryTicketRepository();
        AuditService auditService = new AuditService();
        PriorityCalculator priorityCalculator = new VipPriorityCalculator();
        TicketService ticketService = new TicketService(repository, new ConsoleNotificationService(),
                priorityCalculator, auditService);

        // --- Обязательный сценарий (раздел 6) ---
        System.out.println("=== HELP DESK ===");
        System.out.println("Клиент: " + customer.getName());

        Ticket ticket1 = ticketService.createTicket(customer, "Не работает Wi-Fi",
                "После перезагрузки роутера пропал доступ в сеть", TicketCategory.NETWORK);
        System.out.println("Заявка #" + ticket1.getId() + ": " + ticket1.getTitle()
                + " | " + ticket1.getStatus());

        ticketService.startTicket(agent, ticket1);
        System.out.println("Статус: " + ticket1.getStatus());

        ticketService.resolveTicket(agent, ticket1);
        System.out.println("Статус: " + ticket1.getStatus());

        ticketService.closeTicket(agent, ticket1);
        System.out.println("Статус: " + ticket1.getStatus());

        // --- Полиморфизм: одна строка кода, разное поведение (задание 8) ---
        System.out.println();
        System.out.println("=== РОЛИ ПОЛЬЗОВАТЕЛЕЙ ===");
        List<User> users = List.of(customer, agent, administrator);
        for (User user : users) {
            user.performAction();
        }

        // --- Ещё две заявки и вывод содержимого репозитория (задание 12) ---
        ticketService.createTicket(customer, "Ошибка приложения",
                "Приложение вылетает при запуске", TicketCategory.SOFTWARE);
        ticketService.createTicket(customer, "Не печатает принтер",
                "Принтер не реагирует на команду печати", TicketCategory.HARDWARE);

        System.out.println();
        System.out.println("Список всех заявок в репозитории:");
        for (Ticket ticket : ticketService.findAll()) {
            System.out.println(ticket);
        }

        // --- Статистика по статусам (задание 11) ---
        System.out.println();
        System.out.println("Статистика по статусам:");
        ticketService.getStatistics().forEach((status, count) ->
                System.out.println("  " + status + ": " + count));

        // --- Смена канала уведомлений без изменения TicketService (задание 10) ---
        System.out.println();
        System.out.println("=== СМЕНА КАНАЛА УВЕДОМЛЕНИЙ ===");
        TicketService emailTicketService = new TicketService(repository,
                new EmailNotificationService(customer.getEmail()), priorityCalculator, auditService);
        Ticket ticket4 = emailTicketService.createTicket(customer, "Забыл пароль от учётной записи",
                "Не получается войти в корпоративную почту", TicketCategory.ACCESS);
        emailTicketService.startTicket(agent, ticket4);

        TicketService telegramTicketService = new TicketService(repository,
                new TelegramNotificationService("@helpdesk_bot"), priorityCalculator, auditService);
        Ticket ticket5 = telegramTicketService.createTicket(customer, "Медленно работает сеть",
                "Скорость упала после обновления оборудования", TicketCategory.NETWORK);
        telegramTicketService.startTicket(agent, ticket5);

        // Заявка с истёкшим сроком, чтобы команда overdue что-то показала.
        repository.add(new Ticket(6, "Не работает VPN",
                "Удалённый сотрудник не может подключиться к корпоративной сети",
                TicketPriority.MEDIUM, TicketCategory.NETWORK, LocalDateTime.now().minusHours(10)));

        // --- Консольная команда «показать просроченные заявки» (задание 18) ---
        new ConsoleMenu(ticketService, new Scanner(System.in)).start();
    }
}
