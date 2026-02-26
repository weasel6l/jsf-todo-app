/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.service;

import com.example.todo.dto.CreateTodoRequest;
import com.example.todo.dto.TodoResponse;
import com.example.todo.dto.UpdateTodoRequest;
import com.example.todo.model.TodoItem;
import com.example.todo.repository.TodoRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Todo ビジネスロジックを担うサービスクラス
 */
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED, force = true)
public class TodoService {

    /**
     * Todo リポジトリ
     */
    private final TodoRepository repository;

    /**
     * 全件取得する
     *
     * @return Todo レスポンスのリスト
     */
    public List<TodoResponse> findAll() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * ID で単件取得する
     *
     * @param id 取得対象の ID
     * @return Todo レスポンス
     * @throws NotFoundException 対象 ID が存在しない場合
     */
    public TodoResponse findById(Long id) {
        return repository.findById(id)
                .map(this::toResponse)
                .orElseThrow(NotFoundException::new);
    }

    /**
     * Todo を新規作成する
     *
     * @param request 作成リクエスト
     * @return 作成した Todo レスポンス
     */
    public TodoResponse create(CreateTodoRequest request) {
        String title = request.getTitle().trim();
        String description = request.getDescription() != null
                ? request.getDescription().trim()
                : "";
        TodoItem item = repository.save(title, description);
        return toResponse(item);
    }

    /**
     * Todo を更新する
     *
     * @param id      更新対象の ID
     * @param request 更新リクエスト
     * @return 更新後の Todo レスポンス
     * @throws NotFoundException 対象 ID が存在しない場合
     */
    public TodoResponse update(Long id, UpdateTodoRequest request) {
        TodoItem item = repository.findById(id)
                .orElseThrow(NotFoundException::new);
        item.setTitle(request.getTitle().trim());
        item.setDescription(request.getDescription() != null
                ? request.getDescription().trim()
                : "");
        return toResponse(item);
    }

    /**
     * Todo を削除する
     *
     * @param id 削除対象の ID
     * @throws NotFoundException 対象 ID が存在しない場合
     */
    public void delete(Long id) {
        if (!repository.deleteById(id)) {
            throw new NotFoundException();
        }
    }

    /**
     * Todo の完了/未完了を切り替える
     *
     * @param id 対象の ID
     * @return トグル後の Todo レスポンス
     * @throws NotFoundException 対象 ID が存在しない場合
     */
    public TodoResponse toggle(Long id) {
        TodoItem item = repository.findById(id)
                .orElseThrow(NotFoundException::new);
        item.setCompleted(!item.isCompleted());
        return toResponse(item);
    }

    /**
     * TodoItem を TodoResponse に変換する
     *
     * @param item 変換元の TodoItem
     * @return TodoResponse
     */
    private TodoResponse toResponse(TodoItem item) {
        return new TodoResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.isCompleted(),
                item.getFormattedCreatedAt()
        );
    }
}
