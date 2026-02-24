/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.resource;

import com.example.todo.api.TodoApplication;
import com.example.todo.api.dto.TodoAddRequest;
import com.example.todo.api.dto.TodoAddResponse;
import com.example.todo.api.dto.TodoListResponse;
import com.example.todo.api.dto.TodoToggleResponse;
import com.example.todo.api.service.TodoListService;
import com.example.todo.api.service.TodoStore;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TodoListResource のテストクラス。
 * 初期状態に 3 件のサンプルデータが存在することを前提とする。
 * TodoApplication のインスタンス化検証も含む。
 */
@DisplayName("TodoListResource のテスト")
class TodoListResourceTest {

    /**
     * テスト対象の TodoStore（共有インメモリストア）。
     */
    private TodoStore todoStore;

    /**
     * テスト対象の TodoListService。
     */
    private TodoListService todoListService;

    /**
     * テスト対象の TodoListResource。
     */
    private TodoListResource todoListResource;

    /**
     * 各テスト前に実オブジェクトを生成する。
     */
    @BeforeEach
    void setUp() {
        todoStore = new TodoStore();
        todoListService = new TodoListService(todoStore);
        todoListResource = new TodoListResource(todoListService);
    }

    /**
     * GET /api/todo/list のテストグループ。
     */
    @Nested
    @DisplayName("GET /api/todo/list")
    class GetList {

        /**
         * 初期状態（3 件）で取得できること。
         */
        @Test
        @DisplayName("初期状態で一覧を取得すると 3 件返る")
        void returnsInitialList() {
            // Given - TodoStore は初期化時に 3 件のサンプルデータを保持する

            // When
            Response response = todoListResource.getList();

            // Then
            assertEquals(200, response.getStatus(), "200 OK が返ること");
            TodoListResponse body = (TodoListResponse) response.getEntity();
            assertEquals(3, body.getItems().size(), "3 件あること");
            assertEquals(3, body.getTotalCount(), "合計 3 件");
            assertEquals(0, body.getCompletedCount(), "完了 0 件");
            assertEquals(3, body.getPendingCount(), "未完了 3 件");
        }

        /**
         * 完了済みが 1 件のとき、カウントが正しく返ること。
         */
        @Test
        @DisplayName("完了済みが 1 件ある場合は completedCount=1 が返る")
        void returnsCorrectCountsWhenOneIsCompleted() {
            // Given
            todoStore.findById(1L).ifPresent(item -> {
                item.setCompleted(true);
                todoStore.save(item);
            });

            // When
            Response response = todoListResource.getList();

            // Then
            TodoListResponse body = (TodoListResponse) response.getEntity();
            assertEquals(1, body.getCompletedCount(), "完了 1 件");
            assertEquals(2, body.getPendingCount(), "未完了 2 件");
        }
    }

    /**
     * POST /api/todo/list のテストグループ。
     */
    @Nested
    @DisplayName("POST /api/todo/list")
    class AddTodo {

        /**
         * タイトルのみで追加できること。
         */
        @Test
        @DisplayName("タイトルのみで追加すると 201 と採番された ID が返る")
        void addsTodoWithTitleOnly() {
            // Given
            TodoAddRequest request = new TodoAddRequest();
            request.setTitle("買い物");

            // When
            Response response = todoListResource.addTodo(request);

            // Then
            assertEquals(201, response.getStatus(), "201 Created が返ること");
            TodoAddResponse body = (TodoAddResponse) response.getEntity();
            assertNotNull(body.getId(), "ID が採番されること");
            assertEquals("買い物", body.getTitle(), "タイトルが一致すること");
            assertFalse(body.isCompleted(), "完了フラグが false であること");
        }

        /**
         * タイトルと説明を指定して追加できること。
         */
        @Test
        @DisplayName("タイトルと説明で追加すると description が返る")
        void addsTodoWithTitleAndDescription() {
            // Given
            TodoAddRequest request = new TodoAddRequest();
            request.setTitle("買い物");
            request.setDescription("スーパーで");

            // When
            Response response = todoListResource.addTodo(request);

            // Then
            assertEquals(201, response.getStatus(), "201 Created が返ること");
            TodoAddResponse body = (TodoAddResponse) response.getEntity();
            assertEquals("スーパーで", body.getDescription(), "説明が一致すること");
        }

