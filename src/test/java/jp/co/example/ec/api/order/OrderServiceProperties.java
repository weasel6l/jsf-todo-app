/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.order;

import jp.co.example.ec.api.cart.CartService;
import jp.co.example.ec.api.product.ProductCatalogService;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.lifecycle.BeforeProperty;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OrderService のロジックに対するプロパティベーステスト
 *
 * CDI なしで OrderService を直接テストし、ミュータント耐性を高める
 */
@DisplayName("OrderService プロパティテスト")
class OrderServiceProperties {

    private CartService cartService;
    private OrderService orderService;

    /**
     * 各テスト前にサービスを初期化する
     */
    @BeforeProperty
    void setUp() {
        cartService = new CartService(new ProductCatalogService());
        orderService = new OrderService(cartService);
    }

    /**
     * カートが空のとき createOrder は常に false を返すこと
     */
    @Property(tries = 1)
    void emptyCartCreateOrderAlwaysFalse(
            @ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given: カートが空
        cartService.clear();

        // When
        boolean result = orderService.createOrder();

        // Then
        assertFalse(result, "空カートでは注文作成が失敗すること");
    }

    /**
     * 任意の存在する商品 ID でカートに追加後、createOrder は常に true を返すこと
     *
     * @param productId ランダムな存在商品 ID (1-5)
     */
    @Property(tries = 50)
    void nonEmptyCartCreateOrderAlwaysTrue(
            @ForAll @LongRange(min = 1, max = 5) long productId) {
        // Given
        cartService.clear();
        orderService.reset();
        cartService.addToCart(productId, 1);

        // When
        boolean result = orderService.createOrder();

        // Then
        assertTrue(result, "productId=" + productId + " でカートに追加後、注文作成が成功すること");
    }

    /**
     * createOrder 成功後、getOrder は常に注文番号が非 null であること
     *
     * @param productId ランダムな存在商品 ID (1-5)
     */
    @Property(tries = 50)
    void orderNumberAlwaysNonNullAfterCreate(
            @ForAll @LongRange(min = 1, max = 5) long productId) {
        // Given
        cartService.clear();
        orderService.reset();
        cartService.addToCart(productId, 1);
        orderService.createOrder();

        // When
        OrderResponse order = orderService.getOrder();

        // Then
        assertNotNull(order.getOrderNumber(), "注文後の orderNumber は非 null であること");
    }

    /**
     * reset 後、getOrder の isOrderCompleted は常に false であること
     */
    @Property(tries = 1)
    void orderCompletedAlwaysFalseAfterReset(
            @ForAll @IntRange(min = 1, max = 1) int dummy) {
        // Given
        cartService.addToCart(1L, 1);
        orderService.createOrder();

        // When
        orderService.reset();
        OrderResponse order = orderService.getOrder();

        // Then
        assertFalse(order.isOrderCompleted(), "reset 後は orderCompleted=false であること");
    }
}
