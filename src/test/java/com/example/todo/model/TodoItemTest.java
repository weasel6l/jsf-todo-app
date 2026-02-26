/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TodoItem の単体テストクラス
 */
@DisplayName("TodoItem")
class TodoItemTest {

    // ── getFormattedCreatedAt ──

    /**
     * 前提条件: createdAt が null（no-args コンストラクタで生成）
     * 事後条件: フォーマット結果が空文字列を返す
     */
    @Test
    @DisplayName("getFormattedCreatedAt - createdAt が null の場合は空文字列を返す")
    void getFormattedCreatedAt_returnsEmptyStringWhenCreatedAtIsNull() {
        // Given
        TodoItem item = new TodoItem();

        // When
        String result = item.getFormattedCreatedAt();

        // Then
        assertThat(result).isEmpty();
    }

    /**
     * 前提条件: createdAt が設定済み（パラメータ付きコンストラクタで生成）
     * 事後条件: yyyy/MM/dd HH:mm 形式の文字列を返す
     */
    @Test
    @DisplayName("getFormattedCreatedAt - createdAt が設定されている場合はフォーマットされた文字列を返す")
    void getFormattedCreatedAt_returnsFormattedStringWhenCreatedAtIsSet() {
        // Given
        TodoItem item = new TodoItem(1L, "タイトル", "説明");

        // When
        String result = item.getFormattedCreatedAt();

        // Then
        assertThat(result).matches("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}");
    }
}