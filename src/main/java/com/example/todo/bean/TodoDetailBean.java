package com.example.todo.bean;

import com.example.todo.model.TodoItem;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * Todo 詳細・編集ページを管理するビュースコープの Bean。
 *
 * <p><strong>FlashContainer からのデータ取得:</strong><br>
 * {@code @PostConstruct} 初期化メソッド内で {@link Flash Flash コンテナ} の
 * {@code get("selectedTodo")} を呼び出し、前のページ ({@code todos.xhtml}) が
 * Flash に格納した {@link TodoItem} を取得します。</p>
 *
 * <pre>
 * Flash flash = externalContext.getFlash();
 * selectedTodo = (TodoItem) flash.get("selectedTodo");  // Flash から取得
 * </pre>
 */
@Named
@ViewScoped
public class TodoDetailBean implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Flash コンテナから取得した選択 Todo */
    private TodoItem selectedTodo;

    private String editTitle;
    private String editDescription;

    /**
     * ビュー初期化時に Flash コンテナから選択された Todo を取得する。
     *
     * <p>JSF の Flash スコープは POST/Redirect/GET パターン内で
     * 1 リダイレクトを跨いでデータを保持します。ここでは
     * {@code TodoBean#viewDetail} が格納した "selectedTodo" キーの
     * オブジェクトを取得しています。</p>
     */
    @PostConstruct
    public void init() {
        ExternalContext externalContext =
                FacesContext.getCurrentInstance().getExternalContext();
        Flash flash = externalContext.getFlash();

        // FlashContainer から選択された TodoItem を取得
        selectedTodo = (TodoItem) flash.get("selectedTodo");

        if (selectedTodo != null) {
            // 編集フォームの初期値を設定
            editTitle = selectedTodo.getTitle();
            editDescription = selectedTodo.getDescription();
        }
    }

    // ---- アクションメソッド ----

    /**
     * 編集内容を保存して Todo リストページへリダイレクトする。
     * 更新完了メッセージは Flash に格納してリダイレクト後に表示する。
     */
    public String save() {
        if (selectedTodo == null) {
            return "todos?faces-redirect=true";
        }
        if (editTitle == null || editTitle.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "警告", "タイトルは必須です");
            return null;
        }

        // 元の TodoItem を直接更新 (SessionScoped の TodoBean が保持するオブジェクトと同一参照)
        selectedTodo.setTitle(editTitle.trim());
        selectedTodo.setDescription(
                editDescription != null ? editDescription.trim() : "");

        // リダイレクト後にメッセージを表示するため Flash に格納
        ExternalContext externalContext =
                FacesContext.getCurrentInstance().getExternalContext();
        Flash flash = externalContext.getFlash();
        flash.setKeepMessages(true);
        addMessage(FacesMessage.SEVERITY_INFO, "成功", "Todo を更新しました");

        return "todos?faces-redirect=true";
    }

    /**
     * 変更をキャンセルして Todo リストページへ戻る。
     */
    public String cancel() {
        return "todos?faces-redirect=true";
    }

    // ---- ヘルパー ----

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ---- Getters / Setters ----

    public TodoItem getSelectedTodo() {
        return selectedTodo;
    }

    public String getEditTitle() {
        return editTitle;
    }

    public void setEditTitle(String editTitle) {
        this.editTitle = editTitle;
    }

    public String getEditDescription() {
        return editDescription;
    }

    public void setEditDescription(String editDescription) {
        this.editDescription = editDescription;
    }
}
