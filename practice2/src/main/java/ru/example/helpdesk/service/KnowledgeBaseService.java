package ru.example.helpdesk.service;

import ru.example.helpdesk.model.ArticleCategory;
import ru.example.helpdesk.model.KnowledgeArticle;
import ru.example.helpdesk.repository.KnowledgeBaseRepository;

import java.util.List;
import java.util.Optional;

/**
 * Логика базы знаний (вариант A, раздел 42).
 */
public class KnowledgeBaseService {

    private final KnowledgeBaseRepository repository;

    public KnowledgeBaseService(KnowledgeBaseRepository repository) {
        this.repository = repository;
    }

    public List<ArticleCategory> getCategories() {
        return repository.findAllCategories();
    }

    public ArticleCategory createCategory(String name, String description) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Пустое название категории статей");
        }

        ArticleCategory category = new ArticleCategory();
        category.setName(name.trim());
        category.setDescription(description);

        return repository.saveCategory(category);
    }

    public KnowledgeArticle publishArticle(String title,
                                           String content,
                                           long categoryId,
                                           long authorId,
                                           Long sourceTicketId) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Пустой заголовок статьи");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Пустой текст статьи");
        }

        KnowledgeArticle article = new KnowledgeArticle();
        article.setTitle(title.trim());
        article.setContent(content.trim());
        article.setCategoryId(categoryId);
        article.setAuthorId(authorId);
        article.setSourceTicketId(sourceTicketId);

        return repository.publishArticle(article);
    }

    public List<KnowledgeArticle> findByCategory(long categoryId) {
        return repository.findByCategory(categoryId);
    }

    public List<KnowledgeArticle> search(String query) {
        return repository.search(query);
    }

    public Optional<KnowledgeArticle> findById(long id) {
        return repository.findById(id);
    }
}
