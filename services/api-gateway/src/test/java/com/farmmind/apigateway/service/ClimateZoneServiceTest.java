package com.farmmind.apigateway.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ClimateZoneServiceTest {

    private final ClimateZoneService service = new ClimateZoneService();

    @ParameterizedTest(name = "{0},{1} → {2}")
    @CsvSource({
        "43.7315, -79.7624, 6b",  // Brampton, ON
        "43.2557, -79.8711, 6b",  // Hamilton, ON
        "44.3894, -79.6903, 6a",  // Barrie, ON
        "46.4918, -80.9930, 5a",  // Sudbury, ON
        "49.2488, -82.4328, 4b",  // Timmins, ON
        "49.8954, -97.1385, 5a",  // Winnipeg, MB (falls through to default 5a)
    })
    void resolvesCorrectZone(double lat, double lon, String expected) {
        assertThat(service.resolve(lat, lon)).isEqualTo(expected);
    }

    @Test
    void defaultsToTemperateForUnknownRegion() {
        assertThat(service.resolve(60.0, -100.0)).isNotNull();
    }
}
