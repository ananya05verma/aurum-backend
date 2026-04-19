package com.aurum.market;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/amfi")
@RequiredArgsConstructor
public class AmfiController {

    private final MarketDataService marketDataService;

    @GetMapping("/schemes")
    public List<Map<String, Object>> getSchemes() {
        return marketDataService.getAllSchemes();
    }
}