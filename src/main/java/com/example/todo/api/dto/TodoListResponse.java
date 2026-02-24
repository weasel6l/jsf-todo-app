/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo 一覧画面のレスポンス DTO。
 * 統計情報と Todo アイテムリストを含む。
 */
@Schema(description = "Todo 一覧レスポンス")
@Getter
@AllArgsConstructor
public class TodoListResponse {

    /**
     * 合計件数。
     */
    @Schema(description = "合計件数", example = "3")
    private long totalCount;

    /**
     * 完了済み件数。
     */
    @Schema(description = "完了件数", example = "1")
    private long completedCount;

    /**
     * 未完了件数。
     */
    @Schema(description = "未完了件数", example = "2")
    private long pendingCount;

    /**
     * Todo アイテムリスト。
     */
    @Schema(description = "Todo アイテムリスト")
    private List<TodoItemDto> items;
}
