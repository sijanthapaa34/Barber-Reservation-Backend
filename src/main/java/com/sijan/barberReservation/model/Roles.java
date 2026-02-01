package com.sijan.barberReservation.model;

public enum Roles {
    // Match these exactly to your frontend expectations (lowercase strings)
    MAIN_ADMIN("main_admin"),
    SHOP_ADMIN("shop_admin"),
    BARBER("barber"),
    CUSTOMER("customer");

    private final String name;

    // Default constructor: Assigns the value to the 'name' field
    private Roles(String name) {
        this.name = name;
    }

    // Getter: Required for tokenProvider.generateToken(..., user.getRole().name())
    public String getName() {
        return name;
    }
}