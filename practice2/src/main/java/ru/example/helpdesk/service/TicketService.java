package ru.example.helpdesk.service;

import ru.example.helpdesk.model.AgentLoad;
import ru.example.helpdesk.model.CategoryTicketCount;
import ru.example.helpdesk.model.Ticket;
import ru.example.helpdesk.model.TicketComment;
import ru.example.helpdesk.model.TicketDetails;
import ru.example.helpdesk.model.TicketPriority;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.model.TicketStatusHistory;
import ru.example.helpdesk.model.User;
import ru.example.helpdesk.model.UserRole;
import ru.example.helpdesk.repository.CommentRepository;
import ru.example.helpdesk.repository.ReportRepository;
import ru.example.helpdesk.repository.StatusRepository;
import ru.example.helpdesk.repository.TicketDetailsRepository;
import ru.example.helpdesk.repository.TicketRepository;
import ru.example.helpdesk.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Бизнес-логика над репозиторием (раздел 30).
 * SQL и преобразование строк БД в объекты живут в repository.
 */
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final StatusRepository statusRepository;
    private final TicketDetailsRepository detailsRepository;
    private final ReportRepository reportRepository;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         CommentRepository commentRepository,
                         StatusRepository statusRepository,
                         TicketDetailsRepository detailsRepository,
                         ReportRepository reportRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.statusRepository = statusRepository;
        this.detailsRepository = detailsRepository;
        this.reportRepository = reportRepository;
    }

    public Ticket createTicket(String title,
                               String description,
                               TicketPriority priority,
                               long customerId,
                               Long categoryId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Пустая тема заявки");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(title.trim());
        ticket.setDescription(description);
        ticket.setStatus(TicketStatus.NEW);
        ticket.setPriority(priority);
        ticket.setCustomerId(customerId);
        ticket.setCategoryId(categoryId);

        return ticketRepository.save(ticket);
    }

    public Optional<Ticket> findById(long id) {
        return ticketRepository.findById(id);
    }

    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findUserById(long id) {
        return userRepository.findById(id);
    }

    /**
     * Назначение заявки сотруднику (раздел 32): проверяем, что пользователь
     * существует и имеет роль SUPPORT_AGENT.
     */
    public void assignTicket(long ticketId, long supportAgentId) {
        User agent = userRepository.findById(supportAgentId)
                .orElseThrow(() -> new IllegalArgumentException("Сотрудник не найден"));

        if (agent.getRole() != UserRole.SUPPORT_AGENT) {
            throw new IllegalArgumentException("Назначить заявку можно только сотруднику поддержки");
        }

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена"));

        if (ticket.getStatus() == TicketStatus.CLOSED
                || ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new IllegalStateException("Нельзя назначить закрытую или отменённую заявку");
        }

        ticket.setAssigneeId(supportAgentId);
        if (ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        ticketRepository.update(ticket);
    }

    public void changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId) {
        statusRepository.changeStatus(ticketId, newStatus, changedByUserId);
    }

    public TicketComment addComment(long ticketId, long authorId, String text, boolean internal) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Пустой комментарий");
        }

        TicketComment comment = new TicketComment();
        comment.setTicketId(ticketId);
        comment.setAuthorId(authorId);
        comment.setText(text.trim());
        comment.setInternal(internal);

        return commentRepository.add(comment);
    }

    public List<TicketComment> getComments(long ticketId) {
        return commentRepository.findByTicketId(ticketId);
    }

    public List<TicketStatusHistory> getHistory(long ticketId) {
        return statusRepository.findHistory(ticketId);
    }

    public Optional<TicketDetails> getDetails(long ticketId) {
        return detailsRepository.findDetails(ticketId);
    }

    public Map<String, Long> countByStatus() {
        return reportRepository.countByStatus();
    }

    public List<CategoryTicketCount> countByCategory() {
        return reportRepository.countByCategory();
    }

    public List<AgentLoad> agentLoad() {
        return reportRepository.agentLoad();
    }
}
