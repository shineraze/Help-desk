package ru.example.helpdesk.repository.jdbc;

import ru.example.helpdesk.config.DatabaseConfig;
import ru.example.helpdesk.model.ArticleCategory;
import ru.example.helpdesk.model.KnowledgeArticle;
import ru.example.helpdesk.model.TicketStatus;
import ru.example.helpdesk.repository.KnowledgeBaseRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * База знаний (вариант A, раздел 42): две таблицы, транзакция публикации
 * и три запроса — по категории, поиск по тексту, карточка статьи.
 */
public class JdbcKnowledgeBaseRepository implements KnowledgeBaseRepository {

    private static final String ARTICLE_COLUMNS = """
            SELECT a.id,
                   a.title,
                   a.content,
                   a.category_id,
                   a.author_id,
                   a.source_ticket_id,
                   a.created_at,
                   a.updated_at,
                   c.name AS category_name,
                   u.name AS author_name,
                   t.title AS ticket_title
            FROM knowledge_articles a
            JOIN article_categories c ON c.id = a.category_id
            JOIN users u ON u.id = a.author_id
            LEFT JOIN tickets t ON t.id = a.source_ticket_id
            """;

    @Override
    public List<ArticleCategory> findAllCategories() {
        String sql = "SELECT * FROM article_categories ORDER BY id";
        List<ArticleCategory> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                ArticleCategory category = new ArticleCategory();
                category.setId(resultSet.getLong("id"));
                category.setName(resultSet.getString("name"));
                category.setDescription(resultSet.getString("description"));
                result.add(category);
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения категорий статей", exception);
        }
    }

    @Override
    public ArticleCategory saveCategory(ArticleCategory category) {
        String sql = """
                INSERT INTO article_categories(name, description)
                VALUES (?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    category.setId(resultSet.getLong("id"));
                }
            }

            return category;
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось сохранить категорию статей", exception);
        }
    }

    @Override
    public KnowledgeArticle publishArticle(KnowledgeArticle article) {
        try (Connection connection = DatabaseConfig.getConnection()) {
            connection.setAutoCommit(false);

            try {
                validateSourceTicket(connection, article);
                insertArticle(connection, article);
                connection.commit();
                return article;
            } catch (Exception exception) {
                connection.rollback();
                throw exception;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Не удалось опубликовать статью", exception);
        }
    }

    @Override
    public List<KnowledgeArticle> findByCategory(long categoryId) {
        String sql = ARTICLE_COLUMNS + """
                WHERE a.category_id = ?
                ORDER BY a.created_at DESC
                """;
        List<KnowledgeArticle> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, categoryId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapArticle(resultSet));
                }
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения статей категории", exception);
        }
    }

    @Override
    public List<KnowledgeArticle> search(String query) {
        String sql = ARTICLE_COLUMNS + """
                WHERE a.title ILIKE ? OR a.content ILIKE ?
                ORDER BY a.created_at DESC
                """;
        String pattern = "%" + query + "%";
        List<KnowledgeArticle> result = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, pattern);
            statement.setString(2, pattern);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    result.add(mapArticle(resultSet));
                }
            }

            return result;
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка поиска статей", exception);
        }
    }

    @Override
    public Optional<KnowledgeArticle> findById(long id) {
        String sql = ARTICLE_COLUMNS + """
                WHERE a.id = ?
                """;

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapArticle(resultSet));
                }
                return Optional.empty();
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Ошибка чтения статьи", exception);
        }
    }

    private void validateSourceTicket(Connection connection, KnowledgeArticle article)
            throws SQLException {
        if (article.getSourceTicketId() == null) {
            return;
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT status FROM tickets WHERE id = ? FOR UPDATE")) {

            statement.setLong(1, article.getSourceTicketId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Заявка для статьи не найдена");
                }

                String status = resultSet.getString("status");
                if (!TicketStatus.RESOLVED.name().equals(status)
                        && !TicketStatus.CLOSED.name().equals(status)) {
                    throw new IllegalArgumentException(
                            "Статью можно связать только с решённой или закрытой заявкой");
                }
            }
        }
    }

    private void insertArticle(Connection connection, KnowledgeArticle article) throws SQLException {
        String sql = """
                INSERT INTO knowledge_articles(
                    title, content, category_id, author_id, source_ticket_id
                )
                VALUES (?, ?, ?, ?, ?)
                RETURNING id, created_at, updated_at
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, article.getTitle());
            statement.setString(2, article.getContent());
            statement.setLong(3, article.getCategoryId());
            statement.setLong(4, article.getAuthorId());

            if (article.getSourceTicketId() == null) {
                statement.setNull(5, java.sql.Types.BIGINT);
            } else {
                statement.setLong(5, article.getSourceTicketId());
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    article.setId(resultSet.getLong("id"));
                    article.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
                    article.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
                }
            }
        }
    }

    private KnowledgeArticle mapArticle(ResultSet resultSet) throws SQLException {
        KnowledgeArticle article = new KnowledgeArticle();
        article.setId(resultSet.getLong("id"));
        article.setTitle(resultSet.getString("title"));
        article.setContent(resultSet.getString("content"));
        article.setCategoryId(resultSet.getLong("category_id"));
        article.setAuthorId(resultSet.getLong("author_id"));

        long sourceTicketId = resultSet.getLong("source_ticket_id");
        article.setSourceTicketId(resultSet.wasNull() ? null : sourceTicketId);

        article.setCreatedAt(resultSet.getTimestamp("created_at").toLocalDateTime());
        article.setUpdatedAt(resultSet.getTimestamp("updated_at").toLocalDateTime());
        article.setCategoryName(resultSet.getString("category_name"));
        article.setAuthorName(resultSet.getString("author_name"));
        article.setSourceTicketTitle(resultSet.getString("ticket_title"));
        return article;
    }
}
