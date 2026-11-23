package ru.example.helpdesk.repository;

import ru.example.helpdesk.model.ArticleCategory;
import ru.example.helpdesk.model.KnowledgeArticle;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий базы знаний (вариант A, раздел 42).
 */
public interface KnowledgeBaseRepository {

    List<ArticleCategory> findAllCategories();

    ArticleCategory saveCategory(ArticleCategory category);

    KnowledgeArticle publishArticle(KnowledgeArticle article);

    List<KnowledgeArticle> findByCategory(long categoryId);

    List<KnowledgeArticle> search(String query);

    Optional<KnowledgeArticle> findById(long id);
}
