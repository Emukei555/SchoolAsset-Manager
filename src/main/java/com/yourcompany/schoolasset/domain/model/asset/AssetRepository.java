package com.yourcompany.schoolasset.domain.model.asset;

import jakarta.persistence.LockModeType;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    Optional<Asset> findByIdWithLock(@Param("id") Long id);
    // とりあえず仮定義（エラー回避用）
    @Query("SELECT COUNT(a) FROM Asset a WHERE a.model.id = :modelId AND a.status = 'AVAILABLE'")
    default int countAvailableByModelId(@Param("modelId") Long modelId) {
        return 0; // 仮実装
    }
}