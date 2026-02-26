/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo アイテムのレスポンス DTO
 */
@Schema(description = "Todo アイテムのレスポンス")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TodoResponse {

    /**
     * 一意識別子
     */
    @Schema(description = "Todo の一意識別子", example = "1")
    private Long id;

    /**
     * タイトル
     */
    @Schema(description = "タイトル", example = "買い物をする")
    private String title;

    /**
     * 説明
     */
    @Schema(description = "説明", example = "スーパーで食材を購入する")
    private String description;

    /**
     * 完了フラグ
     */
    @Schema(description = "完了フラグ。true の場合は完了済み", example = "false")
    private boolean completed;

    /**
     * 作成日時（yyyy/MM/dd HH:mm 形式）
     */
    @Schema(description = "作成日時", example = "2026/02/27 12:00")
    private String createdAt;
}
