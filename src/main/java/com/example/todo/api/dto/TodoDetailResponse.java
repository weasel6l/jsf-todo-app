/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo 詳細レスポンス DTO。
 * 詳細取得および更新のレスポンスで使用する。
 */
@Schema(description = "Todo 詳細レスポンス")
@Getter
@AllArgsConstructor
public class TodoDetailResponse {

    /**
     * Todo の ID。
     */
    @Schema(description = "Todo の ID", example = "1")
    private Long id;

    /**
     * Todo のタイトル。
     */
    @Schema(description = "タイトル", example = "買い物をする")
    private String title;

    /**
     * Todo の説明。
     */
    @Schema(description = "説明", example = "スーパーで食材を購入する")
    private String description;

    /**
     * 完了状態フラグ。
     */
    @Schema(description = "完了フラグ", example = "false")
    private boolean completed;

    /**
     * フォーマット済み作成日時文字列。
     */
    @Schema(description = "作成日時（フォーマット済み）", example = "2026/02/25 10:00")
    private String formattedCreatedAt;
}
