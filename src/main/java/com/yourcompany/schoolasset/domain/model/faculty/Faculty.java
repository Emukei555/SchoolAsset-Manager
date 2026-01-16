package com.yourcompany.schoolasset.domain.model.faculty;

import com.yourcompany.schoolasset.domain.model.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "faculties")
@Getter
@Setter
public class Faculty {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId // UserのIDをそのままFacultyのPKとして使う設定
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "faculty_code", nullable = false)
    private String facultyCode;
}