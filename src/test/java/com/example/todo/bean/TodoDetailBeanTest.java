package com.example.todo.bean;

import com.example.todo.model.TodoItem;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;

import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * {@link TodoDetailBean} の単体テスト。
 *
 * <p>テスト観点:</p>
 * <ul>
 *   <li>{@code init}: Flash コンテナからの {@link TodoItem} 取得と編集フィールド初期化</li>
 *   <li>{@code save}: バリデーション・フィールド更新・ナビゲーション・FlashMessage</li>
 *   <li>{@code cancel}: ナビゲーション結果</li>
 *   <li>各 Getter / Setter の入出力</li>
 * </ul>
 *
 * <p>{@link FacesContext#getCurrentInstance()} は static メソッドのため
 * Mockito の {@code mockStatic} を用いてモックする。</p>
 */
@DisplayName("TodoDetailBean のテスト")
class TodoDetailBeanTest {

    private MockedStatic<FacesContext> mockedFacesContext;

    private FacesContext mockContext;
    private ExternalContext mockExtCtx;
    private Flash mockFlash;

    /** テスト対象 */
    private TodoDetailBean bean;

    @BeforeEach
    void setUp() {
        mockContext = mock(FacesContext.class);
        mockExtCtx  = mock(ExternalContext.class);
        mockFlash   = mock(Flash.class);

        mockedFacesContext = mockStatic(FacesContext.class);
        mockedFacesContext.when(FacesContext::getCurrentInstance).thenReturn(mockContext);
        when(mockContext.getExternalContext()).thenReturn(mockExtCtx);
        when(mockExtCtx.getFlash()).thenReturn(mockFlash);

        bean = new TodoDetailBean();
    }

    @AfterEach
    void tearDown() {
        mockedFacesContext.close();
    }

    /**
     * Flash コンテナに指定した {@link TodoItem} を格納して {@code bean.init()} を呼び出す
     * ヘルパーメソッド。
     */
    private void initBeanWithTodo(TodoItem todo) {
        when(mockFlash.get("selectedTodo")).thenReturn(todo);
        bean.init();
    }

    // =========================================================================
    // init
    // =========================================================================

    @Nested
    @DisplayName("init のテスト (FlashContainer からのデータ取得)")
    class InitTest {

        @Test
        @DisplayName("正常系: Flash に TodoItem が存在するとき、selectedTodo が設定されること")
        void init_flashHasTodo_selectedTodoIsSet() {
            // Given: Flash に TodoItem を格納する
            TodoItem todo = new TodoItem(1L, "タスク", "説明");

            // When
            initBeanWithTodo(todo);

            // Then
            assertNotNull(bean.getSelectedTodo(),
                    "Flash に TodoItem がある場合、init 後に selectedTodo が設定されること");
            assertEquals(1L, bean.getSelectedTodo().getId(),
                    "Flash から取得した TodoItem の id が一致すること");
        }

        @Test
        @DisplayName("正常系: Flash に TodoItem が存在するとき、editTitle が selectedTodo のタイトルで初期化されること")
        void init_flashHasTodo_editTitleInitialized() {
            // Given
            TodoItem todo = new TodoItem(1L, "買い物をする", "スーパーへ");

            // When
            initBeanWithTodo(todo);

            // Then
            assertEquals("買い物をする", bean.getEditTitle(),
                    "init 後に editTitle が selectedTodo のタイトルで初期化されること");
        }

        @Test
        @DisplayName("正常系: Flash に TodoItem が存在するとき、editDescription が selectedTodo の説明で初期化されること")
        void init_flashHasTodo_editDescriptionInitialized() {
            // Given
            TodoItem todo = new TodoItem(1L, "タスク", "詳細説明テキスト");

            // When
            initBeanWithTodo(todo);

            // Then
            assertEquals("詳細説明テキスト", bean.getEditDescription(),
                    "init 後に editDescription が selectedTodo の説明で初期化されること");
        }

        @Test
        @DisplayName("異常系: Flash が null を返すとき、selectedTodo が null のままであること")
        void init_flashReturnsNull_selectedTodoIsNull() {
            // Given: Flash の "selectedTodo" キーに null が格納されている
            when(mockFlash.get("selectedTodo")).thenReturn(null);

            // When
            bean.init();

            // Then
            assertNull(bean.getSelectedTodo(),
                    "Flash が null を返す場合、selectedTodo は null のままであること");
        }

        @Test
        @DisplayName("異常系: Flash が null を返すとき、editTitle / editDescription が null のままであること")
        void init_flashReturnsNull_editFieldsAreNull() {
            // Given
            when(mockFlash.get("selectedTodo")).thenReturn(null);

            // When
            bean.init();

            // Then
            assertNull(bean.getEditTitle(),
                    "selectedTodo が null の場合、editTitle は null のままであること");
            assertNull(bean.getEditDescription(),
                    "selectedTodo が null の場合、editDescription は null のままであること");
        }
    }

    // =========================================================================
    // save
    // =========================================================================

    @Nested
    @DisplayName("save のテスト")
    class SaveTest {

        @Test
        @DisplayName("境界値: selectedTodo が null のとき、'todos?faces-redirect=true' を返すこと")
        void save_selectedTodoIsNull_returnsRedirect() {
            // Given: init を呼ばず selectedTodo = null のまま
            // When
            String result = bean.save();

            // Then
            assertEquals("todos?faces-redirect=true", result,
                    "selectedTodo が null のとき save は 'todos?faces-redirect=true' を返すこと");
        }

        @Test
        @DisplayName("境界値: selectedTodo が null のとき、FacesContext へのメッセージ追加は行われないこと")
        void save_selectedTodoIsNull_noMessageAdded() {
            // Given
            // When
            bean.save();

            // Then: addMessage が呼ばれないこと
            verify(mockContext, never()).addMessage(any(), any());
        }

        @Test
        @DisplayName("異常系: editTitle が null のとき、null が返ること (ページ留まり)")
        void save_nullEditTitle_returnsNull() {
            // Given: selectedTodo を設定し、editTitle を null にする
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle(null);

            // When
            String result = bean.save();

            // Then
            assertNull(result,
                    "editTitle が null のとき save は null を返すこと (バリデーションエラー)");
        }

        @Test
        @DisplayName("異常系: editTitle が空文字のとき、null が返ること")
        void save_emptyEditTitle_returnsNull() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("");

            // When
            String result = bean.save();

            // Then
            assertNull(result,
                    "editTitle が空文字のとき save は null を返すこと (バリデーションエラー)");
        }

        @Test
        @DisplayName("異常系: editTitle が空白のみのとき、null が返ること")
        void save_blankEditTitle_returnsNull() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("   ");

            // When
            String result = bean.save();

            // Then
            assertNull(result,
                    "editTitle が空白のみのとき save は null を返すこと (バリデーションエラー)");
        }

        @Test
        @DisplayName("異常系: editTitle が空のとき、WARN メッセージが追加されること")
        void save_emptyEditTitle_addsWarnMessage() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("");

            // When
            bean.save();

            // Then
            verify(mockContext, times(1))
                    .addMessage(isNull(), argThat(msg ->
                        msg.getSeverity() == FacesMessage.SEVERITY_WARN
                    ));
        }

        @Test
        @DisplayName("正常系: 有効な入力のとき、selectedTodo のタイトルが更新されること")
        void save_validInput_updatesTodoTitle() {
            // Given
            TodoItem todo = new TodoItem(1L, "古いタイトル", "説明");
            initBeanWithTodo(todo);
            bean.setEditTitle("新しいタイトル");
            bean.setEditDescription("新しい説明");

            // When
            bean.save();

            // Then: selectedTodo のタイトルが直接更新されること (同一オブジェクト参照)
            assertEquals("新しいタイトル", todo.getTitle(),
                    "save 後、selectedTodo のタイトルが editTitle の値で更新されること");
        }

        @Test
        @DisplayName("正常系: 有効な入力のとき、selectedTodo の説明が更新されること")
        void save_validInput_updatesTodoDescription() {
            // Given
            TodoItem todo = new TodoItem(1L, "タスク", "古い説明");
            initBeanWithTodo(todo);
            bean.setEditTitle("タスク");
            bean.setEditDescription("新しい説明テキスト");

            // When
            bean.save();

            // Then
            assertEquals("新しい説明テキスト", todo.getDescription(),
                    "save 後、selectedTodo の description が editDescription の値で更新されること");
        }

        @Test
        @DisplayName("正常系: 有効な入力のとき、'todos?faces-redirect=true' が返ること")
        void save_validInput_returnsTodosRedirect() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("更新タイトル");
            bean.setEditDescription("更新説明");

            // When
            String result = bean.save();

            // Then
            assertEquals("todos?faces-redirect=true", result,
                    "save 成功後、'todos?faces-redirect=true' に遷移すること");
        }

        @Test
        @DisplayName("正常系: 有効な入力のとき、INFO メッセージが追加されること")
        void save_validInput_addsInfoMessage() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("更新タイトル");

            // When
            bean.save();

            // Then
            verify(mockContext, times(1))
                    .addMessage(isNull(), argThat(msg ->
                        msg.getSeverity() == FacesMessage.SEVERITY_INFO
                    ));
        }

        @Test
        @DisplayName("正常系: editDescription が null のとき、selectedTodo の description が空文字で保存されること")
        void save_nullEditDescription_savedAsEmpty() {
            // Given
            TodoItem todo = new TodoItem(1L, "タスク", "説明");
            initBeanWithTodo(todo);
            bean.setEditTitle("タスク");
            bean.setEditDescription(null);

            // When
            bean.save();

            // Then
            assertEquals("", todo.getDescription(),
                    "editDescription が null のとき selectedTodo の description は空文字で保存されること");
        }

        @Test
        @DisplayName("正常系: editTitle に前後空白があるとき、trim されて保存されること")
        void save_titleWithSpaces_isTrimmed() {
            // Given
            TodoItem todo = new TodoItem(1L, "旧タイトル", "説明");
            initBeanWithTodo(todo);
            bean.setEditTitle("  新タイトル  ");
            bean.setEditDescription("説明");

            // When
            bean.save();

            // Then
            assertEquals("新タイトル", todo.getTitle(),
                    "editTitle に前後空白がある場合、trim されて保存されること");
        }

        @Test
        @DisplayName("正常系: save 後、Flash の setKeepMessages(true) が呼ばれること")
        void save_validInput_setsKeepMessages() {
            // Given
            initBeanWithTodo(new TodoItem(1L, "タスク", "説明"));
            bean.setEditTitle("更新タイトル");

            // When
            bean.save();

            // Then
            verify(mockFlash).setKeepMessages(true);
        }
    }

    // =========================================================================
    // cancel
    // =========================================================================

    @Nested
    @DisplayName("cancel のテスト")
    class CancelTest {

        @Test
        @DisplayName("正常系: cancel は 'todos?faces-redirect=true' を返すこと")
        void cancel_returnsTodosRedirect() {
            // Given / When
            String result = bean.cancel();

            // Then
            assertEquals("todos?faces-redirect=true", result,
                    "cancel は 'todos?faces-redirect=true' を返すこと");
        }

        @Test
        @DisplayName("正常系: cancel 後、FacesContext へのメッセージ追加は行われないこと")
        void cancel_noMessageAdded() {
            // Given / When
            bean.cancel();

            // Then
            verify(mockContext, never()).addMessage(any(), any());
        }

        @Test
        @DisplayName("正常系: cancel 後も selectedTodo に変更がないこと")
        void cancel_doesNotModifySelectedTodo() {
            // Given: selectedTodo を設定する
            TodoItem todo = new TodoItem(1L, "タスク", "説明");
            initBeanWithTodo(todo);
            String originalTitle = todo.getTitle();

            // When
            bean.cancel();

            // Then: selectedTodo のタイトルが変更されていないこと
            assertEquals(originalTitle, bean.getSelectedTodo().getTitle(),
                    "cancel 後も selectedTodo のタイトルが変更されていないこと");
        }
    }

    // =========================================================================
    // Getter / Setter
    // =========================================================================

    @Nested
    @DisplayName("Getter / Setter のテスト")
    class GetterSetterTest {

        @Test
        @DisplayName("setEditTitle / getEditTitle: 設定した値が取得できること")
        void setEditTitle_getEditTitle() {
            // Given
            String expected = "編集タイトル";

            // When
            bean.setEditTitle(expected);

            // Then
            assertEquals(expected, bean.getEditTitle(),
                    "setEditTitle で設定した値が getEditTitle で取得できること");
        }

        @Test
        @DisplayName("setEditDescription / getEditDescription: 設定した値が取得できること")
        void setEditDescription_getEditDescription() {
            // Given
            String expected = "編集説明テキスト";

            // When
            bean.setEditDescription(expected);

            // Then
            assertEquals(expected, bean.getEditDescription(),
                    "setEditDescription で設定した値が getEditDescription で取得できること");
        }

        @Test
        @DisplayName("getSelectedTodo: init で設定された TodoItem が取得できること")
        void getSelectedTodo_afterInit_returnsExpectedTodo() {
            // Given
            TodoItem expected = new TodoItem(99L, "テストタスク", "テスト説明");
            initBeanWithTodo(expected);

            // When
            TodoItem actual = bean.getSelectedTodo();

            // Then
            assertNotNull(actual,
                    "init 後に getSelectedTodo は null でないこと");
            assertEquals(99L, actual.getId(),
                    "getSelectedTodo は init で Flash から取得した TodoItem を返すこと");
        }
    }
}
