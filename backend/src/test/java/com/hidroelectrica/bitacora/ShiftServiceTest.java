package com.hidroelectrica.bitacora;

import com.hidroelectrica.bitacora.dto.request.ShiftHandoffDTO;
import com.hidroelectrica.bitacora.model.ShiftHandoff;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.repository.ShiftHandoffRepository;
import com.hidroelectrica.bitacora.repository.UserRepository;
import com.hidroelectrica.bitacora.service.ShiftService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShiftServiceTest {

    @Mock
    private ShiftHandoffRepository shiftHandoffRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ShiftService shiftService;

    @Test
    @DisplayName("Debe clasificar hora 10 como Turno A (06:00 a 18:00)")
    void testShiftA() {
        ShiftService.ShiftInfo info = shiftService.getShiftByHour(10);
        assertEquals("A", info.id());
        assertEquals("Turno A", info.label());
        assertEquals("06:00 – 18:00", info.range());
    }

    @Test
    @DisplayName("Debe clasificar hora 21 como Turno B (18:00 a 06:00)")
    void testShiftB() {
        ShiftService.ShiftInfo info = shiftService.getShiftByHour(21);
        assertEquals("B", info.id());
        assertEquals("Turno B", info.label());
        assertEquals("18:00 – 06:00", info.range());
    }

    @Test
    @DisplayName("Debe registrar entrega de turno exitosamente")
    void testRecordHandoff() {
        User del = new User("c.mendoza", "h", "Carlos Mendoza", "ROLE_OPERATOR", "Turno A");
        del.setId(1L);

        User rec = new User("r.torres", "h", "Ramiro Torres", "ROLE_OPERATOR", "Turno B");
        rec.setId(2L);

        ShiftHandoffDTO dto = new ShiftHandoffDTO(
            LocalDate.of(2026, 9, 20),
            14,
            "Turno A",
            2L,
            "Entrega sin anomalías"
        );

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(del));
        when(userRepository.findById(2L)).thenReturn(Optional.of(rec));
        when(shiftHandoffRepository.save(any(ShiftHandoff.class))).thenAnswer(i -> {
            ShiftHandoff sh = i.getArgument(0);
            sh.setId(100L);
            return sh;
        });

        Map<String, Object> result = shiftService.recordHandoff(dto, del);

        assertNotNull(result);
        assertEquals("COMPLETED", result.get("status"));
        assertEquals("Carlos Mendoza", result.get("deliveringOperator"));
        assertEquals("Ramiro Torres", result.get("receivingOperator"));
    }

    @Test
    @DisplayName("Debe rechazar entrega de turno si el usuario no es el operador en turno")
    void testRecordHandoff_NotOnDuty_ThrowsException() {
        User onDuty = new User("c.mendoza", "h", "Carlos Mendoza", "ROLE_OPERATOR", "Turno A");
        onDuty.setId(1L);

        User notOnDuty = new User("f.rios", "h", "Felipe Ríos", "ROLE_OPERATOR", "Turno B");
        notOnDuty.setId(3L);

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(onDuty));

        ShiftHandoffDTO dto = new ShiftHandoffDTO(
            LocalDate.of(2026, 9, 20),
            14,
            "Turno A",
            2L,
            "Intento de entrega sin estar en turno"
        );

        com.hidroelectrica.bitacora.exception.BusinessRuleException ex =
            assertThrows(com.hidroelectrica.bitacora.exception.BusinessRuleException.class, () ->
                shiftService.recordHandoff(dto, notOnDuty)
            );

        assertTrue(ex.getMessage().contains("No puede entregar el turno porque usted no es el operador actualmente en turno"));
    }

    @Test
    @DisplayName("Debe rechazar entrega de turno si se intenta auto-entregar el turno")
    void testRecordHandoff_SelfDelivery_ThrowsException() {
        User del = new User("c.mendoza", "h", "Carlos Mendoza", "ROLE_OPERATOR", "Turno A");
        del.setId(1L);

        when(userRepository.findByUsername("c.mendoza")).thenReturn(Optional.of(del));
        when(userRepository.findById(1L)).thenReturn(Optional.of(del));

        ShiftHandoffDTO dto = new ShiftHandoffDTO(
            LocalDate.of(2026, 9, 20),
            14,
            "Turno A",
            1L, // Same ID
            "Auto entrega"
        );

        com.hidroelectrica.bitacora.exception.BusinessRuleException ex =
            assertThrows(com.hidroelectrica.bitacora.exception.BusinessRuleException.class, () ->
                shiftService.recordHandoff(dto, del)
            );

        assertTrue(ex.getMessage().contains("No puede entregar el turno a usted mismo"));
    }
}
