//package com.sijan.barberReservation.controller;
//
//import com.sijan.barberReservation.DTO.user.AdminBalanceResponse;
//import com.sijan.barberReservation.DTO.user.BarberBalanceResponse;
//import com.sijan.barberReservation.DTO.user.ShopAdminBalanceResponse;
//import com.sijan.barberReservation.model.*;
//import com.sijan.barberReservation.service.BarberService;
//import com.sijan.barberReservation.service.BarbershopService;
//import com.sijan.barberReservation.service.CustomerService;
//import com.sijan.barberReservation.service.PaymentService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/wallet")
//@RequiredArgsConstructor
//public class WalletController {
//
//        private final PaymentService paymentService;
//        private final CustomerService customerService;
//        private final BarberService barberService;
//        private final BarbershopService barbershopService;
//
//        // ==================== BARBER ENDPOINTS ====================
//
//        @GetMapping("/barber")
//        @PreAuthorize("hasRole('BARBER')")
//        public ResponseEntity<BarberBalanceResponse> getBarberBalance(
//                @AuthenticationPrincipal UserPrincipal principal) {
//
//            Barber barber = barberService.findById(principal.getId());
//            return ResponseEntity.ok(paymentService.getBarberBalance(barber));
//        }
//
//        @GetMapping("/barber/range")
//        @PreAuthorize("hasRole('BARBER')")
//        public ResponseEntity<BarberBalanceResponse> getBarberBalanceByDateRange(
//                @AuthenticationPrincipal UserPrincipal principal,
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//            Barber barber = barberService.findById(principal.getId());
//            return ResponseEntity.ok(paymentService.getBarberBalanceByDateRange(barber, startDate, endDate));
//        }
//
//        // ==================== SHOP ADMIN ENDPOINTS ====================
//
//        @GetMapping("/shop")
//        @PreAuthorize("hasRole('SHOP_ADMIN')")
//        public ResponseEntity<ShopAdminBalanceResponse> getShopAdminBalance(
//                @AuthenticationPrincipal UserPrincipal principal) {
//
//            Barbershop shop = barbershopService.getShopByAdminUserId(principal.getId());
//            return ResponseEntity.ok(paymentService.getShopAdminBalance(shop));
//        }
//
//        @GetMapping("/shop/{shopId}")
//        @PreAuthorize("hasAnyRole('SHOP_ADMIN', 'ADMIN')")
//        public ResponseEntity<ShopAdminBalanceResponse> getShopBalanceById(
//                @PathVariable Long shopId) {
//
//            Barbershop shop = barbershopService.getShopById(shopId);
//            return ResponseEntity.ok(paymentService.getShopAdminBalance(shop));
//        }
//
//        @GetMapping("/shop/range")
//        @PreAuthorize("hasRole('SHOP_ADMIN')")
//        public ResponseEntity<ShopAdminBalanceResponse> getShopAdminBalanceByDateRange(
//                @AuthenticationPrincipal UserPrincipal principal,
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//            Barbershop shop = barbershopService.getShopByAdminUserId(principal.getId());
//            return ResponseEntity.ok(paymentService.getShopAdminBalanceByDateRange(shop, startDate, endDate));
//        }
//
//        // ==================== ADMIN ENDPOINTS ====================
//
//        @GetMapping("/admin")
//        @PreAuthorize("hasRole('ADMIN')")
//        public ResponseEntity<AdminBalanceResponse> getAdminBalance() {
//            return ResponseEntity.ok(paymentService.getAdminBalance());
//        }
//
//        @GetMapping("/admin/range")
//        @PreAuthorize("hasRole('ADMIN')")
//        public ResponseEntity<AdminBalanceResponse> getAdminBalanceByDateRange(
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
//                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
//
//            return ResponseEntity.ok(paymentService.getAdminBalanceByDateRange(startDate, endDate));
//        }
//
//}