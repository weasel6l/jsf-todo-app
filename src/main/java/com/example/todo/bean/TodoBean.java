package com.example.todo.bean;

import com.example.todo.model.TodoItem;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Todo リストを管理するセッションスコープの Bean。
 *
 * <p>「詳細ページへの遷移」時に {@link Flash Flash コンテナ} を使用して
 * 選択した {@link TodoItem} をリダイレクト後のページへ渡します。</p>
 *
 * <pre>
 * Flash flash = externalContext.getFlash();
 * flash.put("selectedTodo", item);          // Flash に格納
 * return "detail?faces-redirect=true";     // リダイレクト
 * </pre>
 */
@Named
@SessionScoped
public class TodoBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<TodoItem> todoList = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    private String newTitle;
    private String newDescription;

    public TodoBean() {
        // 初期サンプルデータ
        todoList.add(new TodoItem(idGenerator.getAndIncrement(),
                "買い物をする", "スーパーで食材を購入する"));
        todoList.add(new TodoItem(idGenerator.getAndIncrement(),
                "レポートを書く", "プロジェクトの進捗レポートを完成させる"));
        todoList.add(new TodoItem(idGenerator.getAndIncrement(),
                "運動する", "30分のジョギング"));
    }

    // ---- アクションメソッド ----

    /**
     * 新しい Todo を追加する。
     */
    public String addTodo() {
        if (newTitle == null || newTitle.trim().isEmpty()) {
            addMessage(FacesMessage.SEVERITY_WARN, "警告", "タイトルは必須です");
            return null;
        }
        TodoItem item = new TodoItem(
                idGenerator.getAndIncrement(),
                newTitle.trim(),
                newDescription != null ? newDescription.trim() : ""
        );
        todoList.add(item);
        newTitle = "";
        newDescription = "";
        addMessage(FacesMessage.SEVERITY_INFO, "成功", "Todo を追加しました");
        return null;
    }

    /**
     * 指定した ID の Todo を削除する。
     *
     * @param id 削除対象の Todo ID
     */
    public String deleteTodo(Long id) {
        todoList.removeIf(item -> item.getId().equals(id));
        addMessage(FacesMessage.SEVERITY_INFO, "成功", "Todo を削除しました");
        return null;
    }

    /**
     * Todo の完了/未完了を切り替える。
     *
     * @param item 対象の Todo
     */
    public String toggleComplete(TodoItem item) {
        item.setCompleted(!item.isCompleted());
        return null;
    }

    /**
     * 詳細ページへ遷移する。
     *
     * <p><strong>FlashContainer の使用:</strong><br>
     * JSF の Flash スコープ ({@code ExternalContext#getFlash()}) に選択した
     * {@link TodoItem} を格納し、リダイレクト後の {@code detail.xhtml} ページで
     * 同じオブジェクトを参照できるようにします。Flash スコープは 1 リクエストを
     * 超えて (= POST/Redirect/GET パターンを跨いで) データを保持します。</p>
     *
     * @param item 詳細表示する Todo
     * @return ナビゲーション結果
     */
    public String viewDetail(TodoItem item) {
        ExternalContext externalContext =
                FacesContext.getCurrentInstance().getExternalContext();
        Flash flash = externalContext.getFlash();

        // Flash に詳細ページで使用するデータを格納
        flash.setKeepMessages(true);        // メッセージもリダイレクト後に保持
        flash.put("selectedTodo", item);    // 選択した TodoItem を Flash に格納
        flash.put("selectedTodoId", item.getId());

        // faces-redirect=true: POST → REDIRECT → GET でFlash スコープを活用
        return "detail?faces-redirect=true";
    }

    // ---- ヘルパー ----

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(severity, summary, detail));
    }

    // ---- Getters / Setters ----

    public List<TodoItem> getTodoList() {
        return todoList;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public String getNewDescription() {
        return newDescription;
    }

    public void setNewDescription(String newDescription) {
        this.newDescription = newDescription;
    }

    public long getCompletedCount() {
        return todoList.stream().filter(TodoItem::isCompleted).count();
    }

    public long getPendingCount() {
        return todoList.stream().filter(item -> !item.isCompleted()).count();
    }
}
