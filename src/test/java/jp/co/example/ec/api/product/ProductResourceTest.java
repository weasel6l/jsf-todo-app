/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.product;

import io.helidon.microprofile.tests.junit5.HelidonTest;
import javax.inject.Inject;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ProductResource の統合テスト
 *
 * GET /api/products の正常系・境界値を検証する
 */
@HelidonTest
@DisplayName("ProductResource")
class ProductResourceTest {

    /**
     * JAX-RS テスト用 WebTarget（Helidon テストフレームワークが注入する）
     */
    @Inject
    WebTarget target;

    /**
     * 商品カタログサービス（isOutOfStock の直接テスト用）
     */
    @Inject
    ProductCatalogService catalogService;

    /**
     * 前提条件: 商品データが初期化済み
     * 期待する事後条件: HTTP 200 が返り、商品リストが5件含まれること
     */
    @Test
    @DisplayName("GET /api/products - 全商品一覧が取得できること")
    void returnsAllProducts() {
        // Given: 初期化済みの商品データ（5件）

        // When
        Response response = target.path("/api/products")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        List<?> products = response.readEntity(new GenericType<List<?>>() {});
        assertEquals(5, products.size(), "商品が5件返ること");
    }

    /**
     * 前提条件: 商品データが初期化済み
     * 期待する事後条件: 各商品に id, name, description, price, stock フィールドが含まれること
     */
    @Test
    @DisplayName("GET /api/products - 各商品が必要なフィールドを持つこと")
    void eachProductHasRequiredFields() {
        // Given: 初期化済みの商品データ

        // When
        Response response = target.path("/api/products")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        List<Map<String, Object>> products = response.readEntity(
                new GenericType<List<Map<String, Object>>>() {}
        );
        assertFalse(products.isEmpty(), "商品リストが空でないこと");
        Map<String, Object> first = products.get(0);
        assertNotNull(first.get("id"), "id フィールドがあること");
        assertNotNull(first.get("name"), "name フィールドがあること");
        assertNotNull(first.get("description"), "description フィールドがあること");
        assertNotNull(first.get("price"), "price フィールドがあること");
        assertNotNull(first.get("stock"), "stock フィールドがあること");
    }

    /**
     * 前提条件: 商品データが初期化済みで全商品 stock > 0
     * 期待する事後条件: 全商品の outOfStock が false であること
     */
    @Test
    @DisplayName("GET /api/products - 全商品の在庫あり状態が正しく返ること")
    void allProductsAreInStock() {
        // Given: 初期データは全商品 stock > 0

        // When
        Response response = target.path("/api/products")
                .request(MediaType.APPLICATION_JSON)
                .get();

        // Then
        assertEquals(200, response.getStatus(), "HTTP 200 が返ること");
        List<Map<String, Object>> products = response.readEntity(
                new GenericType<List<Map<String, Object>>>() {}
        );
        for (Map<String, Object> product : products) {
            assertFalse((Boolean) product.get("outOfStock"),
                    "stock > 0 の商品は outOfStock=false であること");
        }
    }

    /**
     * 前提条件: なし
     * 期待する事後条件: ProductResponse のデフォルトコンストラクタでインスタンスを生成できること
     */
    @Test
    @DisplayName("ProductResponse - デフォルトコンストラクタでインスタンスを生成できること")
    void productResponseNoArgsConstructor() {
        // Given / When
        ProductResponse productResponse = new ProductResponse();

        // Then
        assertNotNull(productResponse, "インスタンスが生成されること");
    }

    /**
     * 事前条件: 存在しない商品 ID（99999）
     * 期待する事後条件: isOutOfStock が true を返すこと（orElse(true) の分岐）
     */
    @Test
    @DisplayName("ProductCatalogService.isOutOfStock - 存在しない商品 ID は在庫切れ扱いになること")
    void isOutOfStockReturnsTrueForNonExistentProduct() {
        // Given: 存在しない商品 ID
        long nonExistentId = 99999L;

        // When
        boolean result = catalogService.isOutOfStock(nonExistentId);

        // Then
        assertTrue(result, "存在しない商品 ID は isOutOfStock=true であること");
    }
}
