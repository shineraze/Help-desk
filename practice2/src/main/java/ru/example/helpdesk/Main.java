package ru.example.helpdesk;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.ArticleCategory;
import ru.example.helpdesk.model.Category;
import ru.example.helpdesk.model.KnowledgeArticle;
import ru.example.helpdesk.model.Ticket;
import ru.example.helpdesk.model.TicketComment;
import ru.example.helpdesk.model.TicketDetails;
import ru.example.helpdesk.model.TicketPriority;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.model.TicketStatusHistory;
import ru.example.helpdesk.model.User;
import ru.example.helpdesk.repository.CategoryRepository;
import ru.example.helpdesk.repository.CommentRepository;
import ru.example.helpdesk.repository.ReportRepository;
import ru.example.helpdesk.repository.StatusRepository;
import ru.example.helpdesk.repository.TicketDetailsRepository;
import ru.example.helpdesk.repository.TicketRepository;
import ru.example.helpdesk.repository.UserRepository;
import ru.example.helpdesk.repository.jdbc.JdbcCategoryRepository;
import ru.example.helpdesk.repository.jdbc.JdbcCommentRepository;
import ru.example.helpdesk.repository.jdbc.JdbcKnowledgeBaseRepository;
import ru.example.helpdesk.repository.jdbc.JdbcReportRepository;
import ru.example.helpdesk.repository.jdbc.JdbcStatusRepository;
import ru.example.helpdesk.repository.jdbc.JdbcTicketDetailsRepository;
import ru.example.helpdesk.repository.jdbc.JdbcTicketRepository;
import ru.example.helpdesk.repository.jdbc.JdbcUserRepository;
import ru.example.helpdesk.service.KnowledgeBaseService;
import ru.example.helpdesk.service.TicketService;

import java.sql.Connection;
import java.util.List;

/**
 * Обязательный сценарий (раздел 36) и демонстрация варианта A (раздел 42).
 */
public class Main {

