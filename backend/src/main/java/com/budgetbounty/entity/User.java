package com.budgetbounty.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Builder.Default
    @Column(length = 20)
    private String role = "user";

    @Builder.Default
    @Column(name = "reward_points")
    private Integer rewardPoints = 0;

    @Builder.Default
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled = false;

    @Builder.Default
    @Column(name = "avatar_color", length = 20)
    private String avatarColor = "#5b4bff";

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LinkedAccount> linkedAccounts = new ArrayList<>();

    public String getInitials() {
        if (name == null || name.isBlank()) {
            return "??";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}
