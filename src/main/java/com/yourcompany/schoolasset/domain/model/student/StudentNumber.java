package com.yourcompany.schoolasset.domain.model.student;

import io.jsonwebtoken.lang.Assert;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable // JPAで埋め込み可能にする
@Getter
@EqualsAndHashCode // 値が同じなら等価とみなす（VOの要件）
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA用
public class StudentNumber implements Serializable {
    @Column(name = "student_number")
    private String value;

    public StudentNumber(String value) {
        Assert.hasText(value, "学籍番号は必須です");
        if (value.length() != 8) {
            throw new IllegalArgumentException("学籍番号は8桁である必要があります: " + value);
        }
        this.value = value;
    }
    @Override
    public String toString() {
        return value;
    }
}
