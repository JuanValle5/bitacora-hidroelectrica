package com.hidroelectrica.bitacora.repository;

import com.hidroelectrica.bitacora.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);
    List<AuditLog> findAllByOrderByCreatedAtDesc();
}
