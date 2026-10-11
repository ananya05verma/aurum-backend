package com.aurum.transaction.service;

import com.aurum.sip.Sip;
import com.aurum.transaction.dto.HoldingDTO;
import java.util.Map;

import com.aurum.market.MarketDataService;
import com.aurum.portfolio.Portfolio;
import com.aurum.sip.SipRepository;
import com.aurum.transaction.Transaction;
import com.aurum.transaction.repository.TransactionRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MarketDataService marketDataService;

    @Mock
    private SipRepository sipRepository;

    @InjectMocks
    private TransactionService transactionService;

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
    void calculatesPortfolioSummaryCorrectly() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        Transaction first = Transaction.builder()
                .amount(100.0)
                .quantity(10.0)
                .instrumentType("MUTUAL_FUND")
                .schemeCode("101")
                .build();

        Transaction second = Transaction.builder()
                .amount(200.0)
                .quantity(5.0)
                .instrumentType("MUTUAL_FUND")
                .schemeCode("202")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getPortfolio()).thenReturn(portfolio);

        when(portfolio.getTransactions())
                .thenReturn(List.of(first, second));

        when(marketDataService.getPrice(
                anyString(), nullable(String.class), nullable(String.class)
        )).thenReturn(12.0, 20.0);

        var summary = transactionService.getPortfolioSummary();

        assertEquals(300.0, summary.getTotalInvested(), 0.001);
        assertEquals(220.0, summary.getCurrentValue(), 0.001);
        assertEquals(-80.0, summary.getProfitLoss(), 0.001);
    }

    @Test
    void throwsExceptionWhenPortfolioIsMissing() {
        User user = mock(User.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));

        when(user.getPortfolio()).thenReturn(null);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.getPortfolioSummary()
        );

        assertEquals("Portfolio not found", exception.getMessage());
    }

    @Test
    void throwsExceptionWhenUserDoesNotExist() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> transactionService.getPortfolioSummary()
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void addsTransactionToUsersPortfolio() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        Transaction transaction = Transaction.builder()
                .amount(100.0)
                .quantity(2.0)
                .instrumentType("MUTUAL_FUND")
                .schemeCode("101")
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(transactionRepository.save(transaction))
                .thenReturn(transaction);

        Transaction result = transactionService.addTransaction(transaction);

        assertSame(transaction, result);
        assertSame(portfolio, transaction.getPortfolio());
        verify(transactionRepository).save(transaction);
    }

    @Test
    void calculatesTotalPortfolioValueCorrectly() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        Transaction first = Transaction.builder()
                .quantity(10.0)
                .instrumentType("MUTUAL_FUND")
                .symbol("FUND-A")
                .schemeCode("101")
                .build();

        Transaction second = Transaction.builder()
                .quantity(5.0)
                .instrumentType("STOCK")
                .symbol("STOCK-B")
                .schemeCode(null)
                .build();

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(portfolio.getTransactions())
                .thenReturn(List.of(first, second));
        when(marketDataService.getPrice(anyString(),
                nullable(String.class), nullable(String.class)))
                .thenReturn(12.0, 20.0);

        double result = transactionService.getTotalPortfolioValue();

        assertEquals(220.0, result, 0.001);
    }

    @Test
    void returnsEmptyHoldingsWhenUserHasNoSips() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(sipRepository.findByPortfolio(portfolio))
                .thenReturn(List.of());

        List<HoldingDTO> result = transactionService.getHoldings();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(marketDataService, never()).getNavHistory(any());
    }

    @Test
    void calculatesSipHoldingsCorrectly() {
        User user = mock(User.class);
        Portfolio portfolio = mock(Portfolio.class);
        Sip sip = mock(Sip.class);

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(user.getPortfolio()).thenReturn(portfolio);
        when(sipRepository.findByPortfolio(portfolio))
                .thenReturn(List.of(sip));

        when(sip.getStartDate()).thenReturn(
                java.time.LocalDate.now());
        when(sip.getMonthlyAmount()).thenReturn(1000.0);
        when(sip.getSchemeCode()).thenReturn("101");
        when(sip.getFundName()).thenReturn("Test Mutual Fund");

        when(marketDataService.getNavHistory("101"))
                .thenReturn(List.of(
                        Map.of("date",
                                java.time.LocalDate.now().format(
                                        java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                                "nav", "100")
                ));

        when(marketDataService.getPrice("MUTUAL_FUND", null, "101"))
                .thenReturn(110.0);

        List<HoldingDTO> result = transactionService.getHoldings();

        assertEquals(1, result.size());
        assertEquals(1000.0, result.get(0).getInvested(), 0.01);
        assertEquals(10.0, result.get(0).getUnits(), 0.01);
        assertEquals(1100.0, result.get(0).getCurrentValue(), 0.01);
        assertEquals(100.0, result.get(0).getProfitLoss(), 0.01);
        assertEquals(100.0, result.get(0).getWeight(), 0.01);
    }
}