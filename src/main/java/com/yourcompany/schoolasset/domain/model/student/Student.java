package com.yourcompany.schoolasset.domain.model.student;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.user.User;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
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
    public boolean canBorrow(int activeLoanCount, boolean hasOverdueItems) {
        if (this.isSuspended){
            return false;
        }

        if (hasOverdueItems) {
            throw new BusinessException(ErrorCode.OVERDUE_RESTRICTION); //
        }

        return activeLoanCount < 3;
    }
}