package com.cambiaso.ioc.controller;

import com.cambiaso.ioc.dto.ai.DashboardExplanationRequest;
import com.cambiaso.ioc.dto.ai.DashboardExplanationResponse;
import com.cambiaso.ioc.service.ai.DynamicDashboardExplanationService;
import com.cambiaso.ioc.service.DashboardAccessService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v2/ai")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AiExplanationControllerV2 {

    private final DynamicDashboardExplanationService explanationService;
    private final DashboardAccessService dashboardAccessService;

    @PostMapping(path = { "/explain", "/explain-dashboard" })
    @RateLimiter(name = "aiExplanation")
    public ResponseEntity<DashboardExplanationResponse> explainDashboard(
            @Valid @RequestBody DashboardExplanationRequest request,
            Authentication authentication) {
        log.info("V2 AI explanation requested - User: {}, Dashboard: {}",
                authentication.getName(), request.dashboardId());

        // Verify access
        dashboardAccessService.checkAccessOrThrow(authentication, request.dashboardId());

        // Generate dynamic explanation
        DashboardExplanationResponse response = explanationService.explainDashboard(request);

        return ResponseEntity.ok(response);
    }
}
