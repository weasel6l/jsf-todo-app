/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.resource;

import com.example.todo.api.dto.TodoAddRequest;
import com.example.todo.api.dto.TodoAddResponse;
import com.example.todo.api.dto.TodoListResponse;
import com.example.todo.api.dto.TodoToggleResponse;
import com.example.todo.api.service.TodoListService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
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
 * Todo 一覧画面に対応する REST API リソースクラス。
 * 一覧取得・追加・削除・完了トグルを提供する。
 */
@Tag(name = "Todo一覧", description = "Todo 一覧画面の API")
@Path("/todo/list")
@RequestScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TodoListResource {

    /**
     * Todo 一覧業務サービス。
     */
    private final TodoListService todoListService;

    /**
     * すべての Todo を統計情報と合わせて返す。
     *
     * @return Todo 一覧レスポンス
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Todo 一覧取得", description = "登録されているすべての Todo を一覧で返す")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoListResponse.class)
            )
        )
    })
    public Response getList() {
        return Response.ok(todoListService.getList()).build();
    }

    /**
     * 新しい Todo を追加する。
     *
     * @param request 追加リクエスト
     * @return 追加した Todo のレスポンス
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
                schema = @Schema(implementation = TodoAddResponse.class)
            )
        ),
        @APIResponse(responseCode = "400", description = "バリデーションエラー")
    })
    public Response addTodo(
            @RequestBody(
                description = "Todo 追加リクエスト",
                required = true,
                content = @Content(schema = @Schema(implementation = TodoAddRequest.class))
            )
            @Valid TodoAddRequest request) {
        TodoAddResponse addResponse = todoListService.addTodo(request);
        return Response.status(Response.Status.CREATED).entity(addResponse).build();
    }

    /**
     * 指定した ID の Todo を削除する。
     *
     * @param id 削除対象の Todo ID
     * @return 削除成功時は 204、対象が存在しない場合は 404
     */
    @DELETE
    @Path("/{id}")
    @Operation(summary = "Todo 削除", description = "指定した ID の Todo を削除する")
    @APIResponses({
        @APIResponse(responseCode = "204", description = "削除成功"),
        @APIResponse(responseCode = "404", description = "対象 Todo が存在しない")
    })
    public Response deleteTodo(
            @Parameter(description = "削除する Todo の ID")
            @PathParam("id") Long id) {
        if (todoListService.deleteTodo(id)) {
            return Response.noContent().build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * 指定した Todo の完了/未完了を切り替える。
     *
     * @param id トグル対象の Todo ID
     * @return 更新後の完了状態レスポンス、対象が存在しない場合は 404
     */
    @PATCH
    @Path("/{id}/toggle")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "完了状態トグル", description = "指定した Todo の完了/未完了を切り替える")
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "更新成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoToggleResponse.class)
            )
        ),
        @APIResponse(responseCode = "404", description = "対象 Todo が存在しない")
    })
    public Response toggleComplete(
            @Parameter(description = "対象 Todo の ID")
            @PathParam("id") Long id) {
        return todoListService.toggleComplete(id)
                .map(r -> Response.ok(r).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }
}
