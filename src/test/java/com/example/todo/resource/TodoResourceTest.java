/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.resource;

import com.example.todo.repository.TodoRepository;
import io.helidon.microprofile.tests.junit5.HelidonTest;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TodoResource の結合テストクラス
 *
 * 前提条件: Helidon MP 組み込みサーバーが起動していること
 * 事後条件: 各テストは独立して実行でき、テスト間でデータが汚染されないこと
 */
@HelidonTest
@DisplayName("TodoResource")
class TodoResourceTest {

    /**
     * JAX-RS WebTarget（Helidon MP テストサーバーを指す）
     */
    @Inject
    WebTarget target;

    /**
     * PATCH メソッド対応 WebTarget（HttpUrlConnectorProvider ワークアラウンド使用）
     */
    private WebTarget patchTarget;

    /**
     * Todo リポジトリ（テスト前のリセットに使用）
     */
    @Inject
    TodoRepository repository;

    /**
     * 各テスト前にリポジトリをリセットし、PATCH 用クライアントを初期化する
     */
    @BeforeEach
    void setUp() {
        repository.clear();
        Client patchClient = ClientBuilder.newBuilder()
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .build();
        patchTarget = patchClient.target(target.getUri());
    }

    // ── GET /api/todos ──

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 200 OK と空配列が返る
     */
    @Test
    @DisplayName("GET /api/todos - Todo が 0 件の場合は空配列を返す")
    void getAll_returnsEmptyArrayWhenNoTodos() {
        // Given: setUp でリポジトリはクリア済み

        // When
        Response response = target.path("/api/todos").request().get();

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonArray todos = response.readEntity(JsonArray.class);
        assertTrue(todos.isEmpty(), "空配列が返ること");
    }

    /**
     * 前提条件: Todo が 2 件存在する
     * 事後条件: 200 OK と 2 件のリストが返る
     */
    @Test
    @DisplayName("GET /api/todos - Todo が複数件存在する場合はすべて返す")
    void getAll_returnsAllTodosWhenMultipleExist() {
        // Given
        postTodo("買い物をする", "スーパーで購入");
        postTodo("運動する", "ジョギング");

        // When
        Response response = target.path("/api/todos").request().get();

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonArray todos = response.readEntity(JsonArray.class);
        assertEquals(2, todos.size(), "2 件返ること");
    }

    /**
     * 前提条件: Todo が 1 件存在する
     * 事後条件: レスポンスの全フィールドが含まれる
     */
    @Test
    @DisplayName("GET /api/todos - レスポンスの全フィールドが含まれること")
    void getAll_responseContainsAllFields() {
        // Given
        postTodo("買い物をする", "スーパーで食材を購入する");

        // When
        Response response = target.path("/api/todos").request().get();
        JsonArray todos = response.readEntity(JsonArray.class);
        JsonObject todo = todos.getJsonObject(0);

        // Then
        assertNotNull(todo.get("id"), "id が含まれること");
        assertEquals("買い物をする", todo.getString("title"), "title が一致すること");
        assertEquals("スーパーで食材を購入する", todo.getString("description"), "description が一致すること");
        assertFalse(todo.getBoolean("completed"), "completed が false であること");
        assertNotNull(todo.get("createdAt"), "createdAt が含まれること");
    }

