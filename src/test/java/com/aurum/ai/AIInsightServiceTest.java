
package com.aurum.ai;

import com.aurum.sip.SipSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AIInsightServiceTest {

    private final AIInsightService service = new AIInsightService();

    private SipSummary createSummary(
            double invested,
            double current,
            double profit,
            int months) {

        SipSummary summary = mock(SipSummary.class);
        when(summary.getTotalInvested()).thenReturn(invested);
        when(summary.getCurrentValue()).thenReturn(current);
        when(summary.getProfitLoss()).thenReturn(profit);
        when(summary.getMonths()).thenReturn(months);
        return summary;
    }

    @Test
    void generatesInsightsForShortTermInvestmentAndLowInvestment() {
        SipSummary summary = createSummary(5000, 4500, -500, 3);

        String result = service.generateInsights(summary);

        assertTrue(result.contains("investment duration is very short"));
        assertTrue(result.contains("temporary drawdown"));
        assertTrue(result.contains("Increasing SIP contributions"));
        assertTrue(result.contains("Short-term losses are normal"));
        assertTrue(result.contains("Consistency and patience"));
    }

    @Test
    void generatesInsightsForLongTermStrongReturns() {
        SipSummary summary = createSummary(150000, 180000, 30000, 36);

        String result = service.generateInsights(summary);

        assertTrue(result.contains("long-term consistency is strong"));
        assertTrue(result.contains("performing strongly"));
        assertTrue(result.contains("Strong investment base"));
        assertTrue(result.contains("benefiting from long-term investing"));
    }

    @Test
    void handlesZeroInvestmentWithoutDivisionByZero() {
        SipSummary summary = createSummary(0, 0, 0, 6);

        String result = service.generateInsights(summary);

        assertNotNull(result);
        assertTrue(result.contains("early phase of compounding"));
        assertTrue(result.contains("portfolio is relatively stable"));
        assertTrue(result.contains("Increasing SIP contributions"));
    }

    @Test
    void generatesInsightsForModerateReturnsAndInvestmentBase() {
        SipSummary summary = createSummary(50000, 55000, 5000, 12);

        String result = service.generateInsights(summary);

        assertTrue(result.contains("early phase of compounding"));
        assertTrue(result.contains("healthy returns"));
        assertTrue(result.contains("solid base"));
        assertFalse(result.contains("benefiting from long-term investing"));
    }
}
