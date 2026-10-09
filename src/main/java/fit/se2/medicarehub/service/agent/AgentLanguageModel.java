package fit.se2.medicarehub.service.agent;

import java.util.Optional;

public interface AgentLanguageModel {
    Optional<String> answerGeneralQuestion(String message, String approvedContext);
}
