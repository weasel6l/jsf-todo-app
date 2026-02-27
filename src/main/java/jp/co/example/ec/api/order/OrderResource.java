/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.order;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * 注文処理および注文結果画面に対応する REST リソース
 *
 * /api/orders エンドポイントを提供する
 */
@Tag(name = "注文", description = "注文処理および注文結果画面の API")
@Path("/orders")
@RequestScoped
public class OrderResource {

    /**
     * 注文サービス
     */
    private final OrderService orderService;

    /**
     * CDI コンストラクタインジェクション
     *
     * @param orderService 注文サービス
     */
    @Inject
    public OrderResource(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * カートの内容で注文を作成する
     *
     * @return HTTP 200 またはエラーレスポンス
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "注文作成",
        description = "現在のカートの内容で注文を作成する"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "注文作成成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "カートが空のため注文不可")
    })
    public Response createOrder() {
        boolean created = orderService.createOrder();
        if (!created) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        return Response.ok(orderService.getOrder()).build();
    }

    /**
     * 現在の注文情報を返す
     *
     * @return 注文レスポンス
     */
    @GET
    @Path("/current")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "現在の注文情報取得",
        description = "最後に作成した注文の情報を返却する"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        )
    })
    public OrderResponse getCurrentOrder() {
        return orderService.getOrder();
    }
}
