/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;

/**
 * 商品情報レスポンス DTO
 *
 * GET /api/products のレスポンス要素として使用する
 */
@Schema(description = "商品情報")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {

    /**
     * 商品の一意識別子
     */
    @Schema(description = "商品 ID", example = "1")
    private long id;

    /**
     * 商品名
     */
    @Schema(description = "商品名", example = "Laptop")
    private String name;

    /**
     * 商品説明
     */
    @Schema(description = "商品説明", example = "High-performance laptop")
    private String description;

    /**
     * 税抜き価格
     */
    @Schema(description = "価格（税抜き）", example = "999.99")
    private BigDecimal price;

    /**
     * 現在の在庫数
     */
    @Schema(description = "在庫数", example = "10")
    private int stock;

    /**
     * 在庫切れフラグ
     */
    @Schema(description = "在庫切れかどうかを示すフラグ。true の場合は購入不可", example = "false")
    private boolean outOfStock;
}
