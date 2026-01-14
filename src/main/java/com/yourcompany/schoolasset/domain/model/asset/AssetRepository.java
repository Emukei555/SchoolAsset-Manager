package com.yourcompany.schoolasset.domain.model.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // とりあえず仮定義（エラー回避用）
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.model.id = :modelId AND a.status = 'AVAILABLE'")
    default int countAvailableByModelId(@Param("modelId") Long modelId) {
        return 0; // 仮実装
    }
}