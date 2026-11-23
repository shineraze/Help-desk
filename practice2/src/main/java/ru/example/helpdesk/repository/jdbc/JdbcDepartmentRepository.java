package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.Department;
import ru.example.helpdesk.repository.DepartmentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcDepartmentRepository implements DepartmentRepository {

    @Override
    public List<Department> findAllDepartments() {
        String sql = "SELECT * FROM departments ORDER BY id";
        List<Department> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                result.add(mapDepartment(resultSet));
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения подразделений", exception);
        }
    }

    @Override
    public Optional<Department> findDepartmentById(long id) {
        String sql = "SELECT * FROM departments WHERE id = ?";

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapDepartment(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка поиска подразделения", exception);
        }
    }

    private Department mapDepartment(ResultSet resultSet) throws SQLException {
        Department department = new Department();
        department.setId(resultSet.getLong("id"));
        department.setName(resultSet.getString("name"));
        department.setDescription(resultSet.getString("description"));
        department.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        return department;
    }
}
