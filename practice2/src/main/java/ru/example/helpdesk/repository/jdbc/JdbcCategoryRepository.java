package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.Category;
import ru.example.helpdesk.model.TicketPriority;
import ru.example.helpdesk.repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    @Override
    public List<Category> findAllCategories() {
        String sql = "SELECT * FROM categories ORDER BY id";
        List<Category> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(mapCategory(resultSet));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения категорий", exception);
        }
    }

    @Override
    public Optional<Category> findCategoryById(long id) {
        String sql = "SELECT * FROM categories WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapCategory(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка поиска категории", exception);
        }
    }

    private Category mapCategory(ResultSet resultSet) throws SQLException {
        Category category = new Category();
        category.setId(resultSet.getLong("id"));
        category.setName(resultSet.getString("name"));
        category.setDescription(resultSet.getString("description"));
        category.setDefaultPriority(TicketPriority.valueOf(resultSet.getString("default_priority")));
        category.setActive(resultSet.getBoolean("active"));
        return category;
    }
}
