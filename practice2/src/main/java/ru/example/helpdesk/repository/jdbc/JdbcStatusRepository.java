package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.model.TicketStatusHistory;
import ru.example.helpdesk.repository.StatusRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Смена статуса и запись истории одной транзакцией (раздел 31).
 */
public class JdbcStatusRepository implements StatusRepository {

    private static final String SELECT_FOR_UPDATE =
            "SELECT status FROM tickets WHERE id = ? FOR UPDATE";

    private static final String UPDATE_SQL = """
            UPDATE tickets
            SET status = ?::ticket_status,
                updated_at = CURRENT_TIMESTAMP,
                closed_at = CASE
                    WHEN ?::ticket_status = 'CLOSED' THEN CURRENT_TIMESTAMP
                    ELSE closed_at
                END
            WHERE id = ?
            """;

    private static final String INSERT_HISTORY_SQL = """
            INSERT INTO ticket_status_history(
                ticket_id, old_status, new_status, changed_by_id
            )
            VALUES (?, ?::ticket_status, ?::ticket_status, ?)
            """;

    @Override
    public void changeStatus(long ticketId, TicketStatus newStatus, long changedByUserId) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                TicketStatus oldStatus = readStatusForUpdate(connection, ticketId);
                validateTransition(oldStatus, newStatus);
                updateStatus(connection, ticketId, newStatus);
                insertHistory(connection, ticketId, oldStatus, newStatus, changedByUserId);

                connection.commit();
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка изменения статуса", exception);
        }
    }

    @Override
    public List<TicketStatusHistory> findHistory(long ticketId) {
        String sql = """
                SELECT h.id,
                       h.ticket_id,
                       h.old_status,
                       h.new_status,
                       h.changed_by_id,
                       h.changed_at,
                       u.name AS changed_by_name
                FROM ticket_status_history h
                LEFT JOIN users u ON u.id = h.changed_by_id
                WHERE h.ticket_id = ?
                ORDER BY h.changed_at
                """;
        List<TicketStatusHistory> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapHistory(resultSet));
                }
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения истории статусов", exception);
        }
    }

    private TicketStatus readStatusForUpdate(Connection connection, long ticketId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SELECT_FOR_UPDATE)) {
            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Заявка не найдена");
                }
                return TicketStatus.valueOf(resultSet.getString("status"));
            }
        }
    }

    private void updateStatus(Connection connection, long ticketId, TicketStatus newStatus)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {
            statement.setString(1, newStatus.name());
            statement.setString(2, newStatus.name());
            statement.setLong(3, ticketId);
            statement.executeUpdate();
        }
    }

    private void insertHistory(Connection connection, long ticketId, TicketStatus oldStatus,
                               TicketStatus newStatus, long changedByUserId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_HISTORY_SQL)) {
            statement.setLong(1, ticketId);
            statement.setString(2, oldStatus.name());
            statement.setString(3, newStatus.name());
            statement.setLong(4, changedByUserId);
            statement.executeUpdate();
        }
    }

    private void validateTransition(TicketStatus oldStatus, TicketStatus newStatus) {
        boolean allowed = switch (oldStatus) {
            case NEW -> newStatus == TicketStatus.IN_PROGRESS
                    || newStatus == TicketStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == TicketStatus.RESOLVED
                    || newStatus == TicketStatus.CANCELLED;
            case RESOLVED -> newStatus == TicketStatus.CLOSED
                    || newStatus == TicketStatus.IN_PROGRESS;
            case CLOSED, CANCELLED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    "Недопустимый переход: " + oldStatus + " -> " + newStatus
            );
        }
    }

    private TicketStatusHistory mapHistory(ResultSet resultSet) throws SQLException {
        TicketStatusHistory history = new TicketStatusHistory();
        history.setId(resultSet.getLong("id"));
        history.setTicketId(resultSet.getLong("ticket_id"));

        String oldStatus = resultSet.getString("old_status");
        history.setOldStatus(oldStatus == null ? null : TicketStatus.valueOf(oldStatus));
        history.setNewStatus(TicketStatus.valueOf(resultSet.getString("new_status")));

        long changedById = resultSet.getLong("changed_by_id");
        history.setChangedById(resultSet.wasNull() ? null : changedById);
        history.setChangedAt(resultSet.getTimestamp("changed_at").toLocalDateTime());
        history.setChangedByName(resultSet.getString("changed_by_name"));
        return history;
    }
}
