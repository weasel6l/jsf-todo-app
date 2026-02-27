/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import jp.co.example.ec.api.product.ProductCatalogService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CartService のロジックに対するプロパティベーステスト
 *
 * CDI なしで CartService を直接テストし、ミュータント耐性を高める
 */
@DisplayName("CartService プロパティテスト")
class CartServiceProperties {

    private CartService cartService;

    /**
     * 各テスト前に CartService を初期化する
     */
    @BeforeProperty
    void setUp() {
        cartService = new CartService(new ProductCatalogService());
    }

    /**
     * 商品 ID 1-5（全て存在する）で addToCart が常に true を返すこと
     */
    @Property(tries = 50)
    void existingProductIdAlwaysReturnsTrue(
            @ForAll @LongRange(min = 1, max = 5) long productId) {
        // Given
        cartService.clear();

        // When
        boolean result = cartService.addToCart(productId, 1);

        // Then
        assertTrue(result, "存在する productId=" + productId + " は追加できること");
    }

    /**
     * 存在しない商品 ID (100以上) で addToCart が常に false を返すこと
     */
    @Property(tries = 50)
    void nonExistingProductIdAlwaysReturnsFalse(
            @ForAll @LongRange(min = 100, max = 9999) long productId) {
        // Given
        cartService.clear();

        // When
        boolean result = cartService.addToCart(productId, 1);

        // Then
        assertFalse(result, "存在しない productId=" + productId + " は追加できないこと");
    }

    /**
     * 任意の数量 quantity (1以上) で getCart の itemCount が quantity と等しいこと
     */
    @Property(tries = 50)
    void itemCountEqualsAddedQuantity(
            @ForAll @IntRange(min = 1, max = 100) int quantity) {
        // Given
        cartService.clear();

        // When
        cartService.addToCart(1L, quantity);
        CartResponse cart = cartService.getCart();

        // Then
        assertEquals(quantity, cart.getItemCount(),
                "quantity=" + quantity + " 追加後の itemCount が一致すること");
    }

    /**
     * 新規カートの合計は常に 0 であること
     */
    @Property(tries = 1)
    void emptyCartTotalIsAlwaysZero(@ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given
        cartService.clear();

        // When
        CartResponse cart = cartService.getCart();

        // Then
        assertEquals(0, cart.getTotal().compareTo(java.math.BigDecimal.ZERO),
                "空カートの合計は 0 であること");
    }

    /**
     * getCart の返り値は常に非 null であること
     */
    @Property(tries = 1)
    void getCartAlwaysReturnsNonNull(@ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given / When
        CartResponse cart = cartService.getCart();

        // Then
        assertNotNull(cart, "getCart は非 null を返すこと");
        assertNotNull(cart.getItems(), "items は非 null であること");
    }
}
