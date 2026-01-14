package com.yourcompany.schoolasset.domain.model.user;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "clerks")
@Getter
@Setter
public class Clerk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ログインユーザー（Userテーブル）との紐付け用ID
    // Facultyと同じく、どのユーザーアカウントがこの事務員なのかを識別します
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    // TODO [CLERK-101] 部署名や担当エリアなど、事務員特有の情報があれば追加
    // private String departmentName;
}
