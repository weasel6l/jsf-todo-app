/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.service;

import com.example.todo.model.TodoItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import javax.enterprise.context.ApplicationScoped;

/**
 * Todo アイテムをインメモリで管理するストア。
 * アプリケーション起動中はすべてのリクエスト間で状態を共有する。
 */
@ApplicationScoped
public class TodoStore {

    /**
     * Todo アイテムを ID をキーとして保持するマップ。
     */
    private final Map<Long, TodoItem> store = new ConcurrentHashMap<>();

    /**
     * ID 採番用カウンター。
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * ストアを初期化し、サンプルデータを追加する。
     */
    public TodoStore() {
        addInitialData();
    }

    /**
     * 次の ID を採番して返す。
     *
     * @return 採番された ID
     */
    public long nextId() {
        return idGenerator.getAndIncrement();
    }

    /**
     * すべての Todo を取得する。
     *
     * @return 全 Todo リスト
     */
    public List<TodoItem> findAll() {
        return new ArrayList<>(store.values());
    }

    /**
     * 指定した ID の Todo を取得する。
     *
     * @param id 検索する ID
     * @return 対応する TodoItem（不在時は empty）
     */
    public Optional<TodoItem> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Todo を保存する（新規追加・更新の両方に対応）。
     *
     * @param item 保存する TodoItem
     */
    public void save(TodoItem item) {
        store.put(item.getId(), item);
    }

    /**
     * 指定した ID の Todo を削除する。
     *
     * @param id 削除する ID
     * @return 削除が成功した場合は true、対象が存在しない場合は false
     */
    public boolean deleteById(Long id) {
        return store.remove(id) != null;
    }

    /**
     * 初期サンプルデータをストアに追加する。
     */
    private void addInitialData() {
        TodoItem item1 = new TodoItem(idGenerator.getAndIncrement(), "買い物をする", "スーパーで食材を購入する");
        TodoItem item2 = new TodoItem(idGenerator.getAndIncrement(), "レポートを書く", "プロジェクトの進捗レポートを完成させる");
        TodoItem item3 = new TodoItem(idGenerator.getAndIncrement(), "運動する", "30分のジョギング");
        store.put(item1.getId(), item1);
        store.put(item2.getId(), item2);
        store.put(item3.getId(), item3);
    }
}
