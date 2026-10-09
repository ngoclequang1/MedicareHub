package fit.se2.medicarehub.service.agent;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class OpenAiCompatibleLanguageModel implements AgentLanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenAiCompatibleLanguageModel.class);
    private static final String SYSTEM_PROMPT = """
            Bạn là trợ lý sức khỏe của Medicare Hub. Trả lời ngắn gọn bằng tiếng Việt.
            Không chẩn đoán chắc chắn, không kê đơn, không thay đổi liều thuốc.
            Khuyến nghị gặp bác sĩ khi thông tin không đủ và luôn nhắc rằng câu trả lời không thay thế bác sĩ.
            Không yêu cầu người dùng cung cấp căn cước, mật khẩu hoặc dữ liệu định danh không cần thiết.
            """;

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final boolean enabled;

    public OpenAiCompatibleLanguageModel(RestClient.Builder builder,
                                         @Value("${app.ai.base-url:https://api.openai.com/v1}") String baseUrl,
                                         @Value("${app.ai.api-key:}") String apiKey,
                                         @Value("${app.ai.model:gpt-4.1-mini}") String model,
                                         @Value("${app.ai.enabled:false}") boolean enabled) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
    }

    @Override
    public Optional<String> answerGeneralQuestion(String message, String approvedContext) {
        if (!enabled || apiKey.isBlank()) {
            return Optional.empty();
        }
        try {
            Map<String, Object> request = Map.of(
                    "model", model,
                    "temperature", 0.2,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT
                                    + (approvedContext.isBlank() ? "" : "\nChỉ dùng ngữ cảnh đã kiểm duyệt sau nếu phù hợp:\n" + approvedContext)),
                            Map.of("role", "user", "content", message)
                    )
            );
            JsonNode response = restClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(JsonNode.class);
            if (response == null) {
                return Optional.empty();
            }
            String content = response.path("choices").path(0).path("message").path("content").asText("").trim();
            return content.isEmpty() ? Optional.empty() : Optional.of(content);
        } catch (RuntimeException exception) {
            LOGGER.warn("AI provider không khả dụng; sử dụng phản hồi an toàn mặc định", exception);
            return Optional.empty();
        }
    }
}
