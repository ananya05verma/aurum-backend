package com.aurum.transaction;

import com.aurum.portfolio.Portfolio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String instrumentName;  // e.g. HDFC MF, Reliance

    private String instrumentType;  // STOCK, MF, ETF

    private Double quantity;

    private Double pricePerUnit;

    private Double amount;

    private LocalDate transactionDate;

    private String transactionType; // BUY / SELL

    private String symbol;  // e.g. RELIANCE.NS, GOLDBEES.NS

    private String schemeCode; // for mutual funds

    @ManyToOne
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;
}