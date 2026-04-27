package com.pickkasso.pickkasso.user.cash.controller;

import com.pickkasso.pickkasso.user.cash.dto.CashChargeRequest;
import com.pickkasso.pickkasso.user.cash.dto.CashChargeResponse;
import com.pickkasso.pickkasso.user.cash.service.CashService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
public class CashController {

    private final CashService cashService;

    @PostMapping("/charge")
    public CashChargeResponse charge(@RequestBody CashChargeRequest req,
                                     Authentication auth) {
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return cashService.charge(auth.getName(), req.getAmount());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}
