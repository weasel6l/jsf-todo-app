/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.resource;

import com.example.todo.api.dto.TodoDetailResponse;
import com.example.todo.api.dto.TodoDetailUpdateRequest;
import com.example.todo.api.service.TodoDetailService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * Todo 詳細画面に対応する REST API リソースクラス。
 * 詳細取得・更新を提供する。
 */
@Tag(name = "Todo詳細", description = "Todo 詳細画面の API")
@Path("/todo/detail")
@RequestScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TodoDetailResource {

    /**
     * Todo 詳細業務サービス。
     */
    private final TodoDetailService todoDetailService;

    /**
     * 指定した ID の Todo 詳細を返す。
     *
     * @param id 取得対象の Todo ID
     * @return Todo 詳細レスポンス、対象が存在しない場合は 404
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 詳細取得", description = "指定した ID の Todo 詳細情報を返す")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoDetailResponse.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "対象 Todo が存在しない")
    })
    public Response getDetail(
            @Parameter(description = "取得する Todo の ID")
            @PathParam("id") Long id) {
        return todoDetailService.getDetail(id)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    /**
     * 指定した ID の Todo のタイトルと説明を更新する。
     *
     * @param id 更新対象の Todo ID
     * @param request 更新リクエスト
     * @return 更新後の Todo 詳細レスポンス、対象が存在しない場合は 404
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 更新", description = "指定した ID の Todo のタイトルと説明を更新する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoDetailResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "バリデーションエラー"),
        @APIResponse(responseCode = "404", description = "対象 Todo が存在しない")
    })
    public Response updateTodo(
            @Parameter(description = "更新する Todo の ID")
            @PathParam("id") Long id,
            @RequestBody(
                description = "Todo 更新リクエスト",
                required = true,
                content = @Content(schema = @Schema(implementation = TodoDetailUpdateRequest.class))
            )
            @Valid TodoDetailUpdateRequest request) {
        return todoDetailService.updateTodo(id, request)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
