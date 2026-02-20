package com.example.todo.bean;

import com.example.todo.model.TodoItem;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link TodoBean} の単体テスト。
 *
 * <p>テスト観点:</p>
 * <ul>
 *   <li>コンストラクタによる初期データの検証</li>
 *   <li>{@code addTodo}: 正常・バリデーションエラー・トリム処理</li>
 *   <li>{@code deleteTodo}: 存在 ID / 非存在 ID</li>
 *   <li>{@code toggleComplete}: 完了⇔未完了の切り替え</li>
 *   <li>{@code viewDetail}: FlashContainer への格納とナビゲーション</li>
 *   <li>{@code getCompletedCount} / {@code getPendingCount}: 件数集計</li>
 * </ul>
 *
 * <p>{@link FacesContext#getCurrentInstance()} は static メソッドのため
 * Mockito の {@code mockStatic} を用いてモックする。</p>
 */
@DisplayName("TodoBean のテスト")
class TodoBeanTest {

    /** Mockito static モックのクローズ用ハンドル */
    private MockedStatic<FacesContext> mockedFacesContext;

    private FacesContext mockContext;
    private ExternalContext mockExtCtx;
    private Flash mockFlash;

    /** テスト対象 */
    private TodoBean bean;

    @BeforeEach
    void setUp() {
        // FacesContext.getCurrentInstance() を static モックに差し替える
        mockContext  = mock(FacesContext.class);
        mockExtCtx   = mock(ExternalContext.class);
        mockFlash    = mock(Flash.class);

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockContext);
        when(mockContext.getExternalContext()).thenReturn(mockExtCtx);
        when(mockExtCtx.getFlash()).thenReturn(mockFlash);

        bean = new TodoBean();
    }

    @AfterEach
    void tearDown() {
        mockedFacesContext.close();
    }

    // =========================================================================
    // コンストラクタ / 初期化
    // =========================================================================

    @Nested
    @DisplayName("コンストラクタ / 初期化のテスト")
    class ConstructorTest {

        @Test
        @DisplayName("初期状態: サンプルデータとして 3 件の Todo が登録されていること")
        void constructor_initialListHasThreeItems() {
            // Given: TodoBean が生成された直後
            // When
            List<TodoItem> list = bean.getTodoList();

            // Then
            assertEquals(3, list.size(),
                    "コンストラクタ実行後、サンプルとして 3 件の Todo が登録されていること");
        }

        @Test
        @DisplayName("初期状態: newTitle が null であること")
        void constructor_newTitleIsNull() {
            // Given / When / Then
            assertNull(bean.getNewTitle(),
                    "初期状態で newTitle は null であること");
        }

        @Test
        @DisplayName("初期状態: newDescription が null であること")
        void constructor_newDescriptionIsNull() {
            // Given / When / Then
            assertNull(bean.getNewDescription(),
                    "初期状態で newDescription は null であること");
        }

        @Test
        @DisplayName("初期状態: completedCount が 0 であること (全件未完了)")
        void constructor_completedCountIsZero() {
            // Given / When / Then
            assertEquals(0L, bean.getCompletedCount(),
                    "初期状態では全件が未完了のため completedCount は 0 であること");
        }

        @Test
        @DisplayName("初期状態: pendingCount が 3 であること")
        void constructor_pendingCountIsThree() {
            // Given / When / Then
            assertEquals(3L, bean.getPendingCount(),
                    "初期状態では全件が未完了のため pendingCount は 3 であること");
        }
    }

    // =========================================================================
    // addTodo
    // =========================================================================

    @Nested
    @DisplayName("addTodo のテスト")
    class AddTodoTest {

        @Test
        @DisplayName("異常系: タイトルが null のとき、null が返りリストが変化しないこと")
        void addTodo_nullTitle_returnsNull_listUnchanged() {
            // Given: newTitle が null の状態
            bean.setNewTitle(null);
            int sizeBefore = bean.getTodoList().size();

            // When
            String result = bean.addTodo();

            // Then
            assertNull(result,
                    "タイトルが null のとき addTodo は null を返すこと");
            assertEquals(sizeBefore, bean.getTodoList().size(),
                    "タイトルが null のときリストのサイズが変化しないこと");
        }

        @Test
        @DisplayName("異常系: タイトルが空文字のとき、null が返りリストが変化しないこと")
        void addTodo_emptyTitle_returnsNull_listUnchanged() {
            // Given: newTitle が空文字
            bean.setNewTitle("");
            int sizeBefore = bean.getTodoList().size();

            // When
            String result = bean.addTodo();

            // Then
            assertNull(result,
                    "タイトルが空文字のとき addTodo は null を返すこと");
            assertEquals(sizeBefore, bean.getTodoList().size(),
                    "タイトルが空文字のときリストのサイズが変化しないこと");
        }

        @Test
        @DisplayName("異常系: タイトルが空白のみのとき、null が返りリストが変化しないこと")
        void addTodo_blankTitle_returnsNull_listUnchanged() {
            // Given: newTitle が空白のみ
            bean.setNewTitle("   ");
            int sizeBefore = bean.getTodoList().size();

            // When
            String result = bean.addTodo();

            // Then
            assertNull(result,
                    "タイトルが空白のみのとき addTodo は null を返すこと");
            assertEquals(sizeBefore, bean.getTodoList().size(),
                    "タイトルが空白のみのときリストのサイズが変化しないこと");
        }

        @Test
        @DisplayName("正常系: 有効なタイトルを入力したとき、リストに 1 件追加されること")
        void addTodo_validTitle_addsOneItem() {
            // Given: 有効なタイトルと説明が設定されている
            bean.setNewTitle("新規タスク");
            bean.setNewDescription("詳細");
            int sizeBefore = bean.getTodoList().size();

            // When
            bean.addTodo();

            // Then
            assertEquals(sizeBefore + 1, bean.getTodoList().size(),
                    "addTodo 成功後、リストのサイズが 1 増加すること");
        }

        @Test
        @DisplayName("正常系: 有効なタイトルを追加したとき、null が返ること")
        void addTodo_validTitle_returnsNull() {
            // Given
            bean.setNewTitle("新規タスク");

            // When
            String result = bean.addTodo();

            // Then
            assertNull(result,
                    "addTodo 成功後、ナビゲーション結果は null (同一ページ留まり) であること");
        }

        @Test
        @DisplayName("正常系: addTodo 後に newTitle が空文字にリセットされること")
        void addTodo_validTitle_resetsNewTitle() {
            // Given
            bean.setNewTitle("新規タスク");

            // When
            bean.addTodo();

            // Then
            assertEquals("", bean.getNewTitle(),
                    "addTodo 後に newTitle が空文字にリセットされること");
        }

        @Test
        @DisplayName("正常系: addTodo 後に newDescription が空文字にリセットされること")
        void addTodo_validTitle_resetsNewDescription() {
            // Given
            bean.setNewTitle("新規タスク");
            bean.setNewDescription("説明テキスト");

            // When
            bean.addTodo();

            // Then
            assertEquals("", bean.getNewDescription(),
                    "addTodo 後に newDescription が空文字にリセットされること");
        }

        @Test
        @DisplayName("正常系: description が null のとき、TodoItem の description が空文字で保存されること")
        void addTodo_nullDescription_savedAsEmpty() {
            // Given: description が null
            bean.setNewTitle("タスク");
            bean.setNewDescription(null);

            // When
            bean.addTodo();

            // Then: 最後に追加されたアイテムの description が "" であること
            List<TodoItem> list = bean.getTodoList();
            TodoItem added = list.get(list.size() - 1);
            assertEquals("", added.getDescription(),
                    "newDescription が null のとき TodoItem の description は空文字であること");
        }

        @Test
        @DisplayName("正常系: タイトルに前後空白があるとき、trim されて保存されること")
        void addTodo_titleWithSpaces_isTrimmed() {
            // Given: 前後に空白があるタイトル
            bean.setNewTitle("  買い物  ");

            // When
            bean.addTodo();

            // Then: 追加された item のタイトルが trim されていること
            List<TodoItem> list = bean.getTodoList();
            TodoItem added = list.get(list.size() - 1);
            assertEquals("買い物", added.getTitle(),
                    "タイトルに前後空白がある場合、trim されて保存されること");
        }

        @Test
        @DisplayName("正常系: 追加された Todo の completed が false であること")
        void addTodo_validTitle_completedIsFalse() {
            // Given
            bean.setNewTitle("新規タスク");

            // When
            bean.addTodo();

            // Then
            List<TodoItem> list = bean.getTodoList();
            TodoItem added = list.get(list.size() - 1);
            assertFalse(added.isCompleted(),
                    "新規追加した Todo の completed は false であること");
        }

        @Test
        @DisplayName("異常系: タイトルが null のとき、WARN メッセージが FacesContext に追加されること")
        void addTodo_nullTitle_addsWarnMessage() {
            // Given
            bean.setNewTitle(null);

            // When
            bean.addTodo();

            // Then: addMessage が 1 回呼ばれ WARN が渡されること
            verify(mockContext, times(1))
                    .addMessage(isNull(), argThat(msg ->
                        msg.getSeverity() == FacesMessage.SEVERITY_WARN
                    ));
        }

        @Test
        @DisplayName("正常系: 有効なタイトルのとき、INFO メッセージが FacesContext に追加されること")
        void addTodo_validTitle_addsInfoMessage() {
            // Given
            bean.setNewTitle("新規タスク");

            // When
            bean.addTodo();

            // Then
            verify(mockContext, times(1))
                    .addMessage(isNull(), argThat(msg ->
                        msg.getSeverity() == FacesMessage.SEVERITY_INFO
                    ));
        }
    }

    // =========================================================================
    // deleteTodo
    // =========================================================================

    @Nested
    @DisplayName("deleteTodo のテスト")
    class DeleteTodoTest {

        @Test
        @DisplayName("正常系: 存在する ID を指定したとき、該当 Todo がリストから削除されること")
        void deleteTodo_existingId_removesItem() {
            // Given: リストの先頭 Todo の ID を取得
            Long targetId = bean.getTodoList().get(0).getId();
            int sizeBefore = bean.getTodoList().size();

            // When
            bean.deleteTodo(targetId);

            // Then
            assertEquals(sizeBefore - 1, bean.getTodoList().size(),
                    "存在する ID を削除後、リストのサイズが 1 減ること");
            assertTrue(bean.getTodoList().stream()
                            .noneMatch(t -> t.getId().equals(targetId)),
                    "削除した ID の Todo がリストに残っていないこと");
        }

        @Test
        @DisplayName("正常系: deleteTodo は null を返すこと (同一ページ留まり)")
        void deleteTodo_returnsNull() {
            // Given
            Long targetId = bean.getTodoList().get(0).getId();

            // When
            String result = bean.deleteTodo(targetId);

            // Then
            assertNull(result,
                    "deleteTodo はナビゲーション結果として null を返すこと");
        }

        @Test
        @DisplayName("正常系: 存在しない ID を指定したとき、リストのサイズが変化しないこと")
        void deleteTodo_nonExistingId_listUnchanged() {
            // Given: 存在しない ID (大きな値)
            Long nonExistingId = 9999L;
            int sizeBefore = bean.getTodoList().size();

            // When
            bean.deleteTodo(nonExistingId);

            // Then
            assertEquals(sizeBefore, bean.getTodoList().size(),
                    "存在しない ID を指定した場合、リストのサイズが変化しないこと");
        }

        @Test
        @DisplayName("正常系: deleteTodo 呼び出し後、INFO メッセージが追加されること")
        void deleteTodo_addsInfoMessage() {
            // Given
            Long targetId = bean.getTodoList().get(0).getId();

            // When
            bean.deleteTodo(targetId);

            // Then
            verify(mockContext, times(1))
                    .addMessage(isNull(), argThat(msg ->
                        msg.getSeverity() == FacesMessage.SEVERITY_INFO
                    ));
        }
    }

    // =========================================================================
    // toggleComplete
    // =========================================================================

    @Nested
    @DisplayName("toggleComplete のテスト")
    class ToggleCompleteTest {

        @Test
        @DisplayName("正常系: 未完了の Todo に toggleComplete を実行すると、completed が true になること")
        void toggleComplete_pending_becomesCompleted() {
            // Given: 未完了の Todo
            TodoItem item = new TodoItem(100L, "タスク", "説明");
            item.setCompleted(false);

            // When
            bean.toggleComplete(item);

            // Then
            assertTrue(item.isCompleted(),
                    "toggleComplete 後、未完了の Todo が完了状態になること");
        }

        @Test
        @DisplayName("正常系: 完了済みの Todo に toggleComplete を実行すると、completed が false になること")
        void toggleComplete_completed_becomesPending() {
            // Given: 完了済みの Todo
            TodoItem item = new TodoItem(100L, "タスク", "説明");
            item.setCompleted(true);

            // When
            bean.toggleComplete(item);

            // Then
            assertFalse(item.isCompleted(),
                    "toggleComplete 後、完了済みの Todo が未完了状態になること");
        }

        @Test
        @DisplayName("正常系: toggleComplete は null を返すこと")
        void toggleComplete_returnsNull() {
            // Given
            TodoItem item = new TodoItem(100L, "タスク", "説明");

            // When
            String result = bean.toggleComplete(item);

            // Then
            assertNull(result,
                    "toggleComplete はナビゲーション結果として null を返すこと");
        }

        @Test
        @DisplayName("正常系: toggleComplete を 2 回実行すると、元の状態に戻ること")
        void toggleComplete_twice_restoresOriginalState() {
            // Given
            TodoItem item = new TodoItem(100L, "タスク", "説明");
            boolean originalState = item.isCompleted();

            // When: 2 回トグルする
            bean.toggleComplete(item);
            bean.toggleComplete(item);

            // Then
            assertEquals(originalState, item.isCompleted(),
                    "toggleComplete を 2 回実行すると元の状態に戻ること");
        }
    }

    // =========================================================================
    // viewDetail
    // =========================================================================

    @Nested
    @DisplayName("viewDetail のテスト")
    class ViewDetailTest {

        @Test
        @DisplayName("正常系: viewDetail は 'detail?faces-redirect=true' を返すこと")
        void viewDetail_returnsDetailRedirect() {
            // Given: Todo を 1 件用意する
            TodoItem todo = new TodoItem(1L, "タスク", "説明");

            // When
            String result = bean.viewDetail(todo);

            // Then
            assertEquals("detail?faces-redirect=true", result,
                    "viewDetail は 'detail?faces-redirect=true' を返すこと");
        }

        @Test
        @DisplayName("正常系: viewDetail 後、Flash に 'selectedTodo' が格納されること")
        void viewDetail_putsSelectedTodoInFlash() {
            // Given
            TodoItem todo = new TodoItem(1L, "タスク", "説明");

            // When
            bean.viewDetail(todo);

            // Then: flash.put("selectedTodo", todo) が呼ばれること
            verify(mockFlash).put("selectedTodo", todo);
        }

        @Test
        @DisplayName("正常系: viewDetail 後、Flash に 'selectedTodoId' が格納されること")
        void viewDetail_putsSelectedTodoIdInFlash() {
            // Given
            TodoItem todo = new TodoItem(42L, "タスク", "説明");

            // When
            bean.viewDetail(todo);

            // Then
            verify(mockFlash).put("selectedTodoId", 42L);
        }

        @Test
        @DisplayName("正常系: viewDetail 後、Flash の setKeepMessages(true) が呼ばれること")
        void viewDetail_setsKeepMessagesTrue() {
            // Given
            TodoItem todo = new TodoItem(1L, "タスク", "説明");

            // When
            bean.viewDetail(todo);

            // Then
            verify(mockFlash).setKeepMessages(true);
        }
    }

    // =========================================================================
    // getCompletedCount / getPendingCount
    // =========================================================================

    @Nested
    @DisplayName("getCompletedCount / getPendingCount のテスト")
    class CountTest {

        @Test
        @DisplayName("正常系: 全件未完了の場合、completedCount=0 かつ pendingCount=3 であること")
        void count_allPending() {
            // Given: コンストラクタで生成した初期状態 (全件未完了)
            // When / Then
            assertEquals(0L, bean.getCompletedCount(),
                    "全件未完了の場合、completedCount は 0 であること");
            assertEquals(3L, bean.getPendingCount(),
                    "全件未完了の場合、pendingCount は 3 であること");
        }

        @Test
        @DisplayName("正常系: 1 件完了させたとき、completedCount=1 かつ pendingCount=2 になること")
        void count_oneCompleted() {
            // Given: リストの先頭を完了にする
            bean.getTodoList().get(0).setCompleted(true);

            // When / Then
            assertEquals(1L, bean.getCompletedCount(),
                    "1 件完了時、completedCount は 1 であること");
            assertEquals(2L, bean.getPendingCount(),
                    "1 件完了時、pendingCount は 2 であること");
        }

        @Test
        @DisplayName("正常系: 全件完了させたとき、completedCount=3 かつ pendingCount=0 になること")
        void count_allCompleted() {
            // Given: 全件を完了状態にする
            bean.getTodoList().forEach(t -> t.setCompleted(true));

            // When / Then
            assertEquals(3L, bean.getCompletedCount(),
                    "全件完了時、completedCount は 3 であること");
            assertEquals(0L, bean.getPendingCount(),
                    "全件完了時、pendingCount は 0 であること");
        }

        @Test
        @DisplayName("正常系: completedCount + pendingCount が getTodoList().size() と等しいこと")
        void count_sumEqualsListSize() {
            // Given: 1 件を完了にする
            bean.getTodoList().get(0).setCompleted(true);

            // When
            long total = bean.getCompletedCount() + bean.getPendingCount();

            // Then
            assertEquals((long) bean.getTodoList().size(), total,
                    "completedCount + pendingCount は getTodoList().size() と等しいこと");
        }
    }

    // =========================================================================
    // Getter / Setter
    // =========================================================================

    @Nested
    @DisplayName("Getter / Setter のテスト")
    class GetterSetterTest {

        @Test
        @DisplayName("setNewTitle / getNewTitle: 設定した値が取得できること")
        void setNewTitle_getNewTitle() {
            // Given
            String expected = "テストタイトル";

            // When
            bean.setNewTitle(expected);

            // Then
            assertEquals(expected, bean.getNewTitle(),
                    "setNewTitle で設定した値が getNewTitle で取得できること");
        }

        @Test
        @DisplayName("setNewDescription / getNewDescription: 設定した値が取得できること")
        void setNewDescription_getNewDescription() {
            // Given
            String expected = "テスト説明";

            // When
            bean.setNewDescription(expected);

            // Then
            assertEquals(expected, bean.getNewDescription(),
                    "setNewDescription で設定した値が getNewDescription で取得できること");
        }
    }
}
