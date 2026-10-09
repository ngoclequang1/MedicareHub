package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.KnowledgeArticle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {
    List<KnowledgeArticle> findByApprovedTrue();
}
