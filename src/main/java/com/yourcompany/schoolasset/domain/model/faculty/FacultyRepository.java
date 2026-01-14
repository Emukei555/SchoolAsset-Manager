package com.yourcompany.schoolasset.domain.model.faculty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacultyRepository extends JpaRepository<Faculty, Long> {
    // 基本的な findById(Long id) などは JpaRepository が提供してくれるため、
    // 現時点では追加のメソッド定義は不要です。

    // TODO [FAC-201] 教員番号（faculty_code）での検索が必要になったら追加
    // Optional<Faculty> findByFacultyCode(String facultyCode);
}