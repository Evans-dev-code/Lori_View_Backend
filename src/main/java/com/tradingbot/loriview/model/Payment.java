package com.tradingbot.loriview.model;

import com.tradingbot.loriview.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    // M-Pesa specific fields
    @Column(name = "merchant_request_id", length = 100)
    private String merchantRequestId;   // from M-Pesa STK push response

    @Column(name = "checkout_request_id", length = 100, unique = true)
    private String checkoutRequestId;   // used to match M-Pesa callback

    @Column(name = "mpesa_receipt_number", length = 50)
    private String mpesaReceiptNumber;  // confirmed receipt from M-Pesa

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;         // owner's M-Pesa phone number

    @Column(name = "amount_kes", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountKes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "result_code", length = 10)
    private String resultCode;          // M-Pesa result code

    @Column(name = "result_description", length = 255)
    private String resultDescription;   // M-Pesa result message

    @Column(name = "paid_at")
    private ZonedDateTime paidAt;       // set when M-Pesa confirms

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private ZonedDateTime createdAt;
}