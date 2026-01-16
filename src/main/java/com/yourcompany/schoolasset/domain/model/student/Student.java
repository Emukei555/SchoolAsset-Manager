package com.yourcompany.schoolasset.domain.model.student;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.model.user.User;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

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

    @Column(name = "student_number", nullable = false, unique = true)
    private String studentNumber;

    @Column(nullable = false)
    private Integer grade;

    @Column(nullable = false)
    private String department;

    // 任意項目ですがスキーマに合わせて定義しておきます
    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "graduation_date")
    private LocalDate graduationDate;

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