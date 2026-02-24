/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo 追加レスポンス DTO。
 * 追加した Todo の全情報を返す。
 */
@Schema(description = "Todo 追加レスポンス")
@Getter
@AllArgsConstructor
public class TodoAddResponse {

    /**
     * 採番された Todo の ID。
     */
    @Schema(description = "採番された ID", example = "4")
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
     * 完了状態フラグ（追加直後は常に false）。
     */
    @Schema(description = "完了フラグ（追加直後は false）", example = "false")
    private boolean completed;

    /**
     * フォーマット済み作成日時文字列。
     */
    @Schema(description = "作成日時（フォーマット済み）", example = "2026/02/25 10:00")
    private String formattedCreatedAt;
}
