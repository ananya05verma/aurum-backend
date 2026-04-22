package com.aurum.sip;

import com.aurum.market.MarketDataService;
import com.aurum.portfolio.Portfolio;
import com.aurum.user.User;
import com.aurum.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static java.lang.Math.round;

@Service
@RequiredArgsConstructor
public class SipService {

    private final UserRepository userRepository;
    private final SipRepository sipRepository;
    private final MarketDataService marketDataService;

    public SipSummary calculateSip() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        List<Sip> sips = sipRepository.findByPortfolio(portfolio);

        double totalInvested = 0;
        double totalCurrentValue = 0;

        for (Sip sip : sips) {

            long months = ChronoUnit.MONTHS.between(
                    sip.getStartDate(),
                    LocalDate.now()
            ) + 1;

            double sipInvested = 0;
            double sipUnits = 0;

            List<Map<String, String>> navHistory =
                    marketDataService.getNavHistory(sip.getSchemeCode());

            for (int i = 0; i < months; i++) {

                LocalDate sipDate = sip.getStartDate().plusMonths(i);

                double nav = findClosestNav(navHistory, sipDate);

                //  safety check
                if (nav == 0) continue;

                double units = sip.getMonthlyAmount() / nav;

                sipUnits += units;
                sipInvested += sip.getMonthlyAmount();
            }

            totalInvested += sipInvested;

            //  fetch NAV for THIS SIP
            double currentNAV = marketDataService.getPrice(
                    "MUTUAL_FUND",
                    null,
                    sip.getSchemeCode()
            );

            double sipCurrentValue = sipUnits * currentNAV;

            totalCurrentValue += sipCurrentValue;
        }

        return SipSummary.builder()
                .totalInvested(round(totalInvested))
                .currentValue(round(totalCurrentValue))
                .profitLoss(round(totalCurrentValue - totalInvested))
                .months(sips.isEmpty() ? 0 :
                        sips.stream()
                        .mapToInt(s -> (int) ChronoUnit.MONTHS.between(
                                s.getStartDate(),
                                LocalDate.now()
                        ) + 1)
                        .max()
                        .orElse(0)
                )
                .build();
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

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    public Sip createSip(Sip sip) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Portfolio portfolio = user.getPortfolio();

        sip.setPortfolio(portfolio);

        return sipRepository.save(sip);
    }

    public List<Sip> getUserSips() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return sipRepository.findByPortfolio(user.getPortfolio());
    }
}