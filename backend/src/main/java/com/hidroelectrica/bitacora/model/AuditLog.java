package com.hidroelectrica.bitacora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hourly_reading_id")
    private HourlyReading hourlyReading;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private Integer hour;

    @Column(name = "field_key", nullable = false, length = 50)
    private String fieldKey;

    @Column(name = "field_label", nullable = false, length = 100)
    private String fieldLabel;

    @Column(name = "old_value", length = 100)
    private String oldValue;

    @Column(name = "new_value", nullable = false, length = 100)
    private String newValue;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String justification;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public AuditLog() {}

    public AuditLog(HourlyReading hourlyReading, User user, LocalDate reportDate, Integer hour,
                    String fieldKey, String fieldLabel, String oldValue, String newValue, String justification) {
        this.hourlyReading = hourlyReading;
        this.user = user;
        this.reportDate = reportDate;
        this.hour = hour;
        this.fieldKey = fieldKey;
        this.fieldLabel = fieldLabel;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.justification = justification;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public HourlyReading getHourlyReading() { return hourlyReading; }
    public void setHourlyReading(HourlyReading hourlyReading) { this.hourlyReading = hourlyReading; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }

    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
