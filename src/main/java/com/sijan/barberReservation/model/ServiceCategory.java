package com.sijan.barberReservation.model;

public enum ServiceCategory {
    HAIRCUT("Haircut"),
    BEARD("Beard & Mustache"),
    COLOR("Hair Color"),
    STYLING("Hair Styling"),
    TREATMENT("Hair Treatment"),
    PACKAGE("Service Package"),
    FACIAL("Facial & Skincare"),
    MASSAGE("Massage"),
    WAXING("Waxing"),
    KIDS("Kids Services"),
    BRIDAL("Bridal Services"),
    SPECIAL("Special Occasion");

    private final String displayName;

    ServiceCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}