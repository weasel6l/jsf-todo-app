/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.service;

import com.example.todo.api.dto.TodoAddRequest;
import com.example.todo.api.dto.TodoAddResponse;
import com.example.todo.api.dto.TodoItemDto;
import com.example.todo.api.dto.TodoListResponse;
import com.example.todo.api.dto.TodoToggleResponse;
import com.example.todo.model.TodoItem;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Todo 一覧画面の業務処理を担当するサービスクラス。
 * 一覧取得・追加・削除・完了トグルを提供する。
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TodoListService {

    /**
     * Todo のインメモリストア。
     */
    private final TodoStore todoStore;

    /**
     * すべての Todo を統計情報と合わせて返す。
     *
     * @return Todo 一覧レスポンス
     */
    public TodoListResponse getList() {
        List<TodoItem> all = todoStore.findAll();
        List<TodoItemDto> items = all.stream()
                .map(this::toItemDto)
                .collect(Collectors.toList());
        long completed = all.stream().filter(TodoItem::isCompleted).count();
        long pending = all.stream().filter(item -> !item.isCompleted()).count();
        return new TodoListResponse(all.size(), completed, pending, items);
    }

    /**
     * 新しい Todo を追加して返す。
     *
     * @param request 追加リクエスト
     * @return 追加した Todo のレスポンス
     */
    public TodoAddResponse addTodo(TodoAddRequest request) {
        TodoItem item = new TodoItem(
                todoStore.nextId(),
                request.getTitle().trim(),
                request.getDescription() != null ? request.getDescription().trim() : ""
        );
        todoStore.save(item);
        return new TodoAddResponse(
                item.getId(), item.getTitle(), item.getDescription(),
                item.isCompleted(), item.getFormattedCreatedAt()
        );
    }

    /**
     * 指定した ID の Todo を削除する。
     *
     * @param id 削除対象の ID
     * @return 削除が成功した場合は true
     */
    public boolean deleteTodo(Long id) {
        return todoStore.deleteById(id);
    }

    /**
     * 指定した ID の Todo の完了状態をトグルする。
     *
     * @param id トグル対象の ID
     * @return 更新後のトグルレスポンス（ID が存在しない場合は empty）
     */
    public Optional<TodoToggleResponse> toggleComplete(Long id) {
        return todoStore.findById(id).map(item -> {
            item.setCompleted(!item.isCompleted());
            todoStore.save(item);
            return new TodoToggleResponse(item.getId(), item.isCompleted());
        });
    }

    /**
     * TodoItem を TodoItemDto に変換する。
     *
     * @param item 変換元
     * @return 変換後の DTO
     */
    private TodoItemDto toItemDto(TodoItem item) {
        return new TodoItemDto(
                item.getId(), item.getTitle(), item.getDescription(),
                item.isCompleted(), item.getFormattedCreatedAt()
        );
    }
}
