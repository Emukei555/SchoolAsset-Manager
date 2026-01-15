package com.yourcompany.schoolasset.domain.model.asset;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    // 機材を排他ロック（FOR UPDATE）付きで取得する
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Asset a WHERE a.id = :id")
    Optional<Asset> findByIdWithLock(@Param("id") Long id);
}