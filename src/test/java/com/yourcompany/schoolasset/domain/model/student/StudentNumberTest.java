package com.yourcompany.schoolasset.domain.model.student;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StudentNumberTest {

    @Test
    void 正しい8桁の学籍番号はインスタンス化できる() {
        // 正常系：エラーが起きないこと
        assertDoesNotThrow(() -> new StudentNumber("20230001"));

        StudentNumber sn = new StudentNumber("20230001");
        assertEquals("20230001", sn.getValue());
    }

    @Test
    void 学籍番号が8桁より短い場合は例外を投げる() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            new StudentNumber("1234567"); // 7桁
        });
        assertTrue(ex.getMessage().contains("8桁である必要があります"));
    }

    @Test
    void 学籍番号が8桁より長い場合は例外を投げる() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentNumber("123456789"); // 9桁
        });
    }

    @Test
    void 学籍番号が空文字の場合は例外を投げる() {
        assertThrows(IllegalArgumentException.class, () -> {
            new StudentNumber("");
        });
    }

    @Test
    void 同じ値を持つインスタンス同士は等価とみなされる() {
        // Value Object の重要な性質（値オブジェクトは「属性」が同じなら同じもの）
        StudentNumber sn1 = new StudentNumber("20230001");
        StudentNumber sn2 = new StudentNumber("20230001");

        assertEquals(sn1, sn2);
        assertEquals(sn1.hashCode(), sn2.hashCode());
    }
}