package com.hidroelectrica.bitacora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_reports")
public class DailyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false, unique = true)
    private LocalDate reportDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(nullable = false, length = 20)
    private String status = "OPEN"; // "OPEN", "CLOSED"

    @OneToMany(mappedBy = "dailyReport", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("hour ASC")
    private List<HourlyReading> readings = new ArrayList<>();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public DailyReport() {}

    public DailyReport(LocalDate reportDate, User createdBy) {
        this.reportDate = reportDate;
        this.createdBy = createdBy;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addReading(HourlyReading reading) {
        readings.add(reading);
        reading.setDailyReport(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<HourlyReading> getReadings() { return readings; }
    public void setReadings(List<HourlyReading> readings) { this.readings = readings; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
