package com.aurum.ai;

import com.aurum.sip.SipSummary;
import org.springframework.stereotype.Service;

@Service
public class AIInsightService {

    public String generateInsights(SipSummary summary) {
        return generateFallbackInsights(summary);
    }

    private String generateFallbackInsights(SipSummary summary) {

        StringBuilder insights = new StringBuilder();

        double invested = summary.getTotalInvested();
        double current = summary.getCurrentValue();
        double profit = summary.getProfitLoss();
        int months = summary.getMonths();

        double returnPercent = (invested > 0)
                ? ((current - invested) / invested) * 100
                : 0;

        //  Duration Insight
        if (months < 6) {
            insights.append("• Your investment duration is very short. SIP works best over long-term horizons.\n");
        } else if (months < 24) {
            insights.append("• You are in the early phase of compounding. Staying consistent will improve outcomes.\n");
        } else {
            insights.append("• Your long-term consistency is strong. This is ideal for wealth creation.\n");
        }

        //  Performance Insight
        if (returnPercent < -5) {
            insights.append("• Your portfolio is in a temporary drawdown. Avoid emotional decisions.\n");
        } else if (returnPercent <= 5) {
            insights.append("• Your portfolio is relatively stable with balanced risk-return.\n");
        } else if (returnPercent <= 15) {
            insights.append("• Your portfolio is generating healthy returns.\n");
        } else {
            insights.append("• Your portfolio is performing strongly. Consider reviewing allocation.\n");
        }

        //  Investment Strength
        if (invested < 10000) {
            insights.append("• Increasing SIP contributions can significantly boost compounding.\n");
        } else if (invested < 100000) {
            insights.append("• You have built a solid base. Gradually increasing SIP can help.\n");
        } else {
            insights.append("• Strong investment base. Maintain discipline and diversification.\n");
        }

        //  Behavior Insight
        if (profit < 0 && months < 12) {
            insights.append("• Short-term losses are normal. Avoid stopping SIPs during dips.\n");
        } else if (profit > 0 && months > 24) {
            insights.append("• You are benefiting from long-term investing. Stay consistent.\n");
        }

        //  Final Tip
        insights.append("• Consistency and patience are key to long-term wealth creation.");

        return insights.toString();
    }
}