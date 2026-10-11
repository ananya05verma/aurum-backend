package com.aurum.transaction.service;

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
}