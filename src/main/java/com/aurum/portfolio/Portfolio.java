package com.aurum.portfolio;

import com.aurum.transaction.Transaction;
import com.aurum.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;
import java.util.List;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "portfolio")
    private List<Transaction> transactions;
}