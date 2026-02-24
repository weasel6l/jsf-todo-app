/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Todo 追加リクエスト DTO。
 * タイトルは必須、説明は任意とする。
 */
@Schema(description = "Todo 追加リクエスト")
@Getter
@Setter
@NoArgsConstructor
public class TodoAddRequest {

    /**
     * Todo のタイトル（必須・最大 100 文字）。
     */
    @Schema(description = "Todo のタイトル", required = true, maxLength = 100, example = "買い物をする")
    @NotBlank
    @Size(max = 100)
    private String title;

    /**
     * Todo の説明（任意・最大 500 文字）。
     */
    @Schema(description = "Todo の説明", maxLength = 500, example = "スーパーで食材を購入する")
    @Size(max = 500)
    private String description;
}
