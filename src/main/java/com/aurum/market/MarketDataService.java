package com.aurum.market;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class MarketDataService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 🔹 MAIN METHOD (ONLY MF SUPPORTED)
    public double getPrice(String instrumentType, String symbol, String schemeCode) {

        if ("MUTUAL_FUND".equalsIgnoreCase(instrumentType)) {
            return getMutualFundNAV(schemeCode);
        }

        System.out.println("Unsupported instrument type: " + instrumentType);
        return 0.0;
    }

    // ---------- MUTUAL FUND (AMFI API) ----------
    private double getMutualFundNAV(String schemeCode) {
        try {
            System.out.println("Fetching MF NAV for: " + schemeCode);

            String url = "https://api.mfapi.in/mf/" + schemeCode;

            Map response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                System.out.println("MF API returned null response");
                return 0.0;
            }

            List data = (List) response.get("data");

            if (data != null && !data.isEmpty()) {
                Map latest = (Map) data.get(0);
                Object nav = latest.get("nav");

                if (nav != null) {
                    return Double.parseDouble(nav.toString());
                }
            }

            System.out.println("NAV not found for schemeCode: " + schemeCode);

        } catch (Exception e) {
            System.out.println("MF API failed: " + e.getMessage());
        }

        return 0.0;
    }

    public List<Map<String, String>> getNavHistory(String schemeCode) {

        String url = "https://api.mfapi.in/mf/" + schemeCode;

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        return (List<Map<String, String>>) response.get("data");
    }


    public List<Map<String, Object>> getAllSchemes() {
        String url = "https://api.mfapi.in/mf";
        return restTemplate.getForObject(url, List.class);
    }
}