        /**
         * description が null の場合は空文字に変換されること。
         */
        @Test
        @DisplayName("description が null の場合は空文字に変換される")
        void convertsNullDescriptionToEmpty() {
            // Given
            TodoAddRequest request = new TodoAddRequest();
            request.setTitle("買い物");
            request.setDescription(null);

            // When
            Response response = todoListResource.addTodo(request);

            // Then
            TodoAddResponse body = (TodoAddResponse) response.getEntity();
            assertEquals("", body.getDescription(), "説明が空文字になること");
        }
    }

    /**
     * DELETE /api/todo/list/{id} のテストグループ。
     */
    @Nested
    @DisplayName("DELETE /api/todo/list/{id}")
    class DeleteTodo {

        /**
         * 存在する ID を削除すると 204 が返ること。
         */
        @Test
        @DisplayName("存在する ID を指定すると 204 が返る")
        void deletesExistingTodo() {
            // When
            Response response = todoListResource.deleteTodo(1L);

            // Then
            assertEquals(204, response.getStatus(), "204 No Content が返ること");
        }

        /**
         * 存在しない ID を指定すると 404 が返ること。
         */
        @Test
        @DisplayName("存在しない ID を指定すると 404 が返る")
        void returnsNotFoundForNonExistentId() {
            // When
            Response response = todoListResource.deleteTodo(9999L);

            // Then
            assertEquals(404, response.getStatus(), "404 Not Found が返ること");
        }
    }

    /**
     * PATCH /api/todo/list/{id}/toggle のテストグループ。
     */
    @Nested
    @DisplayName("PATCH /api/todo/list/{id}/toggle")
    class ToggleComplete {

        /**
         * 未完了の Todo をトグルすると completed=true になること。
         */
        @Test
        @DisplayName("未完了の Todo をトグルすると completed=true になる")
        void togglesIncompleteToComplete() {
            // Given - id=1 は初期状態で未完了

            // When
            Response response = todoListResource.toggleComplete(1L);

            // Then
            assertEquals(200, response.getStatus(), "200 OK が返ること");
            TodoToggleResponse body = (TodoToggleResponse) response.getEntity();
            assertTrue(body.isCompleted(), "完了フラグが true になること");
        }

        /**
         * 完了済みの Todo をトグルすると completed=false になること。
         */
        @Test
        @DisplayName("完了済みの Todo をトグルすると completed=false になる")
        void togglesCompleteToIncomplete() {
            // Given
            todoStore.findById(1L).ifPresent(item -> {
                item.setCompleted(true);
                todoStore.save(item);
            });

            // When
            Response response = todoListResource.toggleComplete(1L);

            // Then
            TodoToggleResponse body = (TodoToggleResponse) response.getEntity();
            assertFalse(body.isCompleted(), "完了フラグが false になること");
        }

        /**
         * 存在しない ID をトグルすると 404 が返ること。
         */
        @Test
        @DisplayName("存在しない ID をトグルすると 404 が返る")
        void returnsNotFoundForNonExistentId() {
            // When
            Response response = todoListResource.toggleComplete(9999L);

            // Then
            assertEquals(404, response.getStatus(), "404 が返ること");
        }
    }

    /**
     * TodoApplication のインスタンス化テストグループ。
     */
    @Nested
    @DisplayName("TodoApplication のインスタンス化")
    class TodoApplicationInstantiationTest {

        /**
         * TodoApplication のインスタンスが生成できること。
         */
        @Test
        @DisplayName("TodoApplication のインスタンスを生成できる")
        void canInstantiateTodoApplication() {
            // When
            TodoApplication app = new TodoApplication();

            // Then
            assertNotNull(app, "TodoApplication インスタンスが生成できること");
        }
    }
}
