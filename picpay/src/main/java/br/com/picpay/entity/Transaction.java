package br.com.picpay.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id", nullable = false)
    private User payee;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected Transaction() {}

    public Transaction(BigDecimal value, User payer, User payee) {
        this.value = value;
        this.payer = payer;
        this.payee = payee;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public BigDecimal getValue() { return value; }
    public User getPayer() { return payer; }
    public User getPayee() { return payee; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
