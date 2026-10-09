package fit.se2.medicarehub.service.agent;

import fit.se2.medicarehub.model.KnowledgeArticle;
import fit.se2.medicarehub.repository.KnowledgeArticleRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AgentKnowledgeService {

    private final KnowledgeArticleRepository repository;
    private final AgentSafetyService safetyService;

    public AgentKnowledgeService(KnowledgeArticleRepository repository, AgentSafetyService safetyService) {
        this.repository = repository;
        this.safetyService = safetyService;
    }

    public String retrieveApprovedContext(String question) {
        Set<String> terms = tokens(question);
        return repository.findByApprovedTrue().stream()
                .map(article -> new ScoredArticle(article, score(article, terms)))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingInt(ScoredArticle::score).reversed())
                .limit(3)
                .map(item -> "Nguồn: " + item.article().getTitle() + "\n"
                        + item.article().getContent() + "\nURL: " + item.article().getSourceUrl())
                .collect(Collectors.joining("\n\n"));
    }

    private int score(KnowledgeArticle article, Set<String> terms) {
        Set<String> articleTerms = tokens(article.getTitle() + " " + article.getTags() + " " + article.getContent());
        return (int) terms.stream().filter(articleTerms::contains).count();
    }

    private Set<String> tokens(String value) {
        if (value == null) return Set.of();
        return Arrays.stream(safetyService.normalize(value).split("[^a-z0-9]+"))
                .filter(token -> token.length() > 2)
                .collect(Collectors.toSet());
    }

    private record ScoredArticle(KnowledgeArticle article, int score) {
    }
}
