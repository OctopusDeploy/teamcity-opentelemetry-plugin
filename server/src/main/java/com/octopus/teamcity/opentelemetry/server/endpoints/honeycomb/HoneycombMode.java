package com.octopus.teamcity.opentelemetry.server.endpoints.honeycomb;

import java.util.Arrays;
import java.util.Optional;

public enum HoneycombMode {
    CLASSIC("classic"),
    ENVIRONMENTS("environments");

    private final String value;

    HoneycombMode(String value) {
        this.value = value;
    }

    public static String readableJoin() {
        var sb = new StringBuilder();
        var values = HoneycombMode.values();
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].value);
            if (i < values.length - 2) {
                sb.append(", ");
            } else if (i == values.length - 2) {
                sb.append(" or ");
            }
        }
        return sb.toString();
    }

    public String getValue() {
        return value;
    }

    public static Optional<HoneycombMode> get(String value) {
        return Arrays.stream(HoneycombMode.values())
                .filter(service -> service.value.equals(value))
                .findFirst();
    }

    public static HoneycombMode getDefault()
    {
        return HoneycombMode.CLASSIC;
    }
}
