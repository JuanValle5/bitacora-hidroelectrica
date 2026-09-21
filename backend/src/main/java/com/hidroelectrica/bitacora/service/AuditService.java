package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.response.AuditLogResponseDTO;
import com.hidroelectrica.bitacora.model.AuditLog;
import com.hidroelectrica.bitacora.model.HourlyReading;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void recordAudit(HourlyReading reading, User user, LocalDate date, int hour,
                            String fieldKey, String fieldLabel, String oldValue, String newValue, String justification) {
        AuditLog log = new AuditLog(
            reading,
            user,
            date,
            hour,
            fieldKey,
            fieldLabel,
            oldValue,
            newValue,
            justification
        );
        auditLogRepository.save(log);
    }

    public List<AuditLogResponseDTO> getAuditLogs(LocalDate date, String search) {
        List<AuditLog> logs = (date != null)
            ? auditLogRepository.findByReportDateOrderByCreatedAtDesc(date)
            : auditLogRepository.findAllByOrderByCreatedAtDesc();

        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase();
            logs = logs.stream()
                .filter(l -> (l.getUser() != null && l.getUser().getFullName().toLowerCase().contains(q))
                    || l.getFieldLabel().toLowerCase().contains(q)
                    || l.getFieldKey().toLowerCase().contains(q)
                    || (l.getJustification() != null && l.getJustification().toLowerCase().contains(q)))
                .toList();
        }

        return logs.stream().map(this::toDTO).toList();
    }

    private AuditLogResponseDTO toDTO(AuditLog log) {
        String horaFmt = String.format("%02d:00", log.getHour());
        String timestampStr = log.getCreatedAt() != null ? log.getCreatedAt().format(TIMESTAMP_FMT) : "";
        String operador = log.getUser() != null ? log.getUser().getFullName() : "Sistema";

        return new AuditLogResponseDTO(
            log.getId(),
            timestampStr,
            operador,
            horaFmt,
            log.getFieldLabel(),
            log.getOldValue() != null ? log.getOldValue() : "—",
            log.getNewValue(),
            log.getJustification()
        );
    }
}
