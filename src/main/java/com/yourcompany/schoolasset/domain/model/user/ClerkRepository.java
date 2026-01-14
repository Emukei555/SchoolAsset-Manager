package com.yourcompany.schoolasset.domain.model.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClerkRepository extends JpaRepository<Clerk, Long> {
    // ログイン中のユーザーIDから、事務員としての情報を引くために使います
    Optional<Clerk> findByUserId(Long userId);
}