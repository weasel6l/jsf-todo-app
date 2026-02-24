/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.resource;

import com.example.todo.api.dto.TodoDetailResponse;
import com.example.todo.api.dto.TodoDetailUpdateRequest;
import com.example.todo.api.service.TodoDetailService;
import com.example.todo.api.service.TodoStore;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * TodoDetailResource のテストクラス。
 * 初期状態に 3 件のサンプルデータが存在することを前提とする。
 */
@DisplayName("TodoDetailResource のテスト")
class TodoDetailResourceTest {

    /**
     * テスト対象の TodoStore。
     */
    private TodoStore todoStore;

    /**
     * テスト対象の TodoDetailService。
     */
    private TodoDetailService todoDetailService;

    /**
     * テスト対象の TodoDetailResource。
     */
    private TodoDetailResource todoDetailResource;

    /**
     * 各テスト前に実オブジェクトを生成する。
     */
    @BeforeEach
    void setUp() {
        todoStore = new TodoStore();
        todoDetailService = new TodoDetailService(todoStore);
        todoDetailResource = new TodoDetailResource(todoDetailService);
    }

    /**
     * GET /api/todo/detail/{id} のテストグループ。
     */
    @Nested
    @DisplayName("GET /api/todo/detail/{id}")
    class GetDetail {

        /**
         * 存在する ID を指定すると 200 と Todo 詳細が返ること。
         */
        @Test
        @DisplayName("存在する ID を指定すると 200 と詳細情報が返る")
        void returnsDetailForExistingId() {
            // When
            Response response = todoDetailResource.getDetail(1L);

            // Then
            assertEquals(200, response.getStatus(), "200 OK が返ること");
            TodoDetailResponse body = (TodoDetailResponse) response.getEntity();
            assertEquals(1L, body.getId(), "ID が一致すること");
            assertEquals("買い物をする", body.getTitle(), "タイトルが一致すること");
            assertNotNull(body.getFormattedCreatedAt(), "作成日時が返ること");
        }

        /**
         * 存在しない ID を指定すると 404 が返ること。
         */
        @Test
        @DisplayName("存在しない ID を指定すると 404 が返る")
        void returnsNotFoundForNonExistentId() {
            // When
            Response response = todoDetailResource.getDetail(9999L);

            // Then
            assertEquals(404, response.getStatus(), "404 Not Found が返ること");
        }

        /**
         * 完了済みの Todo 詳細を取得すると completed=true が返ること。
         */
        @Test
        @DisplayName("完了済みの Todo を取得すると completed=true が返る")
        void returnsCompletedTodo() {
            // Given
            todoStore.findById(1L).ifPresent(item -> {
                item.setCompleted(true);
                todoStore.save(item);
            });

            // When
            Response response = todoDetailResource.getDetail(1L);

            // Then
            TodoDetailResponse body = (TodoDetailResponse) response.getEntity();
            assertEquals(true, body.isCompleted(), "完了フラグが true であること");
        }
    }

    /**
     * PUT /api/todo/detail/{id} のテストグループ。
     */
    @Nested
    @DisplayName("PUT /api/todo/detail/{id}")
    class UpdateTodo {

        /**
         * タイトルと説明を更新できること。
         */
        @Test
        @DisplayName("タイトルと説明を更新すると 200 と更新後の内容が返る")
        void updatesTitleAndDescription() {
            // Given
            TodoDetailUpdateRequest request = new TodoDetailUpdateRequest();
            request.setTitle("新しいタイトル");
            request.setDescription("新しい説明");

            // When
            Response response = todoDetailResource.updateTodo(1L, request);

            // Then
            assertEquals(200, response.getStatus(), "200 OK が返ること");
            TodoDetailResponse body = (TodoDetailResponse) response.getEntity();
            assertEquals("新しいタイトル", body.getTitle(), "タイトルが更新されること");
            assertEquals("新しい説明", body.getDescription(), "説明が更新されること");
        }

        /**
         * description が null の場合は空文字に変換されること。
         */
        @Test
        @DisplayName("description が null の場合は空文字に変換される")
        void convertsNullDescriptionToEmpty() {
            // Given
            TodoDetailUpdateRequest request = new TodoDetailUpdateRequest();
            request.setTitle("新しいタイトル");
            request.setDescription(null);

            // When
            Response response = todoDetailResource.updateTodo(1L, request);

            // Then
            TodoDetailResponse body = (TodoDetailResponse) response.getEntity();
            assertEquals("", body.getDescription(), "説明が空文字になること");
        }

        /**
         * 存在しない ID を指定すると 404 が返ること。
         */
        @Test
        @DisplayName("存在しない ID を指定すると 404 が返る")
        void returnsNotFoundForNonExistentId() {
            // Given
            TodoDetailUpdateRequest request = new TodoDetailUpdateRequest();
            request.setTitle("新タイトル");

            // When
            Response response = todoDetailResource.updateTodo(9999L, request);

            // Then
            assertEquals(404, response.getStatus(), "404 Not Found が返ること");
        }
    }
}
