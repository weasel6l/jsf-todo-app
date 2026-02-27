/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.cart;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Set;

/**
 * カート画面に対応する REST リソース
 *
 * /api/cart エンドポイントを提供する
 */
@Tag(name = "カート", description = "カート画面の API")
@Path("/cart")
@RequestScoped
public class CartResource {

    /**
     * カートサービス
     */
    private final CartService cartService;

    /**
     * Bean Validation バリデーター
     */
    private final Validator validator;

    /**
     * CDI コンストラクタインジェクション
     *
     * @param cartService カートサービス
     * @param validator Bean Validation バリデーター
     */
    @Inject
    public CartResource(CartService cartService, Validator validator) {
        this.cartService = cartService;
        this.validator = validator;
    }

    /**
     * カートの内容を取得する
     *
     * @return カートレスポンス
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "カート内容取得", description = "現在のカートの内容を返却する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class)
            )
        )
    })
    public CartResponse getCart() {
        return cartService.getCart();
    }

    /**
     * カートに商品を追加する
     *
     * @param request 追加リクエスト
     * @return HTTP 200 またはエラーレスポンス
     */
    @POST
    @Path("/items")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "カートへ商品追加", description = "指定した商品をカートに追加する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "追加成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "バリデーションエラー"),
        @APIResponse(responseCode = "404", description = "商品が見つからない")
    })
    public Response addToCart(
        @RequestBody(
            description = "カートへの追加リクエスト",
            required = true,
            content = @Content(schema = @Schema(implementation = AddToCartRequest.class))
        )
        AddToCartRequest request
    ) {
        Set<ConstraintViolation<AddToCartRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        boolean added = cartService.addToCart(request.getProductId(), request.getQuantity());
        if (!added) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(cartService.getCart()).build();
    }

    /**
     * カートから商品を削除する
     *
     * @param productId 削除する商品 ID
     * @return カートレスポンス
     */
    @DELETE
    @Path("/items/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "カートから商品削除", description = "指定した商品をカートから削除する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "削除成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class)
            )
        )
    })
    public CartResponse removeFromCart(
        @Parameter(description = "削除する商品 ID")
        @PathParam("productId") long productId
    ) {
        cartService.removeFromCart(productId);
        return cartService.getCart();
    }

    /**
     * カートをすべてクリアする
     *
     * @return カートレスポンス
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "カートクリア", description = "カートの全アイテムを削除する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "クリア成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CartResponse.class)
            )
        )
    })
    public CartResponse clearCart() {
        cartService.clear();
        return cartService.getCart();
    }
}