    public static void main(String[] args) throws Exception {
        TicketRepository ticketRepository = new JdbcTicketRepository();
        UserRepository userRepository = new JdbcUserRepository();
        CommentRepository commentRepository = new JdbcCommentRepository();
        StatusRepository statusRepository = new JdbcStatusRepository();
        TicketDetailsRepository detailsRepository = new JdbcTicketDetailsRepository();
        ReportRepository reportRepository = new JdbcReportRepository();
        CategoryRepository categoryRepository = new JdbcCategoryRepository();

        TicketService ticketService = new TicketService(
                ticketRepository,
                userRepository,
                commentRepository,
                statusRepository,
                detailsRepository,
                reportRepository
        );
        KnowledgeBaseService knowledgeBaseService =
                new KnowledgeBaseService(new JdbcKnowledgeBaseRepository());

        // 1. Подключиться к PostgreSQL.
        try (Connection connection = DatabaseConfig.getConnection()) {
            System.out.println("Подключение к PostgreSQL: "
                    + connection.getMetaData().getDatabaseProductVersion());
        }

        // 2. Найти клиента по email.
        User customer = ticketService.findUserByEmail("anna@example.org")
                .orElseThrow(() -> new IllegalStateException("Клиент не найден"));
        System.out.println("Клиент: #" + customer.getId() + " " + customer.getName()
                + " <" + customer.getEmail() + ">");

        // 3. Вывести список категорий.
        List<Category> categories = categoryRepository.findAllCategories();
        System.out.println("Категории:");
        for (Category category : categories) {
            System.out.println("  #" + category.getId() + " " + category.getName()
                    + " (приоритет по умолчанию: " + category.getDefaultPriority() + ")");
        }

        // 4. Создать новую заявку от имени клиента.
        Category network = categories.stream()
                .filter(category -> category.getName().equals("Сеть"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Категория «Сеть» не найдена"));

        Ticket ticket = ticketService.createTicket(
                "Не работает VPN",
                "После обновления клиент VPN не подключается к корпоративной сети",
                TicketPriority.HIGH,
                customer.getId(),
                network.getId()
        );

        // 5. Получить её id из PostgreSQL.
        System.out.println("Создана заявка id=" + ticket.getId()
                + ", статус=" + ticket.getStatus());

        // 6. Вывести заявку через findById.
        Ticket loaded = ticketService.findById(ticket.getId())
                .orElseThrow(() -> new IllegalStateException("Заявка не найдена"));
        System.out.println("findById: #" + loaded.getId() + " " + loaded.getTitle()
                + " | " + loaded.getStatus() + " | " + loaded.getPriority());

        // 7. Назначить сотрудника поддержки.
        User agent = ticketService.findUserByEmail("ivan.support@example.org")
                .orElseThrow(() -> new IllegalStateException("Сотрудник не найден"));
        ticketService.assignTicket(loaded.getId(), agent.getId());
        System.out.println("Назначен сотрудник: #" + agent.getId() + " " + agent.getName());

        // 8. Добавить клиентский комментарий.
        ticketService.addComment(ticket.getId(), customer.getId(),
                "Проблема сохраняется после перезагрузки ноутбука", false);

        // 9. Изменить статус IN_PROGRESS -> RESOLVED.
        ticketService.changeStatus(ticket.getId(), TicketStatus.RESOLVED, agent.getId());

        // 10. Добавить внутренний комментарий сотрудника.
        ticketService.addComment(ticket.getId(), agent.getId(),
                "Переустановлен клиент VPN, профиль проверен", true);

        // 11. Изменить RESOLVED -> CLOSED.
        ticketService.changeStatus(ticket.getId(), TicketStatus.CLOSED, agent.getId());

        // 12. Вывести историю статусов заявки.
        System.out.println("История статусов:");
        for (TicketStatusHistory history : ticketService.getHistory(ticket.getId())) {
            System.out.println("  " + history.getChangedAt() + ": "
                    + history.getOldStatus() + " -> " + history.getNewStatus()
                    + " (" + history.getChangedByName() + ")");
        }

        // 13. Выполнить JOIN-запрос: клиент, сотрудник, категория.
        TicketDetails details = ticketService.getDetails(ticket.getId())
                .orElseThrow(() -> new IllegalStateException("Карточка заявки не найдена"));
        System.out.println("Карточка заявки: #" + details.id() + " " + details.title()
                + " | " + details.status() + " | " + details.priority()
                + " | категория: " + details.categoryName()
                + " | клиент: " + details.customerName()
                + " | исполнитель: " + details.assigneeName());

        System.out.println("Комментарии:");
        for (TicketComment comment : ticketService.getComments(ticket.getId())) {
            System.out.println("  [" + (comment.isInternal() ? "внутренний" : "клиент")
                    + "] " + comment.getAuthorName() + ": " + comment.getText());
        }

        System.out.println("Отчёт по статусам: " + ticketService.countByStatus());
        System.out.println("Отчёт по категориям: " + ticketService.countByCategory());
        System.out.println("Нагрузка сотрудников: " + ticketService.agentLoad());

        // 14. Доказательство сохранения: данные читаются из БД после операций.
        System.out.println("Все заявки в БД:");
        for (Ticket item : ticketService.findAll()) {
            System.out.println("  #" + item.getId() + " " + item.getTitle()
                    + " | " + item.getStatus() + " | " + item.getPriority());
        }

        demoKnowledgeBase(knowledgeBaseService, ticket, agent.getId());
    }

    /**
     * Вариант A «База знаний»: две таблицы, транзакция публикации и три запроса.
     */
    private static void demoKnowledgeBase(KnowledgeBaseService knowledgeBaseService,
                                          Ticket ticket,
                                          long authorId) {
        System.out.println();
        System.out.println("=== Вариант A. База знаний ===");

        List<ArticleCategory> categories = knowledgeBaseService.getCategories();
        System.out.println("Категории статей:");
        for (ArticleCategory category : categories) {
            System.out.println("  #" + category.getId() + " " + category.getName());
        }

        ArticleCategory network = categories.stream()
                .filter(category -> category.getName().equals("Сеть"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Категория статей не найдена"));

        KnowledgeArticle article = knowledgeBaseService.publishArticle(
                "VPN не подключается после обновления",
                "Переустановите клиент VPN и проверьте профиль подключения.",
                network.getId(),
                authorId,
                ticket.getId()
        );
        System.out.println("Опубликована статья id=" + article.getId()
                + ", связана с заявкой #" + article.getSourceTicketId());

        System.out.println("Статьи категории «Сеть»:");
        for (KnowledgeArticle item : knowledgeBaseService.findByCategory(network.getId())) {
            System.out.println("  #" + item.getId() + " " + item.getTitle()
                    + " (автор: " + item.getAuthorName() + ")");
        }

        System.out.println("Поиск по тексту «VPN»:");
        for (KnowledgeArticle item : knowledgeBaseService.search("VPN")) {
            System.out.println("  #" + item.getId() + " " + item.getTitle());
        }

        KnowledgeArticle card = knowledgeBaseService.findById(article.getId())
                .orElseThrow(() -> new IllegalStateException("Статья не найдена"));
        System.out.println("Карточка статьи: #" + card.getId() + " " + card.getTitle()
                + " | категория: " + card.getCategoryName()
                + " | автор: " + card.getAuthorName()
                + " | заявка: " + card.getSourceTicketTitle());
    }
}
