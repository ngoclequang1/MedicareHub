package fit.se2.medicarehub.service.agent;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentSafetyServiceTest {

    private final AgentSafetyService service = new AgentSafetyService();

    @Test
    void detectsVietnameseEmergencyTextWithoutDependingOnDiacritics() {
        assertTrue(service.isEmergency("Tôi đang đau ngực và rất khó thở"));
        assertTrue(service.isEmergency("Nguoi benh bat tinh"));
    }

    @Test
    void doesNotFlagRoutineBookingRequest() {
        assertFalse(service.isEmergency("Tôi muốn đặt lịch bác sĩ da liễu"));
    }
}
