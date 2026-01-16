package com.yourcompany.schoolasset.domain.model.asset;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    // 必要に応じてメソッドを追加（例: findByNameなど）
}