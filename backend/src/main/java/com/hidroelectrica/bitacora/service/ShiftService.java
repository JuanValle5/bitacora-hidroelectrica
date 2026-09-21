package com.hidroelectrica.bitacora.service;

import com.hidroelectrica.bitacora.dto.request.ShiftHandoffDTO;
import com.hidroelectrica.bitacora.exception.BusinessRuleException;
import com.hidroelectrica.bitacora.exception.ResourceNotFoundException;
import com.hidroelectrica.bitacora.model.ShiftHandoff;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.ShiftHandoffRepository;
import com.hidroelectrica.bitacora.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShiftService {

    private final ShiftHandoffRepository shiftHandoffRepository;
    private final UserRepository userRepository;

    public ShiftService(ShiftHandoffRepository shiftHandoffRepository, UserRepository userRepository) {
        this.shiftHandoffRepository = shiftHandoffRepository;
        this.userRepository = userRepository;
    }

    public record ShiftInfo(
        String id,
        String label,
        String range,
        Long activeOperatorId,
        String activeOperatorName,
        String activeOperatorUsername
    ) {}

    public User getActiveOperator() {
        Optional<ShiftHandoff> latestHandoff = shiftHandoffRepository.findTopByOrderByCreatedAtDesc();
        if (latestHandoff.isPresent()) {
            return latestHandoff.get().getReceivingUser();
        }
        return userRepository.findByUsername("c.mendoza")
            .orElseGet(() -> userRepository.findAll().stream()
                .filter(u -> "ROLE_OPERATOR".equals(u.getRole()) && Boolean.TRUE.equals(u.getActive()))
                .findFirst()
                .orElse(null));
    }

    public ShiftInfo getCurrentShift() {
        int hour = LocalTime.now().getHour();
        return getShiftByHour(hour);
    }

    public ShiftInfo getShiftByHour(int hour) {
        User activeOp = getActiveOperator();
        Long opId = activeOp != null ? activeOp.getId() : null;
        String opName = activeOp != null ? activeOp.getFullName() : "Sin asignar";
        String opUsername = activeOp != null ? activeOp.getUsername() : "";

        if (hour >= 6 && hour < 18) {
            return new ShiftInfo("A", "Turno A", "06:00 – 18:00", opId, opName, opUsername);
        }
        return new ShiftInfo("B", "Turno B", "18:00 – 06:00", opId, opName, opUsername);
    }

    @Transactional
    public Map<String, Object> recordHandoff(ShiftHandoffDTO dto, User deliveringUser) {
        User activeOp = getActiveOperator();

        // Si el usuario que entrega es un operador, debe ser el operador que actualmente ostenta el turno
        if (deliveringUser != null && "ROLE_OPERATOR".equals(deliveringUser.getRole())) {
            if (activeOp != null && !Objects.equals(deliveringUser.getId(), activeOp.getId())) {
                throw new BusinessRuleException("No puede entregar el turno porque usted no es el operador actualmente en turno. El turno le pertenece a " + activeOp.getFullName() + ".");
            }
        }

        User receivingUser = userRepository.findById(dto.receivingUserId())
            .orElseThrow(() -> new ResourceNotFoundException("Operario receptor no encontrado"));

        if (!Boolean.TRUE.equals(receivingUser.getActive())) {
            throw new BusinessRuleException("El operario receptor no se encuentra activo.");
        }

        if (deliveringUser != null && Objects.equals(deliveringUser.getId(), receivingUser.getId())) {
            throw new BusinessRuleException("No puede entregar el turno a usted mismo. Seleccione a otro operador activo.");
        }

        String shiftType = dto.shiftType() != null ? dto.shiftType() : getShiftByHour(dto.hour()).label();

        ShiftHandoff handoff = new ShiftHandoff(
            dto.date(),
            dto.hour(),
            shiftType,
            deliveringUser,
            receivingUser,
            dto.notes()
        );
        handoff = shiftHandoffRepository.save(handoff);

        return Map.of(
            "id", handoff.getId(),
            "date", handoff.getReportDate(),
            "shiftType", handoff.getShiftType(),
            "deliveringOperator", deliveringUser != null ? deliveringUser.getFullName() : "Sistema",
            "receivingOperator", receivingUser.getFullName(),
            "timestamp", handoff.getCreatedAt().toString(),
            "status", "COMPLETED"
        );
    }
}
