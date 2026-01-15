package com.yourcompany.schoolasset.domain.model.asset;

import com.yourcompany.schoolasset.domain.exception.BusinessException;
import com.yourcompany.schoolasset.domain.shared.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "assets")
@Getter @Setter @NoArgsConstructor
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status = AssetStatus.AVAILABLE;

    public void rentOut() {
        // 在庫ロックの整合性チェック
        if (this.status != AssetStatus.AVAILABLE) {
            throw new BusinessException(ErrorCode.OUT_OF_STOCK); // "在庫がありません"
        }
        this.status = AssetStatus.LENT; // ステータス変更
    }

    public void returnBack() {
        if (this.status == AssetStatus.AVAILABLE) {
            return;
        }
        this.status = AssetStatus.AVAILABLE;
    }

    private String location;
    private String note;
}
