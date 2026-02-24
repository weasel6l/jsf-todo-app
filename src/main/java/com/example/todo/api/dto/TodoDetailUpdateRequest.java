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
 * Todo 更新リクエスト DTO。
 * タイトルは必須、説明は任意とする。
 */
@Schema(description = "Todo 更新リクエスト")
@Getter
@Setter
@NoArgsConstructor
public class TodoDetailUpdateRequest {

    /**
     * 更新後のタイトル（必須・最大 100 文字）。
     */
    @Schema(description = "更新後のタイトル", required = true, maxLength = 100, example = "新しいタイトル")
    @NotBlank
    @Size(max = 100)
    private String title;

    /**
     * 更新後の説明（任意・最大 500 文字）。
     */
    @Schema(description = "更新後の説明", maxLength = 500, example = "新しい説明")
    @Size(max = 500)
    private String description;
}
