package com.hidroelectrica.bitacora.repository;

import com.hidroelectrica.bitacora.model.ShiftHandoff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftHandoffRepository extends JpaRepository<ShiftHandoff, Long> {
    List<ShiftHandoff> findByReportDateOrderByCreatedAtDesc(LocalDate reportDate);
    Optional<ShiftHandoff> findTopByOrderByCreatedAtDesc();
}
