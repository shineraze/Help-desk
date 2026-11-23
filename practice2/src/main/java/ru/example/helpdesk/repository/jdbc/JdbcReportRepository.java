package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.AgentLoad;
import ru.example.helpdesk.model.CategoryTicketCount;
import ru.example.helpdesk.repository.ReportRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Базовые отчёты SQL (раздел 35).
 */
public class JdbcReportRepository implements ReportRepository {

    @Override
    public Map<String, Long> countByStatus() {
        String sql = """
                SELECT status, COUNT(*) AS count
                FROM tickets
                GROUP BY status
                ORDER BY status
                """;
        Map<String, Long> result = new LinkedHashMap<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.put(resultSet.getString("status"), resultSet.getLong("count"));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка отчёта по статусам", exception);
        }
    }

    @Override
    public List<CategoryTicketCount> countByCategory() {
        String sql = """
                SELECT c.name, COUNT(t.id) AS ticket_count
                FROM categories c
                LEFT JOIN tickets t ON t.category_id = c.id
                GROUP BY c.id, c.name
                ORDER BY ticket_count DESC
                """;
        List<CategoryTicketCount> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(new CategoryTicketCount(
                        resultSet.getString("name"),
                        resultSet.getLong("ticket_count")
                ));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка отчёта по категориям", exception);
        }
    }

    @Override
    public List<AgentLoad> agentLoad() {
        String sql = """
                SELECT u.name,
                       COUNT(t.id) FILTER (
                           WHERE t.status IN ('NEW', 'IN_PROGRESS')
                       ) AS active_tickets
                FROM users u
                LEFT JOIN tickets t ON t.assignee_id = u.id
                WHERE u.role = 'SUPPORT_AGENT'
                GROUP BY u.id, u.name
                ORDER BY active_tickets DESC
                """;
        List<AgentLoad> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(new AgentLoad(
                        resultSet.getString("name"),
                        resultSet.getLong("active_tickets")
                ));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка отчёта по нагрузке сотрудников", exception);
        }
    }
}
