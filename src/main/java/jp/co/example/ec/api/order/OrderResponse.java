/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.order;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 注文情報のレスポンス DTO
 *
 * 注文番号・合計金額・注文日時・ステータス・完了フラグを保持する
 */
@Schema(description = "注文情報")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {

    /**
     * 注文番号（8文字の大文字英数字）
     */
    @Schema(description = "注文番号（8文字の大文字英数字）", example = "A1B2C3D4")
    private String orderNumber;

    /**
     * 注文合計金額
     */
    @Schema(description = "注文合計金額", example = "999.99")
    private BigDecimal orderTotal;

    /**
     * 注文日時
     */
    @Schema(description = "注文日時", example = "2026-02-28T01:00:00")
    private LocalDateTime orderDate;

    /**
     * 注文ステータス
     */
    @Schema(description = "注文ステータス", example = "COMPLETED")
    private String status;

    /**
     * 注文完了フラグ
     */
    @Schema(description = "注文完了フラグ。true の場合は注文完了済み", example = "true")
    private boolean orderCompleted;
}
