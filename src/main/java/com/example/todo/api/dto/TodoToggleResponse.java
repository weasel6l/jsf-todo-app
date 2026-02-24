/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo 完了トグルレスポンス DTO。
 * トグル後の ID と完了状態を返す。
 */
@Schema(description = "完了状態トグルレスポンス")
@Getter
@AllArgsConstructor
public class TodoToggleResponse {

    /**
     * Todo の ID。
     */
    @Schema(description = "Todo の ID", example = "1")
    private Long id;

    /**
     * トグル後の完了状態フラグ。
     */
    @Schema(description = "更新後の完了フラグ", example = "true")
    private boolean completed;
}
