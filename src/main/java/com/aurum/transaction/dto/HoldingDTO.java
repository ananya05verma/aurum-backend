package com.aurum.transaction.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HoldingDTO {

    private String fundName;
    private double invested;
    private double currentValue;
    private double units;
    private double profitLoss;
    private double weight;
}