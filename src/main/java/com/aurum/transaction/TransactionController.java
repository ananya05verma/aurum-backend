package com.aurum.transaction;

import com.aurum.analytics.PortfolioSummary;
import com.aurum.market.MarketDataService;
import com.aurum.transaction.dto.HoldingDTO;
import com.aurum.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final MarketDataService marketDataService;

    @PostMapping
    public Transaction addTransaction(@RequestBody Transaction transaction) {
        return transactionService.addTransaction(transaction);
    }

    @GetMapping("/test")
    public String test() {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            return "No authentication found";
        }

        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
    }

    // ✅ FIXED API
    @GetMapping("/price")
    public double getPrice(
            @RequestParam String instrumentType,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String schemeCode
    ) {
        return marketDataService.getPrice(instrumentType, symbol, schemeCode);
    }

    @GetMapping("/portfolio/value")
    public double getPortfolioValue() {
        return transactionService.getTotalPortfolioValue();
    }

    @GetMapping("/portfolio/summary")
    public PortfolioSummary getSummary() {
        return transactionService.getPortfolioSummary();
    }

    @GetMapping("/portfolio/holdings")
    public List<HoldingDTO> getHoldings() {
        return transactionService.getHoldings();
    }
}