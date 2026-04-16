package com.aurum.analytics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PortfolioSummary {

    private double totalInvested;
    private double currentValue;
    private double profitLoss;
}