/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/**
 * カート情報のレスポンス DTO
 *
 * カートアイテム一覧・合計金額・合計数量・空判定を保持する
 */
@Schema(description = "カート情報")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {

    /**
     * カートアイテム一覧
     */
    @Schema(description = "カートアイテムの一覧")
    private List<CartItemResponse> items;

    /**
     * 合計金額
     */
    @Schema(description = "合計金額", example = "999.99")
    private BigDecimal total;

    /**
     * 合計数量（全アイテムの quantity 合算）
     */
    @Schema(description = "合計数量", example = "3")
    private int itemCount;

    /**
     * カートが空かどうかを示すフラグ
     */
    @Schema(description = "カートが空かどうかを示すフラグ。true の場合は空", example = "false")
    private boolean isEmpty;
}
