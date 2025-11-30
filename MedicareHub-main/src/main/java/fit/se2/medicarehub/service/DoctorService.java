package fit.se2.medicarehub.service;

import fit.se2.medicarehub.model.Appointment;
import fit.se2.medicarehub.model.Doctor;
import fit.se2.medicarehub.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class DoctorService {

    @Autowired
    private Dao doctorDao;

    @Autowired
    private DoctorRepository doctorRepository;

    public Doctor getCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return doctorRepository.findByUser_Email(email);
    }

    public Page<Appointment> filterAndPageAppointments(Long doctorID, boolean ongoingExamination, String patientName, Pageable pageable) {
        return doctorDao.filterAndPageAppointments(doctorID, ongoingExamination, patientName, pageable);
    }

}
