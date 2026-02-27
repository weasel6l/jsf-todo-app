/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import jp.co.example.ec.CartBean;
import jp.co.example.ec.CartItem;
import jp.co.example.ec.Product;
import jp.co.example.ec.ProductBean;
import jp.co.example.ec.api.product.ProductCatalogService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ショッピングカートを管理するアプリケーションスコープのサービス
 *
 * CartBean および ProductCatalogService のライフサイクルを管理し、
 * CDI 経由でリソースに提供する
 */
@ApplicationScoped
public class CartService {

    /**
     * 商品カタログサービス
     */
    private final ProductCatalogService productCatalogService;

    /**
     * カートの状態を保持する CartBean インスタンス
     */
    private final CartBean cartBean;

    /**
     * CDI プロキシ用の引数なしコンストラクタ
     */
    protected CartService() {
        this.productCatalogService = null;
        this.cartBean = null;
    }

    /**
     * コンストラクタインジェクション
     *
     * @param productCatalogService 商品カタログサービス
     */
    @Inject
    public CartService(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
        ProductBean productBean = new ProductBean();
        this.cartBean = new CartBean();
        this.cartBean.setProductBean(productBean);
    }

    /**
     * 指定した商品をカートに追加する
     *
     * @param productId 追加する商品 ID
     * @param quantity 追加する数量
     * @return 指定した商品が存在しない場合は false
     */
    public boolean addToCart(long productId, int quantity) {
        Optional<Product> product = productCatalogService.findProductById(productId);
        if (!product.isPresent()) {
            return false;
        }
        cartBean.addToCart(productId, quantity);
        return true;
    }

    /**
     * 指定した商品をカートから削除する
     *
     * @param productId 削除する商品 ID
     */
    public void removeFromCart(long productId) {
        cartBean.removeFromCart(productId);
    }

    /**
     * カートをすべてクリアする
     */
    public void clear() {
        cartBean.clear();
    }

    /**
     * カートの内容を CartResponse として返す
     *
     * @return カートレスポンス
     */
    public CartResponse getCart() {
        List<CartItemResponse> items = cartBean.getItems()
                .stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());
        return new CartResponse(
            items,
            cartBean.getTotal(),
            cartBean.getItemCount(),
            cartBean.isEmpty()
        );
    }

    /**
     * CartItem を CartItemResponse に変換する
     *
     * @param item 変換元の CartItem
     * @return CartItemResponse
     */
    private CartItemResponse toItemResponse(CartItem item) {
        return new CartItemResponse(
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getPrice(),
            item.getQuantity(),
            item.getSubtotal()
        );
    }

    /**
     * 注文作成用に内部の CartBean を返す
     *
     * @return 内部 CartBean インスタンス
     */
    public CartBean getCartBeanForOrder() {
        return cartBean;
    }
}
