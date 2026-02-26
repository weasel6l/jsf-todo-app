/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Todo 追加リクエスト DTO
 */
@Schema(description = "Todo 追加リクエスト")
@Getter
@Setter
@NoArgsConstructor
public class CreateTodoRequest {

    /**
     * タイトル（必須、最大100文字）
     */
    @Schema(description = "Todo のタイトル", required = true, maxLength = 100, example = "買い物をする")
    @NotBlank
    @Size(max = 100)
    private String title;

    /**
     * 説明（任意）
     */
    @Schema(description = "Todo の説明", maxLength = 500, example = "スーパーで食材を購入する")
    private String description;
}
