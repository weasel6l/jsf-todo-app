/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * カートへの商品追加リクエスト DTO
 *
 * 商品 ID と数量を保持する
 */
@Schema(description = "カートへの商品追加リクエスト")
@Getter
@Setter
@NoArgsConstructor
public class AddToCartRequest {

    /**
     * 追加する商品 ID
     */
    @Schema(description = "追加する商品の ID", required = true, example = "1")
    @NotNull
    private Long productId;

    /**
     * 追加する数量（1以上）
     */
    @Schema(description = "追加する数量（1以上）", required = true, example = "1")
    @NotNull
    @Min(1)
    private Integer quantity;
}
