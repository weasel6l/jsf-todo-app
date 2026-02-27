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

/**
 * カートアイテムのレスポンス DTO
 *
 * 商品名・価格・数量・小計を保持する
 */
@Schema(description = "カートアイテム情報")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse {

    /**
     * 商品 ID
     */
    @Schema(description = "商品 ID", example = "1")
    private long productId;

    /**
     * 商品名
     */
    @Schema(description = "商品名", example = "Laptop")
    private String productName;

    /**
     * 商品単価
     */
    @Schema(description = "商品単価", example = "999.99")
    private BigDecimal price;

    /**
     * 数量
     */
    @Schema(description = "数量", example = "1")
    private int quantity;

    /**
     * 小計（単価×数量）
     */
    @Schema(description = "小計（単価×数量）", example = "999.99")
    private BigDecimal subtotal;
}
