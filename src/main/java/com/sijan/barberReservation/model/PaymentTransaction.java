package com.sijan.barberReservation.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version; // Optimistic locking version

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee", precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "shop_earnings", precision = 10, scale = 2)
    private BigDecimal shopEarnings;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "pidx")
    private String pidx;

    @Column(name = "ref_id")
    private String refId;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barber_id", nullable = false)
    private Barber barber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    @ManyToMany
    @JoinTable(
            name = "payment_transaction_services",
            joinColumns = @JoinColumn(name = "payment_transaction_id"),
            inverseJoinColumns = @JoinColumn(name = "service_offering_id")
    )
    private List<ServiceOffering> services;

    @OneToOne(mappedBy = "paymentTransaction")
    private Appointment appointment;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (refundStatus == null) {
            refundStatus = RefundStatus.NOT_APPLICABLE;
        }
        if (status == null) {
            status = TransactionStatus.PENDING;
        }
    }

    // In PaymentTransaction entity
    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "refund_percentage")
    private Double refundPercentage;

    @Column(name = "penalty_amount")
    private BigDecimal penaltyAmount;

    @Column(name = "refund_status")
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;  // NEW enum

    public boolean isPending() {
        return status == TransactionStatus.PENDING;
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == TransactionStatus.FAILED;
    }
}
