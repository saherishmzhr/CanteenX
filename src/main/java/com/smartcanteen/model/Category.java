package com.smartcanteen.model;

public enum Category {
    VEG("VEG"),
    NON_VEG("NON-VEG");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Category fromString(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().toUpperCase().replace('_', '-');
        if ("VEG".equals(normalized)) {
            return VEG;
        }
        if ("NON-VEG".equals(normalized) || "NONVEG".equals(normalized)) {
            return NON_VEG;
        }
        throw new IllegalArgumentException("Invalid category: " + value);
    }
}
