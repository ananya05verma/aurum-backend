package com.aurum.transaction.service;

import com.aurum.market.MarketDataService;
import com.aurum.portfolio.Portfolio;
import com.aurum.sip.SipRepository;
import com.aurum.transaction.Transaction;
import com.aurum.transaction.dto.HoldingDTO;
import com.aurum.transaction.repository.TransactionRepository;
import com.aurum.user.User;
import com.aurum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.aurum.analytics.PortfolioSummary;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.aurum.sip.Sip;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;
    private final SipRepository sipRepository;

    public Transaction addTransaction(Transaction transaction) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null) {
            throw new RuntimeException("Portfolio not created yet");
        }

        // attach portfolio
        transaction.setPortfolio(portfolio);

        return transactionRepository.save(transaction);
    }
    public double getTotalPortfolioValue() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null) {
            throw new RuntimeException("Portfolio not found");
        }

        double totalValue = 0;

        // 👉 IMPORTANT: fetch only this user's transactions
        for (Transaction t : portfolio.getTransactions()) {

            double currentPrice = marketDataService.getPrice(
                    t.getInstrumentType(),
                    t.getSymbol(),
                    t.getSchemeCode()
            );

            totalValue += t.getQuantity() * currentPrice;
        }

        return totalValue;
    }

    public PortfolioSummary getPortfolioSummary() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        if (portfolio == null) {
            throw new RuntimeException("Portfolio not found");
        }

        double totalInvested = 0;
        double currentValue = 0;

        for (Transaction t : portfolio.getTransactions()) {

            // invested amount
            totalInvested += t.getAmount();

            // current value using NAV
            double nav = marketDataService.getPrice(
                    t.getInstrumentType(),
                    t.getSymbol(),
                    t.getSchemeCode()
            );

            currentValue += t.getQuantity() * nav;
        }

        return PortfolioSummary.builder()
                .totalInvested(totalInvested)
                .currentValue(currentValue)
                .profitLoss(currentValue - totalInvested)
                .build();
    }

    public List<HoldingDTO> getHoldings() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        List<Sip> sips = sipRepository.findByPortfolio(portfolio);

        List<HoldingDTO> holdings = new ArrayList<>();
        double totalValue = 0;

        for (Sip sip : sips) {

            long months = ChronoUnit.MONTHS.between(
                    sip.getStartDate(),
                    LocalDate.now()
            );

            double invested = months * sip.getMonthlyAmount();

            double nav = marketDataService.getPrice(
                    "MUTUAL_FUND",
                    null,
                    sip.getSchemeCode()
            );

            double units = invested / nav;
            double currentValue = units * nav;

            totalValue += currentValue;

            holdings.add(HoldingDTO.builder()
                    .fundName(sip.getFundName())
                    .invested(invested)
                    .currentValue(currentValue)
                    .units(units)
                    .profitLoss(currentValue - invested)
                    .weight(0)
                    .build());
        }

        // calculate weight
        for (HoldingDTO h : holdings) {
            double weight = (h.getCurrentValue() / totalValue) * 100;
            h.setWeight(weight);
        }

        return holdings;
    }
}