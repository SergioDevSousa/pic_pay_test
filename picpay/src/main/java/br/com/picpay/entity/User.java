package br.com.picpay.entity;

import br.com.picpay.enums.UserType;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false, unique = true, length = 14)
    private String document;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType userType;

    protected User() {}

    public User(String fullName, String document, String email, String password,
                BigDecimal balance, UserType userType) {
        this.fullName = fullName;
        this.document = document;
        this.email = email;
        this.password = password;
        this.balance = balance == null ? BigDecimal.ZERO : balance;
        this.userType = userType;
    }

    public void debit(BigDecimal value) { balance = balance.subtract(value); }
    public void credit(BigDecimal value) { balance = balance.add(value); }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getDocument() { return document; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public BigDecimal getBalance() { return balance; }
    public UserType getUserType() { return userType; }
}
