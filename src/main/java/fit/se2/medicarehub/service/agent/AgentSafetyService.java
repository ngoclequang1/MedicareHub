package fit.se2.medicarehub.service.agent;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class AgentSafetyService {

    private static final List<String> EMERGENCY_TERMS = List.of(
            "dau nguc", "kho tho", "khong tho duoc", "bat tinh", "dot quy",
            "co giat", "chay mau nhieu", "tu tu", "tu sat", "qua lieu"
    );

    public boolean isEmergency(String message) {
        String normalized = normalize(message);
        return EMERGENCY_TERMS.stream().anyMatch(normalized::contains);
    }

    public String normalize(String text) {
        String decomposed = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D');
        return decomposed.toLowerCase(Locale.ROOT).trim();
    }
}
