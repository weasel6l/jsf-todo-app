/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.repository;

import com.example.todo.model.TodoItem;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Todo アイテムのインメモリリポジトリ
 */
@ApplicationScoped
public class TodoRepository {

    /**
     * Todo アイテムの格納リスト
     */
    private final List<TodoItem> items = Collections.synchronizedList(new ArrayList<>());

    /**
     * ID 採番カウンター
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * 全件取得する
     *
     * @return Todo アイテムのリスト
     */
    public List<TodoItem> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(items));
    }

    /**
     * ID で単件取得する
     *
     * @param id 取得対象の ID
     * @return 対象の TodoItem。存在しない場合は empty
     */
    public Optional<TodoItem> findById(Long id) {
        return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
    }

    /**
     * Todo アイテムを保存する
     *
     * @param title       タイトル
     * @param description 説明
     * @return 保存した TodoItem
     */
    public TodoItem save(String title, String description) {
        TodoItem item = new TodoItem(idGenerator.getAndIncrement(), title, description);
        items.add(item);
        return item;
    }

    /**
     * ID で Todo アイテムを削除する
     *
     * @param id 削除対象の ID
     * @return 削除に成功した場合は true、対象が存在しない場合は false
     */
    public boolean deleteById(Long id) {
        return items.removeIf(item -> item.getId().equals(id));
    }

    /**
     * テスト用にリポジトリの状態をリセットする
     */
    public void clear() {
        items.clear();
        idGenerator.set(1);
    }
}
