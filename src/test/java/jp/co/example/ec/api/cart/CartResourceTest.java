/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import io.helidon.microprofile.tests.junit5.HelidonTest;
import javax.inject.Inject;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * CartResource の統合テスト
 *
 * /api/cart エンドポイントの正常系・異常系・境界値を検証する
 */
@HelidonTest
@DisplayName("CartResource")
class CartResourceTest {

    /**
     * JAX-RS テスト用 WebTarget（Helidon テストフレームワークが注入する）
     */
    @Inject
    WebTarget target;

    /**
     * カートサービス（テスト間の状態リセット用）
     */
    @Inject
    CartService cartService;

    /**
     * 各テスト前にカートをクリアしてテスト間の状態干渉を防ぐ
     */
    @BeforeEach
    void setUp() {
        cartService.clear();
    }

    /**
     * 前提条件: カートが空の状態
     * 期待する事後条件: HTTP 200 が返り、isEmpty=true・items=空リストであること
     */
    @Test
    @DisplayName("GET /api/cart - 空のカートが正しく取得できること")
    void getEmptyCart() {
        // Given: カートが空

        // When
        Response response = target.path("/api/cart")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> cart = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertTrue((Boolean) cart.get("empty"), "カートが空であること");
        assertEquals(0, ((List<?>) cart.get("items")).size(), "アイテムが0件であること");
    }

    /**
     * 前提条件: 商品 id=1 が存在する
     * 期待する事後条件: HTTP 200 が返り、カートに商品が1件追加されること
     */
    @Test
    @DisplayName("POST /api/cart/items - 存在する商品をカートに追加できること")
    void addExistingProductToCart() {
        // Given: 商品 id=1 と数量 1

        // When
        Map<String, Object> body = Map.of("productId", 1L, "quantity", 1);
        Response response = target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> cart = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertFalse((Boolean) cart.get("empty"), "カートが空でないこと");
        assertEquals(1, ((List<?>) cart.get("items")).size(), "アイテムが1件であること");
    }

    /**
     * 前提条件: 存在しない商品 id=999
     * 期待する事後条件: HTTP 404 が返ること
     */
    @Test
    @DisplayName("POST /api/cart/items - 存在しない商品は 404 を返すこと")
    void addNonExistentProductReturns404() {
        // Given: 存在しない商品 id=999

        // When
        Map<String, Object> body = Map.of("productId", 999L, "quantity", 1);
        Response response = target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        // Then
        assertEquals(404, response.getStatus(), "HTTP 404 が返ること");
    }

    /**
     * 前提条件: quantity=0 の不正リクエスト
     * 期待する事後条件: HTTP 400 が返ること
     */
    @Test
    @DisplayName("POST /api/cart/items - quantity=0 は 400 を返すこと")
    void addWithZeroQuantityReturns400() {
        // Given: quantity=0 の不正リクエスト

        // When
        Map<String, Object> body = Map.of("productId", 1L, "quantity", 0);
        Response response = target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        // Then
        assertEquals(400, response.getStatus(), "HTTP 400 が返ること");
    }

    /**
     * 前提条件: カートに商品 id=1 が1件追加済み
     * 期待する事後条件: 同一商品を再追加すると数量が加算されること
     */
    @Test
    @DisplayName("POST /api/cart/items - 同一商品を追加すると数量が加算されること")
    void addSameProductAccumulatesQuantity() {
        // Given: 商品 id=1 を1件追加済み
        Map<String, Object> firstBody = Map.of("productId", 1L, "quantity", 1);
        target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(firstBody));

        // When: 同一商品をもう1件追加
        Map<String, Object> secondBody = Map.of("productId", 1L, "quantity", 2);
        Response response = target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(secondBody));

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> cart = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertEquals(3, ((Number) cart.get("itemCount")).intValue(), "数量が3に加算されること");
    }

    /**
     * 前提条件: カートに商品 id=1 が追加済み
     * 期待する事後条件: DELETE /api/cart/items/1 でアイテムが削除されること
     */
    @Test
    @DisplayName("DELETE /api/cart/items/{productId} - カートからアイテムを削除できること")
    void removeItemFromCart() {
        // Given: 商品 id=1 をカートに追加
        Map<String, Object> body = Map.of("productId", 1L, "quantity", 1);
        target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        // When
        Response response = target.path("/api/cart/items/1")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> cart = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertTrue((Boolean) cart.get("empty"), "カートが空であること");
    }

    /**
     * 前提条件: カートに商品が1件以上存在する
     * 期待する事後条件: DELETE /api/cart でカートが空になること
     */
    @Test
    @DisplayName("DELETE /api/cart - カートをクリアできること")
    void clearCart() {
        // Given: 商品 id=1 をカートに追加
        Map<String, Object> body = Map.of("productId", 1L, "quantity", 1);
        target.path("/api/cart/items")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(body));

        // When
        Response response = target.path("/api/cart")
                .request(MediaType.APPLICATION_JSON)
                .delete();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        Map<String, Object> cart = response.readEntity(new GenericType<Map<String, Object>>() {});
        assertTrue((Boolean) cart.get("empty"), "カートが空であること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: CartResponse のデフォルトコンストラクタでインスタンスを生成できること
     */
    @Test
    @DisplayName("CartResponse - デフォルトコンストラクタでインスタンスを生成できること")
    void cartResponseNoArgsConstructor() {
        // Given / When
        CartResponse cartResponse = new CartResponse();

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(cartResponse, "インスタンスが生成されること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: CartItemResponse のデフォルトコンストラクタでインスタンスを生成できること
     */
    @Test
    @DisplayName("CartItemResponse - デフォルトコンストラクタでインスタンスを生成できること")
    void cartItemResponseNoArgsConstructor() {
        // Given / When
        CartItemResponse cartItemResponse = new CartItemResponse();

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(cartItemResponse, "インスタンスが生成されること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: AddToCartRequest のデフォルトコンストラクタでインスタンスを生成できること
     */
    @Test
    @DisplayName("AddToCartRequest - デフォルトコンストラクタでインスタンスを生成できること")
    void addToCartRequestNoArgsConstructor() {
        // Given / When
        AddToCartRequest request = new AddToCartRequest();

        // Then
        org.junit.jupiter.api.Assertions.assertNull(request.getProductId(), "productId が null であること");
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: CartService の CDI プロキシ用コンストラクタが呼び出せること
     */
    @Test
    @DisplayName("CartService - CDI プロキシ用コンストラクタが呼び出せること")
    void cartServiceCdiProxyConstructor() throws Exception {
        // Given
        java.lang.reflect.Constructor<CartService> ctor =
                CartService.class.getDeclaredConstructor();
        ctor.setAccessible(true);

        // When
        CartService service = ctor.newInstance();

        // Then
        org.junit.jupiter.api.Assertions.assertNotNull(service, "インスタンスが生成されること");
    }
}
