/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.order;

import io.helidon.microprofile.tests.junit5.HelidonTest;
import jp.co.example.ec.api.cart.CartService;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * OrderResource の統合テスト
 *
 * /api/orders エンドポイントの正常系・異常系を検証する
 */
@HelidonTest
@DisplayName("OrderResource")
class OrderResourceTest {

    /**
     * JAX-RS テスト用 WebTarget（Helidon テストフレームワークが注入する）
     */
    @Inject
    WebTarget target;

    /**
     * カートサービス（テスト前後の状態リセット用）
     */
    @Inject
    CartService cartService;

    /**
     * 注文サービス（テスト前後の状態リセット用）
     */
    @Inject
    OrderService orderService;

    /**
     * 各テスト前にカートと注文をクリアして状態をリセットする
     */
    @BeforeEach
    void setUp() {
        cartService.clear();
        orderService.reset();
    }

    /**
     * 前提条件: カートに商品が1件以上ある状態
     * 期待する事後条件: HTTP 200 が返り、注文番号・合計金額・ステータスが正しく設定されること
     */
    @Test
    @DisplayName("POST /api/orders - カートに商品がある場合に注文を作成できること")
    void createOrderWithItems() {
        // Given: 商品 id=1 をカートに追加
        Map<String, Object> addBody = Map.of("productId", 1L, "quantity", 1);
        target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(addBody));

        // When
        Response response = target.path("/api/orders")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> order = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertNotNull(order.get("orderNumber"), "注文番号があること");
        assertEquals("COMPLETED", order.get("status"), "ステータスが COMPLETED であること");
        assertTrue((Boolean) order.get("orderCompleted"), "orderCompleted=true であること");
    }

    /**
     * 前提条件: カートが空の状態
     * 期待する事後条件: HTTP 400 が返ること
     */
    @Test
    @DisplayName("POST /api/orders - カートが空の場合は 400 を返すこと")
    void createOrderWithEmptyCartReturns400() {
        // Given: カートが空

        // When
        Response response = target.path("/api/orders")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));

        // Then
        assertEquals(400, response.getStatus(), "HTTP 400 が返ること");
    }

    /**
     * 前提条件: 注文完了済みの状態
     * 期待する事後条件: HTTP 200 が返り、注文情報が取得できること
     */
    @Test
    @DisplayName("GET /api/orders/current - 注文完了後に注文情報を取得できること")
    void getCurrentOrderAfterCreation() {
        // Given: カートに商品を追加して注文を作成
        Map<String, Object> addBody = Map.of("productId", 2L, "quantity", 2);
        target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(addBody));
        target.path("/api/orders")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(""));

        // When
        Response response = target.path("/api/orders/current")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> order = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertTrue((Boolean) order.get("orderCompleted"), "orderCompleted=true であること");
        assertNotNull(order.get("orderNumber"), "注文番号があること");
    }

    /**
     * 前提条件: 注文がない状態
     * 期待する事後条件: HTTP 200 が返り、orderCompleted=false であること
     */
    @Test
    @DisplayName("GET /api/orders/current - 注文前は orderCompleted=false であること")
    void getCurrentOrderBeforeCreation() {
        // Given: 注文が作成されていない状態（setUp でカートクリア済み）

        // When
        Response response = target.path("/api/orders/current")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> order = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertFalse((Boolean) order.get("orderCompleted"), "注文前は orderCompleted=false であること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: OrderResponse のデフォルトコンストラクタでインスタンスを生成できること
     */
    @Test
    @DisplayName("OrderResponse - デフォルトコンストラクタでインスタンスを生成できること")
    void orderResponseNoArgsConstructor() {
        // Given / When
        OrderResponse orderResponse = new OrderResponse();

        // Then
        assertNotNull(orderResponse, "インスタンスが生成されること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: OrderService の CDI プロキシ用コンストラクタが呼び出せること
     */
    @Test
    @DisplayName("OrderService - CDI プロキシ用コンストラクタが呼び出せること")
    void orderServiceCdiProxyConstructor() throws Exception {
        // Given
        java.lang.reflect.Constructor<OrderService> ctor =
                OrderService.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        // When
        OrderService service = ctor.newInstance();

        // Then
        assertNotNull(service, "インスタンスが生成されること");
    }
}
