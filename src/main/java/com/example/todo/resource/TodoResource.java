/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.resource;

import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.UpdateTodoRequest;
import com.example.todo.service.TodoService;
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

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

/**
 * Todo リソースクラス
 */
@Tag(name = "Todo", description = "Todo CRUD API")
@Path("/todos")
@RequestScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED, force = true)
public class TodoResource {

    /**
     * Todo サービス
     */
    private final TodoService service;

    /**
     * Todo 一覧を取得する
     *
     * @return Todo レスポンスのリスト
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 一覧取得", description = "登録されているすべての Todo を返す")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoResponse.class)
            )
        )
    })
    public List<TodoResponse> getAll() {
        return service.findAll();
    }

    /**
     * 新しい Todo を追加する
     *
     * @param request 追加リクエスト
     * @return 201 Created と追加した Todo
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 追加", description = "新しい Todo を追加する")
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "追加成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "バリデーションエラー")
    })
    public Response create(
            @RequestBody(
                description = "Todo 追加リクエスト",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateTodoRequest.class))
            )
            @Valid CreateTodoRequest request) {
        TodoResponse response = service.create(request);
        URI location = URI.create("/api/todos/" + response.getId());
        return Response.created(location).entity(response).build();
    }

    /**
     * ID で Todo を取得する
     *
     * @param id 取得対象の ID
     * @return Todo レスポンス
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 詳細取得", description = "ID で Todo を取得する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoResponse.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "対象が存在しない")
    })
    public TodoResponse getById(
            @Parameter(description = "Todo の ID")
            @PathParam("id") Long id) {
        return service.findById(id);
    }

    /**
     * Todo を更新する
     *
     * @param id      更新対象の ID
     * @param request 更新リクエスト
     * @return 更新後の Todo レスポンス
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 更新", description = "ID で Todo を更新する")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "バリデーションエラー"),
        @APIResponse(responseCode = "404", description = "対象が存在しない")
    })
    public TodoResponse update(
            @Parameter(description = "Todo の ID")
            @PathParam("id") Long id,
            @RequestBody(
                description = "Todo 更新リクエスト",
                required = true,
                content = @Content(schema = @Schema(implementation = UpdateTodoRequest.class))
            )
            @Valid UpdateTodoRequest request) {
        return service.update(id, request);
    }

    /**
     * Todo を削除する
     *
     * @param id 削除対象の ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Todo 削除", description = "ID で Todo を削除する")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "削除成功"),
        @APIResponse(responseCode = "404", description = "対象が存在しない")
    })
    public Response delete(
            @Parameter(description = "Todo の ID")
            @PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }

    /**
     * Todo の完了/未完了を切り替える
     *
     * @param id 対象の ID
     * @return トグル後の Todo レスポンス
     */
    @PATCH
    @Path("/{id}/toggle")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "完了トグル", description = "Todo の完了/未完了を切り替える")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "トグル成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoResponse.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "対象が存在しない")
    })
    public TodoResponse toggle(
            @Parameter(description = "Todo の ID")
            @PathParam("id") Long id) {
        return service.toggle(id);
    }
}