    // ── POST /api/todos ──

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 201 Created と作成した Todo が返る
     */
    @Test
    @DisplayName("POST /api/todos - タイトルと説明を指定して追加できる")
    void create_createsTodoWithTitleAndDescription() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .add("title", "買い物をする")
                .add("description", "スーパーで食材を購入する")
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(201, response.getStatus(), "201 Created が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertEquals("買い物をする", todo.getString("title"), "title が一致すること");
        assertEquals("スーパーで食材を購入する", todo.getString("description"), "description が一致すること");
        assertFalse(todo.getBoolean("completed"), "completed が false であること");
        assertNotNull(todo.get("id"), "id が含まれること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 説明なし（空文字）で 201 が返る
     */
    @Test
    @DisplayName("POST /api/todos - 説明なし（空文字）で追加できる")
    void create_createsTodoWithEmptyDescription() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .add("title", "運動する")
                .add("description", "")
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(201, response.getStatus(), "201 Created が返ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: タイトルが null のとき 400 が返る
     */
    @Test
    @DisplayName("POST /api/todos - タイトルが null の場合は 400 を返す")
    void create_returns400WhenTitleIsNull() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .addNull("title")
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: タイトルが空文字のとき 400 が返る
     */
    @Test
    @DisplayName("POST /api/todos - タイトルが空文字の場合は 400 を返す")
    void create_returns400WhenTitleIsEmpty() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .add("title", "")
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: タイトルがスペースのみのとき 400 が返る
     */
    @Test
    @DisplayName("POST /api/todos - タイトルがスペースのみの場合は 400 を返す")
    void create_returns400WhenTitleIsBlank() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .add("title", "   ")
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: タイトルが 100 文字で 201 が返る（境界値）
     */
    @Test
    @DisplayName("POST /api/todos - タイトルが 100 文字のとき追加できる")
    void create_createsTodoWith100CharTitle() {
        // Given
        String title100 = "あ".repeat(100);
        JsonObject request = Json.createObjectBuilder()
                .add("title", title100)
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(201, response.getStatus(), "201 Created が返ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: タイトルが 101 文字のとき 400 が返る（境界値）
     */
    @Test
    @DisplayName("POST /api/todos - タイトルが 101 文字のとき 400 を返す")
    void create_returns400WhenTitleExceeds100Chars() {
        // Given
        String title101 = "あ".repeat(101);
        JsonObject request = Json.createObjectBuilder()
                .add("title", title101)
                .build();

        // When
        Response response = target.path("/api/todos").request()
                .post(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    // ── GET /api/todos/{id} ──

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: 200 OK と対象 Todo が返る
     */
    @Test
    @DisplayName("GET /api/todos/{id} - 存在する id の詳細を取得できる")
    void getById_returnsTodoById() {
        // Given
        JsonObject created = postTodoAndGetBody("買い物をする", "スーパー");
        long id = created.getJsonNumber("id").longValue();

        // When
        Response response = target.path("/api/todos/" + id).request().get();

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertEquals("買い物をする", todo.getString("title"), "title が一致すること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 404 Not Found が返る
     */
    @Test
    @DisplayName("GET /api/todos/{id} - 存在しない id を指定すると 404 を返す")
    void getById_returns404ForUnknownId() {
        // Given: setUp でリポジトリはクリア済み

        // When
        Response response = target.path("/api/todos/999").request().get();

        // Then
        assertEquals(404, response.getStatus(), "404 Not Found が返ること");
    }

    // ── PUT /api/todos/{id} ──

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: 200 OK と更新後の Todo が返る
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - タイトルと説明を更新できる")
    void update_updatesTodoTitleAndDescription() {
        // Given
        JsonObject created = postTodoAndGetBody("旧タイトル", "旧説明");
        long id = created.getJsonNumber("id").longValue();
        JsonObject request = Json.createObjectBuilder()
                .add("title", "新タイトル")
                .add("description", "新説明")
                .build();

        // When
        Response response = target.path("/api/todos/" + id).request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertEquals("新タイトル", todo.getString("title"), "title が更新されること");
        assertEquals("新説明", todo.getString("description"), "description が更新されること");
    }

    /**
     * 前提条件: id=1 の Todo が completed=true で存在する
     * 事後条件: 更新しても completed フラグは変わらない
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - 更新しても completed フラグは変わらない")
    void update_doesNotChangeCompletedFlag() {
        // Given
        JsonObject created = postTodoAndGetBody("タイトル", "説明");
        long id = created.getJsonNumber("id").longValue();
        patchTarget.path("/api/todos/" + id + "/toggle").request()
                .method("PATCH", Entity.json(""));
        JsonObject request = Json.createObjectBuilder()
                .add("title", "更新タイトル")
                .build();

        // When
        Response response = target.path("/api/todos/" + id).request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertTrue(todo.getBoolean("completed"), "completed が true のままであること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 404 Not Found が返る
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - 存在しない id を更新しようとすると 404 を返す")
    void update_returns404ForUnknownId() {
        // Given
        JsonObject request = Json.createObjectBuilder()
                .add("title", "test")
                .build();

        // When
        Response response = target.path("/api/todos/999").request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(404, response.getStatus(), "404 Not Found が返ること");
    }

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: タイトルが空のとき 400 が返る
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - タイトルが空文字の場合は 400 を返す")
    void update_returns400WhenTitleIsEmpty() {
        // Given
        JsonObject created = postTodoAndGetBody("タイトル", "");
        long id = created.getJsonNumber("id").longValue();
        JsonObject request = Json.createObjectBuilder()
                .add("title", "")
                .build();

        // When
        Response response = target.path("/api/todos/" + id).request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: タイトルが 100 文字で 200 が返る（境界値）
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - タイトルが 100 文字のとき更新できる")
    void update_updatesTodoWith100CharTitle() {
        // Given
        JsonObject created = postTodoAndGetBody("タイトル", "");
        long id = created.getJsonNumber("id").longValue();
        String title100 = "あ".repeat(100);
        JsonObject request = Json.createObjectBuilder()
                .add("title", title100)
                .build();

        // When
        Response response = target.path("/api/todos/" + id).request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
    }

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: タイトルが 101 文字のとき 400 が返る（境界値）
     */
    @Test
    @DisplayName("PUT /api/todos/{id} - タイトルが 101 文字のとき 400 を返す")
    void update_returns400WhenTitleExceeds100Chars() {
        // Given
        JsonObject created = postTodoAndGetBody("タイトル", "");
        long id = created.getJsonNumber("id").longValue();
        String title101 = "あ".repeat(101);
        JsonObject request = Json.createObjectBuilder()
                .add("title", title101)
                .build();

        // When
        Response response = target.path("/api/todos/" + id).request()
                .put(Entity.json(request.toString()));

        // Then
        assertEquals(400, response.getStatus(), "400 Bad Request が返ること");
    }

    // ── DELETE /api/todos/{id} ──

    /**
     * 前提条件: id=1 の Todo が存在する
     * 事後条件: 204 No Content が返る
     */
    @Test
    @DisplayName("DELETE /api/todos/{id} - 存在する id を削除できる")
    void delete_deletesTodo() {
        // Given
        JsonObject created = postTodoAndGetBody("削除対象", "");
        long id = created.getJsonNumber("id").longValue();

        // When
        Response response = target.path("/api/todos/" + id).request().delete();

        // Then
        assertEquals(204, response.getStatus(), "204 No Content が返ること");
    }

    /**
     * 前提条件: Todo が 1 件存在する
     * 事後条件: 削除後に一覧から消えている
     */
    @Test
    @DisplayName("DELETE /api/todos/{id} - 削除後に一覧から消えていること")
    void delete_deletedTodoDoesNotAppearInList() {
        // Given
        JsonObject created = postTodoAndGetBody("削除対象", "");
        long id = created.getJsonNumber("id").longValue();
        target.path("/api/todos/" + id).request().delete();

        // When
        Response response = target.path("/api/todos").request().get();
        JsonArray todos = response.readEntity(JsonArray.class);

        // Then
        assertTrue(todos.isEmpty(), "削除後に一覧が空であること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 404 Not Found が返る
     */
    @Test
    @DisplayName("DELETE /api/todos/{id} - 存在しない id を削除しようとすると 404 を返す")
    void delete_returns404ForUnknownId() {
        // Given: setUp でリポジトリはクリア済み

        // When
        Response response = target.path("/api/todos/999").request().delete();

        // Then
        assertEquals(404, response.getStatus(), "404 Not Found が返ること");
    }

    // ── PATCH /api/todos/{id}/toggle ──

    /**
     * 前提条件: completed=false の Todo が存在する
     * 事後条件: completed=true になる
     */
    @Test
    @DisplayName("PATCH /api/todos/{id}/toggle - 未完了 → 完了にトグルできる")
    void toggle_togglesFromFalseToTrue() {
        // Given
        JsonObject created = postTodoAndGetBody("トグル対象", "");
        long id = created.getJsonNumber("id").longValue();

        // When
        Response response = patchTarget.path("/api/todos/" + id + "/toggle").request()
                .method("PATCH", Entity.json(""));

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertTrue(todo.getBoolean("completed"), "completed が true になること");
    }

    /**
     * 前提条件: completed=true の Todo が存在する
     * 事後条件: completed=false になる
     */
    @Test
    @DisplayName("PATCH /api/todos/{id}/toggle - 完了 → 未完了にトグルできる")
    void toggle_togglesFromTrueToFalse() {
        // Given
        JsonObject created = postTodoAndGetBody("トグル対象", "");
        long id = created.getJsonNumber("id").longValue();
        patchTarget.path("/api/todos/" + id + "/toggle").request()
                .method("PATCH", Entity.json(""));

        // When
        Response response = patchTarget.path("/api/todos/" + id + "/toggle").request()
                .method("PATCH", Entity.json(""));

        // Then
        assertEquals(200, response.getStatus(), "200 OK が返ること");
        JsonObject todo = response.readEntity(JsonObject.class);
        assertFalse(todo.getBoolean("completed"), "completed が false に戻ること");
    }

    /**
     * 前提条件: リポジトリが空
     * 事後条件: 404 Not Found が返る
     */
    @Test
    @DisplayName("PATCH /api/todos/{id}/toggle - 存在しない id をトグルしようとすると 404 を返す")
    void toggle_returns404ForUnknownId() {
        // Given: setUp でリポジトリはクリア済み

        // When
        Response response = patchTarget.path("/api/todos/999/toggle").request()
                .method("PATCH", Entity.json(""));

        // Then
        assertEquals(404, response.getStatus(), "404 Not Found が返ること");
    }

    /**
     * Todo を POST して作成するヘルパーメソッド
     *
     * @param title       タイトル
     * @param description 説明
     * @return POST レスポンス
     */
    private Response postTodo(String title, String description) {
        JsonObject request = Json.createObjectBuilder()
                .add("title", title)
                .add("description", description)
                .build();
        return target.path("/api/todos").request()
                .post(Entity.json(request.toString()));
    }

    /**
     * Todo を POST して作成し、レスポンス本文を返すヘルパーメソッド
     *
     * @param title       タイトル
     * @param description 説明
     * @return 作成した Todo の JsonObject
     */
    private JsonObject postTodoAndGetBody(String title, String description) {
        Response response = postTodo(title, description);
        return response.readEntity(JsonObject.class);
    }
}
