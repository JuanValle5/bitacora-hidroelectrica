package com.hidroelectrica.bitacora.controller;

import com.hidroelectrica.bitacora.dto.request.ShiftHandoffDTO;
import com.hidroelectrica.bitacora.model.User;
import com.hidroelectrica.bitacora.security.UserContext;
import com.hidroelectrica.bitacora.service.ShiftService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(ShiftService shiftService) {
        this.shiftService = shiftService;
    }

    @GetMapping("/current")
    public ResponseEntity<ShiftService.ShiftInfo> getCurrentShift() {
        return ResponseEntity.ok(shiftService.getCurrentShift());
    }

    @PostMapping("/handoff")
    public ResponseEntity<Map<String, Object>> recordHandoff(@Valid @RequestBody ShiftHandoffDTO dto) {
        User deliveringUser = UserContext.getCurrentUser();
        Map<String, Object> result = shiftService.recordHandoff(dto, deliveringUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
