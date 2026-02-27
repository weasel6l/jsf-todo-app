/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package jp.co.example.ec.api.product;

import jp.co.example.ec.Product;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品一覧画面に対応する REST リソース
 *
 * /api/products エンドポイントを提供する
 */
@Tag(name = "商品一覧", description = "商品一覧画面の API")
@Path("/products")
@RequestScoped
public class ProductResource {

    /**
     * 商品カタログサービス
     */
    private final ProductCatalogService productCatalogService;

    /**
     * CDI コンストラクタインジェクション
     *
     * @param productCatalogService 商品カタログサービス
     */
    @Inject
    public ProductResource(ProductCatalogService productCatalogService) {
        this.productCatalogService = productCatalogService;
    }

    /**
     * 全商品一覧を取得する
     *
     * @return 全商品のレスポンスリスト
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "全商品一覧取得",
        description = "登録されているすべての商品を一覧で返却する"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        )
    })
    public List<ProductResponse> getAllProducts() {
        return productCatalogService.getAllProducts()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Product エンティティを ProductResponse に変換する
     *
     * @param product 変換元の Product エンティティ
     * @return ProductResponse
     */
    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            productCatalogService.isOutOfStock(product.getId())
        );
    }
}
