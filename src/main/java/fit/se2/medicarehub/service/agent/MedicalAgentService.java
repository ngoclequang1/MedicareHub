package fit.se2.medicarehub.service.agent;

import fit.se2.medicarehub.model.Chatbot;
import fit.se2.medicarehub.model.User;
import fit.se2.medicarehub.model.agent.AgentChatResponse;
import fit.se2.medicarehub.repository.ChatbotRepository;
import fit.se2.medicarehub.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class MedicalAgentService {

    private static final String DISCLAIMER = " Thông tin này chỉ mang tính hỗ trợ, không thay thế chẩn đoán của bác sĩ.";
    private static final Pattern BOOKING_PATTERN = Pattern.compile("dat lich.*bac si\\s+(\\d+).*?(\\d{4}-\\d{2}-\\d{2})");
    private static final Pattern CONFIRM_PATTERN = Pattern.compile("xac nhan\\s+([0-9a-f-]{36})");

    private final AgentSafetyService safetyService;
    private final AgentToolService toolService;
    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository;
    private final AgentLanguageModel languageModel;
    private final AgentActionService actionService;
    private final AgentKnowledgeService knowledgeService;

    public MedicalAgentService(AgentSafetyService safetyService,
                               AgentToolService toolService,
                               ChatbotRepository chatbotRepository,
                               UserRepository userRepository,
                               AgentLanguageModel languageModel,
                               AgentActionService actionService,
                               AgentKnowledgeService knowledgeService) {
        this.safetyService = safetyService;
        this.toolService = toolService;
        this.chatbotRepository = chatbotRepository;
        this.userRepository = userRepository;
        this.languageModel = languageModel;
        this.actionService = actionService;
        this.knowledgeService = knowledgeService;
    }

    public AgentChatResponse chat(String message, Authentication authentication) {
        AgentChatResponse response;
        String normalized = safetyService.normalize(message);

        if (safetyService.isEmergency(message)) {
            response = new AgentChatResponse(
                    "Các triệu chứng bạn mô tả có thể là tình trạng khẩn cấp. Hãy gọi 115 hoặc đến cơ sở cấp cứu gần nhất ngay. Không tự lái xe nếu đang khó thở, đau ngực, mất ý thức hoặc chảy máu nhiều.",
                    "EMERGENCY_TRIAGE", "CRITICAL", null, true, List.of());
        } else if (isDoctor(authentication)) {
            response = routeDoctorRequest(normalized);
        } else if (!isPatient(authentication)) {
            response = AgentChatResponse.text(
                    "Tôi có thể cung cấp thông tin sức khỏe tổng quát và hướng dẫn sử dụng Medicare Hub. Hãy đăng nhập bằng tài khoản bệnh nhân để tra cứu bác sĩ, lịch hẹn hoặc hồ sơ của bạn." + DISCLAIMER,
                    "PUBLIC_GUIDANCE", "LOW");
        } else {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            response = routePatientRequest(user, message, normalized);
        }

        audit(message, response, authentication);
        return response;
    }

    private AgentChatResponse routeDoctorRequest(String message) {
        if (containsAny(message, "lich", "benh nhan", "cuoc hen")) {
            List<Map<String, Object>> data = toolService.listDoctorAppointments();
            return toolResponse(data.isEmpty() ? "Bác sĩ chưa có lịch khám." :
                            "Đây là danh sách lịch khám của bác sĩ. Nội dung chỉ bao gồm dữ liệu cần thiết cho công việc.",
                    "DOCTOR_APPOINTMENTS", "listDoctorAppointments", data);
        }
        return AgentChatResponse.text(
                "Tôi có thể hỗ trợ xem lịch và danh sách bệnh nhân. Mọi nội dung hồ sơ do AI tạo phải được bác sĩ kiểm tra trước khi lưu.",
                "DOCTOR_HELP", "LOW");
    }

    private AgentChatResponse routePatientRequest(User user, String originalMessage, String message) {
        try {
            Matcher confirmation = CONFIRM_PATTERN.matcher(message);
            if (confirmation.find()) {
                var appointment = actionService.confirmAppointment(user, confirmation.group(1));
                return toolResponse("Đã tạo lịch chờ xác nhận với mã " + appointment.getAppointmentID()
                                + ". Hãy kiểm tra lại trong Phiếu khám bệnh.",
                        "CONFIRM_APPOINTMENT", "confirmAppointment", List.of(Map.of(
                                "appointmentId", appointment.getAppointmentID(),
                                "status", appointment.getStatus().name())));
            }

            Matcher booking = BOOKING_PATTERN.matcher(message);
            if (booking.find()) {
                Long doctorId = Long.valueOf(booking.group(1));
                Date appointmentDate = parseDate(booking.group(2));
                var action = actionService.prepareAppointment(user, doctorId, appointmentDate);
                return toolResponse("Tôi chưa thay đổi dữ liệu. Để tạo lịch chờ xác nhận, hãy gửi: xac nhan "
                                + action.getToken(),
                        "PREPARE_APPOINTMENT", "prepareAppointment", List.of(Map.of(
                                "doctorId", doctorId,
                                "date", booking.group(2),
                                "confirmationToken", action.getToken(),
                                "expiresInMinutes", 10)));
            }

            if (containsAny(message, "lich hen", "cuoc hen", "phieu kham")) {
                List<Map<String, Object>> data = toolService.listMyAppointments();
                return toolResponse(data.isEmpty() ? "Bạn chưa có lịch hẹn nào." :
                                "Đây là các lịch hẹn của bạn. Bạn có thể mở trang Phiếu khám bệnh để xem chi tiết.",
                        "LIST_APPOINTMENTS", "listMyAppointments", data);
            }
            if (containsAny(message, "ho so", "benh an", "lich su kham", "don thuoc", "thuoc da dung")) {
                List<Map<String, Object>> data = toolService.summarizeMyMedicalHistory();
                return toolResponse(data.isEmpty() ? "Chưa có hồ sơ khám để tóm tắt." :
                                "Tôi đã lấy tối đa 5 lần khám gần nhất. Vui lòng đối chiếu hướng dẫn thuốc với đơn gốc." + DISCLAIMER,
                        "MEDICAL_HISTORY", "summarizeMyMedicalHistory", data);
            }
            if (containsAny(message, "bac si", "chuyen khoa", "da lieu", "tim mach", "nhi khoa", "rang")) {
                List<Map<String, Object>> data = toolService.searchDoctors(message);
                return toolResponse(data.isEmpty() ? "Tôi chưa tìm thấy bác sĩ phù hợp với yêu cầu này." :
                                "Tôi tìm thấy các bác sĩ phù hợp. Hãy kiểm tra lịch trống trước khi đặt khám.",
                        "SEARCH_DOCTORS", "searchDoctors", data);
            }
            if (containsAny(message, "dau", "sot", "ho", "ngua", "met", "chong mat")) {
                return AgentChatResponse.text(
                        "Bạn nên mô tả vị trí triệu chứng, thời điểm bắt đầu, mức độ, bệnh nền và thuốc đang dùng. Tôi có thể giúp định hướng chuyên khoa nhưng không thể kết luận chẩn đoán." + DISCLAIMER,
                        "SYMPTOM_INTAKE", "MEDIUM");
            }
            String approvedContext = knowledgeService.retrieveApprovedContext(originalMessage);
            String answer = languageModel.answerGeneralQuestion(originalMessage, approvedContext).orElse(
                    "Tôi có thể giúp bạn tìm bác sĩ, xem lịch hẹn, tóm tắt lịch sử khám và hướng dẫn chuẩn bị thông tin triệu chứng." + DISCLAIMER);
            return AgentChatResponse.text(answer,
                    "GENERAL_HELP", "LOW");
        } catch (IllegalStateException exception) {
            return AgentChatResponse.text(exception.getMessage(), "PROFILE_REQUIRED", "LOW");
        } catch (IllegalArgumentException exception) {
            return AgentChatResponse.text(exception.getMessage(), "INVALID_ACTION", "LOW");
        }
    }

    private Date parseDate(String value) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setLenient(false);
        try {
            return format.parse(value);
        } catch (ParseException exception) {
            throw new IllegalArgumentException("Ngày khám phải có định dạng yyyy-MM-dd.");
        }
    }

    private AgentChatResponse toolResponse(String reply, String intent, String toolName,
                                           List<Map<String, Object>> data) {
        return new AgentChatResponse(reply, intent, "LOW", toolName, false, data);
    }

    private boolean isPatient(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_PATIENT"));
    }

    private boolean isDoctor(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DOCTOR"));
    }

    private boolean containsAny(String message, String... terms) {
        for (String term : terms) {
            if (message.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private void audit(String message, AgentChatResponse response, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return;
        }
        Chatbot interaction = new Chatbot();
        interaction.setMessage(message);
        interaction.setResponse(response.reply());
        interaction.setIntent(response.intent());
        interaction.setToolName(response.toolName());
        interaction.setSafetyLevel(response.safetyLevel());
        interaction.setCreatedAt(new Date());
        User user = userRepository.findByUsername(authentication.getName()).orElse(null);
        interaction.setUser(user);
        chatbotRepository.save(interaction);
    }
}
