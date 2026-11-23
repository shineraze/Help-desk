package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.Ticket;
import ru.example.helpdesk.model.TicketPriority;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.repository.TicketRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTicketRepository implements TicketRepository {

    private static final String INSERT_SQL = """
            INSERT INTO tickets(
                title, description, status, priority,
                customer_id, assignee_id, category_id
            )
            VALUES (?, ?, ?::ticket_status, ?::ticket_priority, ?, ?, ?)
            RETURNING id, created_at, updated_at
            """;

    private static final String UPDATE_SQL = """
            UPDATE tickets
            SET title = ?,
                description = ?,
                status = ?::ticket_status,
                priority = ?::ticket_priority,
                customer_id = ?,
                assignee_id = ?,
                category_id = ?,
                updated_at = CURRENT_TIMESTAMP
            WHERE id = ?
            """;

    @Override
    public Ticket save(Ticket ticket) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, ticket.getTitle());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getStatus().name());
            statement.setString(4, ticket.getPriority().name());
            statement.setLong(5, ticket.getCustomerId());
            setNullableLong(statement, 6, ticket.getAssigneeId());
            setNullableLong(statement, 7, ticket.getCategoryId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    ticket.setId(resultSet.getLong("id"));
                    ticket.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    ticket.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                }
            }

            return ticket;
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось сохранить заявку", exception);
        }
    }

    @Override
    public Optional<Ticket> findById(long id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapTicket(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка поиска заявки", exception);
        }
    }

    @Override
    public List<Ticket> findAll() {
        String sql = "SELECT * FROM tickets ORDER BY id";
        List<Ticket> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(mapTicket(resultSet));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения заявок", exception);
        }
    }

    @Override
    public List<Ticket> findByStatus(TicketStatus status) {
        String sql = """
                SELECT * FROM tickets
                WHERE status = ?::ticket_status
                ORDER BY created_at DESC
                """;
        List<Ticket> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapTicket(resultSet));
                }
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка фильтрации заявок по статусу", exception);
        }
    }

    @Override
    public void update(Ticket ticket) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, ticket.getTitle());
            statement.setString(2, ticket.getDescription());
            statement.setString(3, ticket.getStatus().name());
            statement.setString(4, ticket.getPriority().name());
            statement.setLong(5, ticket.getCustomerId());
            setNullableLong(statement, 6, ticket.getAssigneeId());
            setNullableLong(statement, 7, ticket.getCategoryId());
            statement.setLong(8, ticket.getId());

            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось обновить заявку", exception);
        }
    }

    @Override
    public boolean deleteById(long id) {
        String sql = "DELETE FROM tickets WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось удалить заявку", exception);
        }
    }

    private Ticket mapTicket(ResultSet resultSet) throws SQLException {
        Ticket ticket = new Ticket();
        ticket.setId(resultSet.getLong("id"));
        ticket.setTitle(resultSet.getString("title"));
        ticket.setDescription(resultSet.getString("description"));
        ticket.setStatus(TicketStatus.valueOf(resultSet.getString("status")));
        ticket.setPriority(TicketPriority.valueOf(resultSet.getString("priority")));
        ticket.setCustomerId(resultSet.getLong("customer_id"));

        long assigneeId = resultSet.getLong("assignee_id");
        ticket.setAssigneeId(resultSet.wasNull() ? null : assigneeId);

        long categoryId = resultSet.getLong("category_id");
        ticket.setCategoryId(resultSet.wasNull() ? null : categoryId);

        ticket.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        ticket.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());

        Timestamp closedAt = resultSet.getTimestamp("closed_at");
        ticket.setClosedAt(closedAt == null ? null : closedAt.toLocalDateTime());

        return ticket;
    }

    private static void setNullableLong(PreparedStatement statement, int index, Long value)
            throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }
}
