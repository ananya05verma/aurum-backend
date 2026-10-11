package com.aurum.sip;

import com.aurum.market.MarketDataService;
import com.aurum.portfolio.Portfolio;
import com.aurum.user.User;
import com.aurum.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SipServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private SipRepository sipRepository;

    @Mock
    private MarketDataService marketDataService;

    @InjectMocks
    private SipService sipService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "test@example.com", null
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsZeroSummaryWhenNoSipsExist() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(sipRepository.findByPortfolio(portfolio))
                .thenReturn(List.of());

        SipSummary summary = sipService.calculateSip();

        assertEquals(0.0, summary.getTotalInvested(), 0.001);
        assertEquals(0.0, summary.getCurrentValue(), 0.001);
        assertEquals(0.0, summary.getProfitLoss(), 0.001);
        assertEquals(0, summary.getMonths());

        verifyNoInteractions(marketDataService);
    }

    @Test
    void calculatesSipSummaryUsingNavHistory() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        Sip sip = Sip.builder()
                .fundName("Test Fund")
                .schemeCode("101")
                .monthlyAmount(1000.0)
                .startDate(LocalDate.now())
                .portfolio(portfolio)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(sipRepository.findByPortfolio(portfolio))
                .thenReturn(List.of(sip));

        when(marketDataService.getNavHistory("101"))
                .thenReturn(List.of(Map.of(
                        "date", LocalDate.now().format(
                                java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        ),
                        "nav", "100"
                )));

        when(marketDataService.getPrice(
                "MUTUAL_FUND", null, "101"
        )).thenReturn(110.0);

        SipSummary summary = sipService.calculateSip();

        assertEquals(1000.0, summary.getTotalInvested(), 0.001);
        assertEquals(1100.0, summary.getCurrentValue(), 0.001);
        assertEquals(100.0, summary.getProfitLoss(), 0.001);
        assertEquals(1, summary.getMonths());
    }

    @Test
    void throwsExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> sipService.calculateSip()
        );

        assertEquals("User not found", exception.getMessage());
        verifyNoInteractions(marketDataService);
    }
}