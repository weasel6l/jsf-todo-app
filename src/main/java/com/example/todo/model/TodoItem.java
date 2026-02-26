/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
package com.example.todo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Todo アイテムのモデルクラス
 */
@Getter
@Setter
@NoArgsConstructor
public class TodoItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    /**
     * 一意識別子
     */
    private Long id;

    /**
     * タイトル
     */
    private String title;

    /**
     * 説明
     */
    private String description;

    /**
     * 完了フラグ
     */
    private boolean completed;

    /**
     * 作成日時
     */
    private LocalDateTime createdAt;

    /**
     * 主要コンストラクタ
     *
     * @param id          一意識別子
     * @param title       タイトル
     * @param description 説明
     */
    public TodoItem(Long id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.completed = false;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 作成日時をフォーマットして返す
     *
     * @return yyyy/MM/dd HH:mm 形式の作成日時
     */
    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(FORMATTER) : "";
    }
}
