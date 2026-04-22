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
import java.util.Map;

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

        // IMPORTANT: fetch only this user's transactions
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

        double totalPortfolioValue = 0;

        // First pass → calculate each SIP value
        List<Double> sipValues = new ArrayList<>();

        for (Sip sip : sips) {

            long months = ChronoUnit.MONTHS.between(
                    sip.getStartDate(),
                    LocalDate.now()
            ) + 1;

            double invested = 0;
            double units = 0;

            List<Map<String, String>> navHistory =
                    marketDataService.getNavHistory(sip.getSchemeCode());

            for (int i = 0; i < months; i++) {

                LocalDate sipDate = sip.getStartDate().plusMonths(i);

                double nav = findClosestNav(navHistory, sipDate);

                if (nav == 0) continue;

                units += sip.getMonthlyAmount() / nav;
                invested += sip.getMonthlyAmount();
            }

            double currentNAV = marketDataService.getPrice(
                    "MUTUAL_FUND",
                    null,
                    sip.getSchemeCode()
            );

            double currentValue = units * currentNAV;

            sipValues.add(currentValue);
            totalPortfolioValue += currentValue;

            holdings.add(
                    HoldingDTO.builder()
                            .fundName(sip.getFundName())
                            .invested(round(invested))
                            .currentValue(round(currentValue))
                            .units(round(units))
                            .profitLoss(round(currentValue - invested))
                            .weight(0) // temp
                            .build()
            );
        }

        // Second pass → calculate weights
        for (int i = 0; i < holdings.size(); i++) {

            double weight = totalPortfolioValue == 0
                    ? 0
                    : (sipValues.get(i) / totalPortfolioValue) * 100;

            holdings.get(i).setWeight(round(weight));
        }

        return holdings;
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private double findClosestNav(List<Map<String, String>> navHistory, LocalDate targetDate) {

        for (Map<String, String> entry : navHistory) {

            String dateStr = entry.get("date"); // format: dd-MM-yyyy

            LocalDate navDate = LocalDate.parse(
                    dateStr,
                    java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
            );

            if (!navDate.isAfter(targetDate)) {
                return Double.parseDouble(entry.get("nav"));
            }
        }

        return 0.0;
    }
}