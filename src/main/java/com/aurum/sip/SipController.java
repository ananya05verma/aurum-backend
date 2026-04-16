package com.aurum.sip;

import com.aurum.ai.AIInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sip")
@RequiredArgsConstructor
public class SipController {

    private final SipService sipService;
    private final AIInsightService aiInsightService;

    @GetMapping("/summary")
    public Map<String, Object> getSipSummary() {

        SipSummary summary = sipService.calculateSip();

        return Map.of(
                "status", "success",
                "data", summary
        );
    }

    @PostMapping
    public Sip createSip(@RequestBody Sip sip) {
        return sipService.createSip(sip);
    }

    @GetMapping("/ai-insights")
    public String getAIInsights() {
        SipSummary summary = sipService.calculateSip();
        return aiInsightService.generateInsights(summary);
    }

    @GetMapping
    public List<Sip> getAllSips() {
        return sipService.getUserSips();
    }
}