/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.api.service;

import com.example.todo.api.dto.TodoDetailResponse;
import com.example.todo.api.dto.TodoDetailUpdateRequest;
import com.example.todo.model.TodoItem;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * Todo 詳細画面の業務処理を担当するサービスクラス。
 * 詳細取得・更新を提供する。
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TodoDetailService {

    /**
     * Todo のインメモリストア。
     */
    private final TodoStore todoStore;

    /**
     * 指定した ID の Todo 詳細を返す。
     *
     * @param id 取得対象の ID
     * @return Todo 詳細レスポンス（ID が存在しない場合は empty）
     */
    public Optional<TodoDetailResponse> getDetail(Long id) {
        return todoStore.findById(id).map(this::toDetailResponse);
    }

    /**
     * 指定した ID の Todo のタイトルと説明を更新して返す。
     *
     * @param id 更新対象の ID
     * @param request 更新リクエスト
     * @return 更新後の詳細レスポンス（ID が存在しない場合は empty）
     */
    public Optional<TodoDetailResponse> updateTodo(Long id, TodoDetailUpdateRequest request) {
        return todoStore.findById(id).map(item -> {
            item.setTitle(request.getTitle().trim());
            item.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
            todoStore.save(item);
            return toDetailResponse(item);
        });
    }

    /**
     * TodoItem を TodoDetailResponse に変換する。
     *
     * @param item 変換元
     * @return 変換後のレスポンス DTO
     */
    private TodoDetailResponse toDetailResponse(TodoItem item) {
        return new TodoDetailResponse(
                item.getId(), item.getTitle(), item.getDescription(),
                item.isCompleted(), item.getFormattedCreatedAt()
        );
    }
}
