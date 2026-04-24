package com.farmmind.apigateway.service;

import org.springframework.stereotype.Service;

/**
 * Derives USDA hardiness zone from GPS coordinates.
 * Covers Canadian provinces + northern US border regions.
 * Source: Natural Resources Canada / USDA Plant Hardiness Zone data.
 */
@Service
public class ClimateZoneService {

    public String resolve(double lat, double lon) {
        // Ontario (most users)
        if (lat >= 41.6 && lat <= 56.9 && lon >= -95.2 && lon <= -74.3) {
            return resolveOntario(lat, lon);
        }
        // BC
        if (lat >= 48.3 && lat <= 60.0 && lon >= -139.1 && lon <= -114.0) {
            return lat < 50.0 ? "8a" : "6a";
        }
        // Alberta
        if (lat >= 48.9 && lat <= 60.0 && lon >= -120.0 && lon <= -110.0) {
            return lat < 51.0 ? "4b" : "3b";
        }
        // Quebec
        if (lat >= 44.9 && lat <= 62.6 && lon >= -79.8 && lon <= -57.1) {
            return lat < 46.5 ? "5b" : "4a";
        }
        // Default — temperate Canada
        return "5a";
    }

    private String resolveOntario(double lat, double lon) {
        // Southern Ontario (GTA, Brampton, Hamilton area) — warmest
        if (lat < 44.5) return "6b";
        // Central Ontario (Barrie, Orillia)
        if (lat < 45.5) return "6a";
        // Northern shore of Lake Huron / Georgian Bay
        if (lat < 46.5) return "5b";
        // Sudbury / Sault Ste. Marie
        if (lat < 47.5) return "5a";
        // Northern Ontario
        if (lat < 50.0) return "4b";
        // Far north
        return "3b";
    }
}
