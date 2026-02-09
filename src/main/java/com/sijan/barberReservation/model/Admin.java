package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Admin extends User {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id")
    private BarberShop barbershop;

    @Enumerated(EnumType.STRING)
    private AdminLevel adminLevel = AdminLevel.SHOP_ADMIN;

    private String profileImage;

    private LocalDateTime lastLoginAt;
    private String lastLoginIp;

    private String preferredContactMethod;
    private Boolean emailNotificationsEnabled = true;
    private Boolean smsNotificationsEnabled = false;

    @Transient
    public boolean isMainAdmin() {
        return adminLevel == AdminLevel.SUPER_ADMIN;
    }

    @Transient
    public boolean isShopAdmin() {
        return adminLevel == AdminLevel.SHOP_ADMIN;
    }
}