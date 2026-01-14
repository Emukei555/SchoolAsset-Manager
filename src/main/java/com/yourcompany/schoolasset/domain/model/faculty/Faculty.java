package com.yourcompany.schoolasset.domain.model.faculty;

import jakarta.persistence.*;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "faculties")
@Getter @Setter
public class Faculty {
    @Id
    private Long id; // usersテーブルのIDと同じ値が入る想定

    @Column(name = "faculty_code", unique = true)
    private String facultyCode;

    // TODO [JIRA-FAC-102] 氏名などはUserテーブル側にあるので、必要に応じてOneToOneを検討
}