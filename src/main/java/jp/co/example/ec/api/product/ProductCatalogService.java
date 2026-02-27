/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.product;

import jp.co.example.ec.Product;
import jp.co.example.ec.ProductBean;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

/**
 * 商品カタログを管理するアプリケーションスコープのサービス
 *
 * ProductBean のライフサイクルを管理し、CDI 経由でリソースに提供する
 */
@ApplicationScoped
public class ProductCatalogService {

    /**
     * 商品データを保持する ProductBean インスタンス
     */
    private final ProductBean productBean;

    /**
     * CDI プロキシ用の引数なしコンストラクタ
     */
    public ProductCatalogService() {
        this.productBean = new ProductBean();
    }

    /**
     * すべての商品を返す
     *
     * @return 全商品リスト
     */
    public java.util.List<Product> getAllProducts() {
        return productBean.getAllProducts();
    }

    /**
     * 指定した ID の商品を返す
     *
     * @param id 商品 ID
     * @return 商品（存在しない場合は空）
     */
    public Optional<Product> findProductById(long id) {
        return productBean.findProductById(id);
    }

    /**
     * 指定した商品 ID が在庫切れかどうかを返す
     *
     * @param productId 商品 ID
     * @return 在庫切れであれば true
     */
    public boolean isOutOfStock(long productId) {
        return productBean.isOutOfStock(productId);
    }
}
