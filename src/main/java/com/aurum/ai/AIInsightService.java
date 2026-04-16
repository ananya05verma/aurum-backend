package com.aurum.ai;

import com.aurum.sip.SipSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AIInsightService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final GlobalAIUsageRepository globalAIUsageRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateInsights(SipSummary summary) {

        // 🔴 GLOBAL LIMIT
        LocalDate today = LocalDate.now();

        GlobalAIUsage usage = globalAIUsageRepository
                .findByDate(today)
                .orElse(
                        GlobalAIUsage.builder()
                                .date(today)
                                .count(0)
                                .build()
                );

        if (usage.getCount() >= 5) {
            return "Daily AI limit reached (5 calls/day). Try again tomorrow.";
        }

        try {
            // 🔴 PROMPT
            String prompt = "You are a financial advisor.\n" +
                    "Analyze this portfolio:\n" +
                    "Total Invested: " + summary.getTotalInvested() + "\n" +
                    "Current Value: " + summary.getCurrentValue() + "\n" +
                    "Profit/Loss: " + summary.getProfitLoss() + "\n" +
                    "Months: " + summary.getMonths() + "\n\n" +
                    "Give 3 short practical investment insights.";

            // 🔴 REQUEST BODY
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4o-mini");

            List<Map<String, String>> messages = new ArrayList<>();

            messages.add(Map.of(
                    "role", "system",
                    "content", "You are a financial advisor."
            ));

            messages.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

            requestBody.put("messages", messages);

            // 🔴 HEADERS
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            // 🔴 API CALL
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.openai.com/v1/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // 🔴 PARSE RESPONSE
            List choices = (List) response.getBody().get("choices");
            Map choice = (Map) choices.get(0);
            Map message = (Map) choice.get("message");

            String result = message.get("content").toString();

            // 🔴 UPDATE COUNT
            usage.setCount(usage.getCount() + 1);
            globalAIUsageRepository.save(usage);

            return result;

        } catch (Exception e) {
            return generateFallbackInsights(summary);
        }
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

        // 🔹 1. Duration Insight
        if (months < 6) {
            insights.append("• Your investment duration is still very short. SIP strategies typically show meaningful results over 2–3 years.\n");
        } else if (months < 24) {
            insights.append("• You are in the early phase of compounding. Staying consistent will significantly improve long-term outcomes.\n");
        } else {
            insights.append("• Your long-term consistency is strong. This is ideal for wealth compounding strategies.\n");
        }

        // 🔹 2. Performance Insight
        if (returnPercent < -5) {
            insights.append("• Your portfolio is experiencing a temporary drawdown. This is common in markets—avoid reacting emotionally.\n");
        } else if (returnPercent >= -5 && returnPercent <= 5) {
            insights.append("• Your portfolio is relatively stable. This suggests a balanced risk-return profile.\n");
        } else if (returnPercent > 5 && returnPercent <= 15) {
            insights.append("• Your portfolio is generating moderate positive returns. This indicates healthy growth.\n");
        } else {
            insights.append("• Your portfolio is performing strongly. You may review allocation or consider partial profit booking.\n");
        }

        // 🔹 3. Investment Strength
        if (invested < 10000) {
            insights.append("• Your current investment amount is relatively low. Increasing SIP contributions can accelerate compounding.\n");
        } else if (invested < 100000) {
            insights.append("• You have built a decent investment base. Gradually increasing SIP can enhance long-term wealth.\n");
        } else {
            insights.append("• Your investment base is strong. Maintaining discipline and diversification is key going forward.\n");
        }

        // 🔹 4. Risk + Behavior Insight
        if (profit < 0 && months < 12) {
            insights.append("• Short-term losses are normal. Avoid stopping SIPs during market corrections.\n");
        } else if (profit > 0 && months > 24) {
            insights.append("• You are benefiting from long-term investing. Staying invested is crucial for continued growth.\n");
        }

        // 🔹 5. Final Smart Tip
        insights.append("• Consistency, patience, and disciplined investing are the key drivers of long-term portfolio success.");

        return insights.toString();
    }
}