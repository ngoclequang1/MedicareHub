package fit.se2.medicarehub.repository;

import fit.se2.medicarehub.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.hibernate.Session;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.query.criteria.JpaCriteriaQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class Dao {

    @Autowired
    private EntityManager em;

    public Page<Doctor> filterAndSortDoctors(String fullName, DoctorDegree filterDegree, Long specialtyId, Boolean enabled, String sortField, String sortDir, Pageable pageable) {
        HibernateCriteriaBuilder cb = em.unwrap(Session.class).getCriteriaBuilder();
        // Truy vấn chính
        JpaCriteriaQuery<Doctor> cq = cb.createQuery(Doctor.class);
        Root<Doctor> root = cq.from(Doctor.class);
        var user = root.get("user");
        var specialty = root.get("specialty");
        var academicDegree = root.get("academicDegree");
        var userEnabled = user.get("enabled");

        List<Predicate> predicates = new ArrayList<>();

        // Filter theo fullName
        String fullNamePattern = null;
        if (fullName != null && !fullName.trim().isEmpty()) {
            fullNamePattern = "%" + fullName.toLowerCase() + "%";
            predicates.add(cb.like(cb.lower(user.get("fullName")), fullNamePattern));
        }
        // Filter theo academicDegree
        if (filterDegree != null) {
            predicates.add(cb.equal(academicDegree, filterDegree));
        }
        if (enabled != null) {
            predicates.add(cb.equal(userEnabled, enabled));
        }
        // Filter theo specialty (theo specialtyId)
        if (specialtyId != null) {
            predicates.add(cb.equal(specialty.get("specialtyID"), specialtyId));
        }
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Xử lý sắp xếp
        List<Order> orderList = new ArrayList<>();
        if (sortField == null || sortField.trim().isEmpty()) {
            orderList.add(cb.asc(user.get("fullName")));
            orderList.add(cb.asc(user.get("enabled")));
        } else {
            switch (sortField.toLowerCase()) {
                case "fullname":
                    orderList.add("desc".equalsIgnoreCase(sortDir)
                            ? cb.desc(user.get("fullName"))
                            : cb.asc(user.get("fullName")));
                    break;
                case "enabled":
                    orderList.add("desc".equalsIgnoreCase(sortDir)
                            ? cb.desc(user.get("enabled"))
                            : cb.asc(user.get("enabled")));
                    break;
                case "specialty":
                    orderList.add("desc".equalsIgnoreCase(sortDir)
                            ? cb.desc(specialty.get("name"))
                            : cb.asc(specialty.get("name")));
                    break;
                case "academicdegree":
                    orderList.add("desc".equalsIgnoreCase(sortDir)
                            ? cb.desc(academicDegree)
                            : cb.asc(academicDegree));
                    break;
                default:
                    orderList.add(cb.asc(user.get("fullName")));
                    orderList.add(cb.asc(user.get("enabled")));
                    break;
            }
        }
        cq.orderBy(orderList);

        TypedQuery<Doctor> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Doctor> result = query.getResultList();

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Doctor> countRoot = countQuery.from(Doctor.class);
        if (fullNamePattern != null) {
            countQuery.where(cb.like(cb.lower(countRoot.get("user").get("fullName")), fullNamePattern));
        }
        if (enabled != null) {
            countQuery.where(cb.equal(countRoot.get("user").get("enabled"), enabled));
        }
        countQuery.select(cb.count(countRoot));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }


    public Page<Patient> filterAndSortPatients(String fullName, String sortField, Boolean enabled, String sortDir, Pageable pageable) {
        HibernateCriteriaBuilder cb = em.unwrap(Session.class).getCriteriaBuilder();

        // Tính toán pattern tìm kiếm
        String fullNamePattern = null;
        if (fullName != null && !fullName.trim().isEmpty()) {
            fullNamePattern = "%" + fullName.toLowerCase() + "%";
        }

        // Truy vấn chính
        JpaCriteriaQuery<Patient> cq = cb.createQuery(Patient.class);
        Root<Patient> root = cq.from(Patient.class);
        var user = root.get("user");

        List<Predicate> predicates = new ArrayList<>();

        // Bộ lọc theo fullName
        if (fullNamePattern != null) {
            predicates.add(cb.like(cb.lower(user.get("fullName")), fullNamePattern));
        }

        // Bộ lọc theo enabled
        if (enabled != null) {
            predicates.add(cb.equal(user.get("enabled"), enabled));
        }

        // Áp dụng các predicate (bộ lọc)
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Xử lý sắp xếp
        if (sortField == null || sortField.trim().isEmpty()) {
            cq.orderBy(
                    cb.asc(user.get("fullName")),
                    cb.asc(user.get("enabled"))
            );
        } else if ("fullName".equalsIgnoreCase(sortField)) {
            cq.orderBy("desc".equalsIgnoreCase(sortDir)
                    ? cb.desc(user.get("fullName"))
                    : cb.asc(user.get("fullName")));
        } else if ("enabled".equalsIgnoreCase(sortField)) {
            cq.orderBy("desc".equalsIgnoreCase(sortDir)
                    ? cb.desc(user.get("enabled"))
                    : cb.asc(user.get("enabled")));
        } else {
            cq.orderBy(
                    cb.asc(user.get("fullName")),
                    cb.asc(user.get("enabled"))
            );
        }

        TypedQuery<Patient> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Patient> result = query.getResultList();

        // Truy vấn đếm
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Patient> countRoot = countQuery.from(Patient.class);
        var countUser = countRoot.get("user");

        // Bộ lọc theo fullName
        if (fullNamePattern != null) {
            countQuery.where(cb.like(cb.lower(countUser.get("fullName")), fullNamePattern));
        }

        // Bộ lọc theo enabled
        if (enabled != null) {
            countQuery.where(cb.equal(countUser.get("enabled"), enabled));
        }

        countQuery.select(cb.count(countRoot));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }


    public Page<Specialty> searchAndPage(String specialtyName, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        String searchPattern = null;
        if (specialtyName != null && !specialtyName.trim().isEmpty()) {
            searchPattern = "%" + specialtyName.toLowerCase() + "%";
        }

        // Truy vấn chính
        CriteriaQuery<Specialty> cq = cb.createQuery(Specialty.class);
        Root<Specialty> root = cq.from(Specialty.class);
        if (searchPattern != null) {
            cq.where(cb.like(cb.lower(root.get("specialtyName")), searchPattern));
        }
        TypedQuery<Specialty> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Truy vấn đếm
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Specialty> countRoot = countQuery.from(Specialty.class);
        if (searchPattern != null) {
            countQuery.select(cb.count(countRoot))
                    .where(cb.like(cb.lower(countRoot.get("specialtyName")), searchPattern));
        } else {
            countQuery.select(cb.count(countRoot));
        }
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    public Page<Appointment> filterAndPageAppointments(Long doctorID, boolean ongoingExamination, String patientName, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // Main query
        CriteriaQuery<Appointment> cq = cb.createQuery(Appointment.class);
        Root<Appointment> root = cq.from(Appointment.class);

        // Joins
        root.fetch("patient", JoinType.LEFT);
        Join<Appointment, Schedule> scheduleJoin = root.join("doctor").join("schedules", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(root.get("doctor").get("doctorID"), doctorID));

        // Status filter
        predicates.add(cb.equal(root.get("status"), ongoingExamination ? "ONGOING" : "CONFIRMED"));

        // Patient name filter
        if (patientName != null && !patientName.trim().isEmpty()) {
            Join<Object, Object> patientJoin = root.join("patient", JoinType.LEFT);
            Join<Object, Object> userJoin = patientJoin.join("user", JoinType.LEFT);
            predicates.add(cb.like(cb.lower(userJoin.get("fullName")), "%" + patientName.toLowerCase() + "%"));
        }

        // Schedule constraint
        Predicate withinSchedule = cb.and(
                cb.lessThanOrEqualTo(scheduleJoin.get("startTime"), root.get("appointmentDate")),
                cb.greaterThanOrEqualTo(scheduleJoin.get("endTime"), root.get("appointmentDate"))
        );
        predicates.add(withinSchedule);

        cq.select(root).where(predicates.toArray(new Predicate[0])).distinct(true);
        cq.orderBy(cb.asc(root.get("appointmentDate")));

        TypedQuery<Appointment> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Appointment> result = query.getResultList();

        // Count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Appointment> countRoot = countQuery.from(Appointment.class);
        Join<Appointment, Schedule> countScheduleJoin = countRoot.join("doctor").join("schedules", JoinType.INNER);

        List<Predicate> countPredicates = new ArrayList<>();
        countPredicates.add(cb.equal(countRoot.get("doctor").get("doctorID"), doctorID));
        countPredicates.add(cb.equal(countRoot.get("status"), ongoingExamination ? "ONGOING" : "CONFIRMED"));

        if (patientName != null && !patientName.trim().isEmpty()) {
            Join<Object, Object> patientJoin = countRoot.join("patient", JoinType.LEFT);
            Join<Object, Object> userJoin = patientJoin.join("user", JoinType.LEFT);
            countPredicates.add(cb.like(cb.lower(userJoin.get("fullName")), "%" + patientName.toLowerCase() + "%"));
        }

        Predicate countWithinSchedule = cb.and(
                cb.lessThanOrEqualTo(countScheduleJoin.get("startTime"), countRoot.get("appointmentDate")),
                cb.greaterThanOrEqualTo(countScheduleJoin.get("endTime"), countRoot.get("appointmentDate"))
        );
        countPredicates.add(countWithinSchedule);

        countQuery.select(cb.countDistinct(countRoot)).where(countPredicates.toArray(new Predicate[0]));

        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }

    public Page<MedicalRecord> findMedicalRecords(String fullName, Pageable pageable) {
        // Lấy Criteria Builder từ EntityManager
        HibernateCriteriaBuilder cb = em.unwrap(Session.class).getCriteriaBuilder();

        // Tạo truy vấn chính cho MedicalRecord
        JpaCriteriaQuery<MedicalRecord> cq = cb.createQuery(MedicalRecord.class);
        Root<MedicalRecord> root = cq.from(MedicalRecord.class);

        // Join từ MedicalRecord -> Doctor -> User
        Join<MedicalRecord, Doctor> doctorJoin = root.join("doctor");
        Join<Doctor, User> doctorUserJoin = doctorJoin.join("user");

        // Join từ MedicalRecord -> Patient -> User
        Join<MedicalRecord, Patient> patientJoin = root.join("patient");
        Join<Patient, User> patientUserJoin = patientJoin.join("user");

        // Danh sách các điều kiện tìm kiếm
        List<Predicate> predicates = new ArrayList<>();

        // Nếu có tìm theo tên (fullName) thì áp dụng bộ lọc cho cả doctor.user.fullName và patient.user.fullName
        if (fullName != null && !fullName.trim().isEmpty()) {
            String fullNamePattern = "%" + fullName.toLowerCase() + "%";
            // So sánh cả 2 trường với pattern (với toán tử OR)
            Predicate doctorPredicate = cb.like(cb.lower(doctorUserJoin.get("fullName")), fullNamePattern);
            Predicate patientPredicate = cb.like(cb.lower(patientUserJoin.get("fullName")), fullNamePattern);
            predicates.add(cb.or(doctorPredicate, patientPredicate));
        }

        // Áp dụng các điều kiện lọc nếu có
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Sắp xếp (ví dụ: sắp xếp theo ngày khám giảm dần)
        cq.orderBy(cb.desc(root.get("examinationDate")));

        // Tạo truy vấn chính và thiết lập phân trang
        TypedQuery<MedicalRecord> query = em.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<MedicalRecord> result = query.getResultList();

        // Truy vấn đếm tổng số bản ghi thỏa mãn các điều kiện tìm kiếm
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<MedicalRecord> countRoot = countQuery.from(MedicalRecord.class);
        // Thực hiện join tương tự cho truy vấn đếm
        Join<MedicalRecord, Doctor> countDoctorJoin = countRoot.join("doctor");
        Join<Doctor, User> countDoctorUserJoin = countDoctorJoin.join("user");
        Join<MedicalRecord, Patient> countPatientJoin = countRoot.join("patient");
        Join<Patient, User> countPatientUserJoin = countPatientJoin.join("user");

        List<Predicate> countPredicates = new ArrayList<>();
        if (fullName != null && !fullName.trim().isEmpty()) {
            String fullNamePattern = "%" + fullName.toLowerCase() + "%";
            Predicate doctorPredicate = cb.like(cb.lower(countDoctorUserJoin.get("fullName")), fullNamePattern);
            Predicate patientPredicate = cb.like(cb.lower(countPatientUserJoin.get("fullName")), fullNamePattern);
            countPredicates.add(cb.or(doctorPredicate, patientPredicate));
        }
        if (!countPredicates.isEmpty()) {
            countQuery.where(countPredicates.toArray(new Predicate[0]));
        }

        countQuery.select(cb.count(countRoot));
        Long total = em.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(result, pageable, total);
    }


}

