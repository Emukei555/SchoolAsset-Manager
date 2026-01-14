package com.yourcompany.schoolasset.domain.model.loan;

import com.yourcompany.schoolasset.domain.model.asset.Asset;
import com.yourcompany.schoolasset.domain.model.asset.Model;
import com.yourcompany.schoolasset.domain.model.student.Student;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_records")
@Getter @Setter @NoArgsConstructor
public class LoanRecord {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id1;
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id")
    private Asset asset;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private Student student;

    private LocalDateTime loanedAt;
    private LocalDateTime dueDate;
    private LocalDateTime returnedAt; // これがnullなら貸出中とみなす
}
