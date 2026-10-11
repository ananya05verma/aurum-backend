
package com.aurum.market;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MarketDataServiceTest {

    @Test
    void returnsZeroForUnsupportedInstrument() {
        MarketDataService service = new MarketDataService();

        double result = service.getPrice("STOCK", "ABC", null);

        assertEquals(0.0, result);
    }

    @Test
    void returnsLatestNavForMutualFund() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MarketDataService service = new MarketDataService(restTemplate);

        Map<String, Object> response = Map.of(
                "data", List.of(Map.of("nav", "125.50"))
        );

        when(restTemplate.getForObject(
                "https://api.mfapi.in/mf/101", Map.class
        )).thenReturn(response);

        double result = service.getPrice("MUTUAL_FUND", null, "101");

        assertEquals(125.50, result, 0.001);
    }

    @Test
    void returnsZeroWhenApiResponseIsNull() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MarketDataService service = new MarketDataService(restTemplate);

        when(restTemplate.getForObject(
                "https://api.mfapi.in/mf/101", Map.class
        )).thenReturn(null);

        assertEquals(0.0, service.getPrice("MUTUAL_FUND", null, "101"));
    }

    @Test
    void returnsZeroWhenNavIsMissing() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MarketDataService service = new MarketDataService(restTemplate);

        when(restTemplate.getForObject(
                "https://api.mfapi.in/mf/101", Map.class
        )).thenReturn(Map.of("data", List.of(Map.of("date", "11-10-2026"))));

        assertEquals(0.0, service.getPrice("MUTUAL_FUND", null, "101"));
    }

    @Test
    void returnsNavHistory() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        MarketDataService service = new MarketDataService(restTemplate);

        List<Map<String, String>> history = List.of(
                Map.of("date", "11-10-2026", "nav", "125.50")
        );

        when(restTemplate.getForObject(
                "https://api.mfapi.in/mf/101", Map.class
        )).thenReturn(Map.of("data", history));

        assertEquals(history, service.getNavHistory("101"));
    }
}
