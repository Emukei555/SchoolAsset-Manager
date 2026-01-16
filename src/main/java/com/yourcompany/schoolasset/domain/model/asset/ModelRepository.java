package com.yourcompany.schoolasset.domain.model.asset;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ModelRepository extends JpaRepository<Model, Long> {

    // カテゴリIDやキーワードで絞り込み、ページネーションを返す
    @Query("SELECT m FROM Model m WHERE " +
            "(:categoryId IS NULL OR m.category.id = :categoryId) AND " +
            "(:keyword IS NULL OR LOWER(m.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS string), '%')))")
    Page<Model> searchModels(
            @Param("categoryId") Long categoryId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}