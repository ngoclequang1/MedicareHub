package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.Chatbot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotRepository extends JpaRepository<Chatbot, Long> {
}
