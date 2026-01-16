package com.yourcompany.schoolasset.domain.model.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clerks")
@Getter @Setter
@NoArgsConstructor
public class Clerk {
    @Id
    private Long userId;

    // Userテーブルと 1:1 で紐づき、PKを共有する
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // DBで NOT NULL 制約があるため追加必須
    @Column(name = "clerk_code", nullable = false, unique = true)
    private String clerkCode;
}
