package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.response.AuditLogResponseDTO;
import com.hidroelectrica.bitacora.service.AuditService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<List<AuditLogResponseDTO>> getAuditLogs(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(auditService.getAuditLogs(date, search));
    }
}
