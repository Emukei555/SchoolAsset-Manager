package com.yourcompany.schoolasset.domain.model.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {
    // 必要なメソッドがあれば後で追加
}