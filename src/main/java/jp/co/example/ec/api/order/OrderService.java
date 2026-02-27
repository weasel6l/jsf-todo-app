/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.order;

import jp.co.example.ec.OrderBean;
import jp.co.example.ec.api.cart.CartService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * 注文処理を管理するアプリケーションスコープのサービス
 *
 * OrderBean のライフサイクルを管理し、CDI 経由でリソースに提供する
 */
@ApplicationScoped
public class OrderService {

    /**
     * 注文情報を保持する OrderBean インスタンス
     */
    private final OrderBean orderBean;

    /**
     * カートサービス
     */
    private final CartService cartService;

    /**
     * CDI プロキシ用の引数なしコンストラクタ
     */
    protected OrderService() {
        this.orderBean = null;
        this.cartService = null;
    }

    /**
     * コンストラクタインジェクション
     *
     * @param cartService カートサービス
     */
    @Inject
    public OrderService(CartService cartService) {
        this.cartService = cartService;
        this.orderBean = new OrderBean();
    }

    /**
     * カートの内容から注文を作成する
     *
     * カートが空の場合は注文を作成しない
     *
     * @return 注文作成に成功した場合は true
     */
    public boolean createOrder() {
        jp.co.example.ec.CartBean cartBean = cartService.getCartBeanForOrder();
        if (cartBean.isEmpty()) {
            return false;
        }
        orderBean.createOrder(cartBean);
        return true;
    }

    /**
     * 現在の注文情報を OrderResponse として返す
     *
     * @return 注文レスポンス
     */
    public OrderResponse getOrder() {
        return new OrderResponse(
            orderBean.getOrderNumber(),
            orderBean.getOrderTotal(),
            orderBean.getOrderDate(),
            orderBean.getStatus(),
            orderBean.isOrderCompleted()
        );
    }

    /**
     * 注文情報をリセットする
     */
    public void reset() {
        orderBean.reset();
    }
}
