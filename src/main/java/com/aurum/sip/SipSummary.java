package com.aurum.sip;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SipSummary {

    private double totalInvested;
    private double currentValue;
    private double profitLoss;
    private int months;
}