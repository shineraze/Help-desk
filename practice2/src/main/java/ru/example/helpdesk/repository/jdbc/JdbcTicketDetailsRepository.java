package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.TicketDetails;
import ru.example.helpdesk.model.TicketPriority;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.repository.TicketDetailsRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcTicketDetailsRepository implements TicketDetailsRepository {

    private static final String SQL = """
            SELECT t.id,
                   t.title,
                   t.status,
                   t.priority,
                   c.name AS category_name,
                   customer.name AS customer_name,
                   assignee.name AS assignee_name,
                   t.created_at
            FROM tickets t
            JOIN users customer ON customer.id = t.customer_id
            LEFT JOIN users assignee ON assignee.id = t.assignee_id
            LEFT JOIN categories c ON c.id = t.category_id
            WHERE t.id = ?
            """;

    @Override
    public Optional<TicketDetails> findDetails(long ticketId) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(new TicketDetails(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        TicketStatus.valueOf(resultSet.getString("status")),
                        TicketPriority.valueOf(resultSet.getString("priority")),
                        resultSet.getString("category_name"),
                        resultSet.getString("customer_name"),
                        resultSet.getString("assignee_name"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения карточки заявки", exception);
        }
    }
}
