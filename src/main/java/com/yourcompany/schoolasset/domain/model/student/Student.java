package com.yourcompany.schoolasset.domain.model.student;

import com.yourcompany.schoolasset.domain.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "students")
@Getter @Setter @NoArgsConstructor
public class Student {
    @Id
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private boolean isSuspended;

    /**
     * 貸出可能かどうかを判定するドメインロジック
     */
    public boolean canBorrow() {
        // 貸出停止フラグが立っていれば借りられない
        return !isSuspended;
    }
}