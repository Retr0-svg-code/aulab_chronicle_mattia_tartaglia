package it.aulab.aulab_chronicle.repositories;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import it.aulab.aulab_chronicle.models.Article;
import it.aulab.aulab_chronicle.models.Category;
import it.aulab.aulab_chronicle.models.User;

public interface ArticleRepository extends ListCrudRepository<Article, Integer> {
    List<Article> findByCategory(Category category);
    List<Article> findByUser(User user);
}
