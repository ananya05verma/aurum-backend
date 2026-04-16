package com.aurum.user;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import com.aurum.portfolio.Portfolio;
import jakarta.persistence.OneToOne;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @OneToOne(mappedBy = "user")
    private Portfolio portfolio;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    private String passwordHash;

    private LocalDateTime createdAt;
}