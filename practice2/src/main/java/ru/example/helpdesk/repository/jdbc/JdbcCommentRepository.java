package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.TicketComment;
import ru.example.helpdesk.model.UserRole;
import ru.example.helpdesk.repository.CommentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class JdbcCommentRepository implements CommentRepository {

    private static final String INSERT_SQL = """
            INSERT INTO ticket_comments(ticket_id, author_id, text, internal)
            VALUES (?, ?, ?, ?)
            RETURNING id, created_at
            """;

    @Override
    public TicketComment add(TicketComment comment) {
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setLong(1, comment.getTicketId());
            statement.setLong(2, comment.getAuthorId());
            statement.setString(3, comment.getText());
            statement.setBoolean(4, comment.isInternal());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    comment.setId(resultSet.getLong("id"));
                    comment.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                }
            }

            return comment;
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось добавить комментарий", exception);
        }
    }

    @Override
    public List<TicketComment> findByTicketId(long ticketId) {
        String sql = """
                SELECT c.id,
                       c.ticket_id,
                       c.author_id,
                       c.text,
                       c.internal,
                       c.created_at,
                       u.name AS author_name,
                       u.role AS author_role
                FROM ticket_comments c
                JOIN users u ON u.id = c.author_id
                WHERE c.ticket_id = ?
                ORDER BY c.created_at
                """;
        List<TicketComment> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapComment(resultSet));
                }
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения комментариев", exception);
        }
    }

    private TicketComment mapComment(ResultSet resultSet) throws SQLException {
        TicketComment comment = new TicketComment();
        comment.setId(resultSet.getLong("id"));
        comment.setTicketId(resultSet.getLong("ticket_id"));
        comment.setAuthorId(resultSet.getLong("author_id"));
        comment.setText(resultSet.getString("text"));
        comment.setInternal(resultSet.getBoolean("internal"));
        comment.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        comment.setAuthorName(resultSet.getString("author_name"));
        comment.setAuthorRole(UserRole.valueOf(resultSet.getString("author_role")));
        return comment;
    }
}
