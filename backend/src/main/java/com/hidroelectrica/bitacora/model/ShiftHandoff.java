package com.hidroelectrica.bitacora.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shift_handoffs")
public class ShiftHandoff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(nullable = false)
    private Integer hour;

    @Column(name = "shift_type", nullable = false, length = 20)
    private String shiftType; // "Turno A", "Turno B"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivering_user_id", nullable = false)
    private User deliveringUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiving_user_id", nullable = false)
    private User receivingUser;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ShiftHandoff() {}

    public ShiftHandoff(LocalDate reportDate, Integer hour, String shiftType, User deliveringUser, User receivingUser, String notes) {
        this.reportDate = reportDate;
        this.hour = hour;
        this.shiftType = shiftType;
        this.deliveringUser = deliveringUser;
        this.receivingUser = receivingUser;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }

    public Integer getHour() { return hour; }
    public void setHour(Integer hour) { this.hour = hour; }

    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }

    public User getDeliveringUser() { return deliveringUser; }
    public void setDeliveringUser(User deliveringUser) { this.deliveringUser = deliveringUser; }

    public User getReceivingUser() { return receivingUser; }
    public void setReceivingUser(User receivingUser) { this.receivingUser = receivingUser; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
