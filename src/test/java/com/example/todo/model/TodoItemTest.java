package com.example.todo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link TodoItem} モデルクラスの単体テスト。
 *
 * <p>テスト観点:</p>
 * <ul>
 *   <li>コンストラクタによる初期化の正確性</li>
 *   <li>各 Getter / Setter の入出力</li>
 *   <li>{@code getFormattedCreatedAt} のフォーマット仕様</li>
 * </ul>
 */
@DisplayName("TodoItem モデルクラスのテスト")
class TodoItemTest {

    // =========================================================================
    // コンストラクタ
    // =========================================================================

    @Nested
    @DisplayName("コンストラクタのテスト")
    class ConstructorTest {

        @Test
        @DisplayName("デフォルトコンストラクタ: id が null で生成される")
        void defaultConstructor_idIsNull() {
            // Given: 引数なしコンストラクタで TodoItem を生成する
            // When
            TodoItem item = new TodoItem();

            // Then: id は null であること
            assertNull(item.getId(),
                    "デフォルトコンストラクタで生成した場合、id は null であること");
        }

        @Test
        @DisplayName("デフォルトコンストラクタ: createdAt が null でない (現在時刻で初期化される)")
        void defaultConstructor_createdAtIsNotNull() {
            // Given: テスト実行直前の時刻を記録する
            LocalDateTime before = LocalDateTime.now().minusSeconds(1);

            // When
            TodoItem item = new TodoItem();
            LocalDateTime after = LocalDateTime.now().plusSeconds(1);

            // Then: createdAt が生成前後の時刻の間にあること
            assertNotNull(item.getCreatedAt(),
                    "デフォルトコンストラクタで生成した場合、createdAt は null でないこと");
            assertTrue(item.getCreatedAt().isAfter(before),
                    "createdAt はコンストラクタ呼び出し前より後の時刻であること");
            assertTrue(item.getCreatedAt().isBefore(after),
                    "createdAt はコンストラクタ呼び出し後より前の時刻であること");
        }

        @Test
        @DisplayName("デフォルトコンストラクタ: completed が false で初期化される")
        void defaultConstructor_completedIsFalse() {
            // Given / When
            TodoItem item = new TodoItem();

            // Then
            assertFalse(item.isCompleted(),
                    "デフォルトコンストラクタで生成した場合、completed は false であること");
        }

        @Test
        @DisplayName("パラメータコンストラクタ: id が指定した値で設定される")
        void paramConstructor_idIsSet() {
            // Given
            Long expectedId = 42L;

            // When
            TodoItem item = new TodoItem(expectedId, "テスト", "説明");

            // Then
            assertEquals(expectedId, item.getId(),
                    "パラメータコンストラクタで id が正しく設定されること");
        }

        @Test
        @DisplayName("パラメータコンストラクタ: title が指定した値で設定される")
        void paramConstructor_titleIsSet() {
            // Given
            String expectedTitle = "買い物をする";

            // When
            TodoItem item = new TodoItem(1L, expectedTitle, "メモ");

            // Then
            assertEquals(expectedTitle, item.getTitle(),
                    "パラメータコンストラクタで title が正しく設定されること");
        }

        @Test
        @DisplayName("パラメータコンストラクタ: description が指定した値で設定される")
        void paramConstructor_descriptionIsSet() {
            // Given
            String expectedDesc = "スーパーで食材を購入する";

            // When
            TodoItem item = new TodoItem(1L, "買い物", expectedDesc);

            // Then
            assertEquals(expectedDesc, item.getDescription(),
                    "パラメータコンストラクタで description が正しく設定されること");
        }

        @Test
        @DisplayName("パラメータコンストラクタ: completed は false で初期化される")
        void paramConstructor_completedIsFalse() {
            // Given / When
            TodoItem item = new TodoItem(1L, "タスク", "説明");

            // Then
            assertFalse(item.isCompleted(),
                    "パラメータコンストラクタで completed は false で初期化されること");
        }

        @Test
        @DisplayName("パラメータコンストラクタ: createdAt が null でない")
        void paramConstructor_createdAtIsNotNull() {
            // Given / When
            TodoItem item = new TodoItem(1L, "タスク", "説明");

            // Then
            assertNotNull(item.getCreatedAt(),
                    "パラメータコンストラクタで createdAt は null でないこと");
        }
    }

    // =========================================================================
    // Getter / Setter
    // =========================================================================

    @Nested
    @DisplayName("Getter / Setter のテスト")
    class GetterSetterTest {

        @Test
        @DisplayName("setId / getId: 設定した値が取得できること")
        void setId_getId() {
            // Given
            TodoItem item = new TodoItem();
            Long expected = 99L;

            // When
            item.setId(expected);

            // Then
            assertEquals(expected, item.getId(),
                    "setId で設定した値が getId で取得できること");
        }

        @Test
        @DisplayName("setTitle / getTitle: 設定した値が取得できること")
        void setTitle_getTitle() {
            // Given
            TodoItem item = new TodoItem();
            String expected = "新しいタスク";

            // When
            item.setTitle(expected);

            // Then
            assertEquals(expected, item.getTitle(),
                    "setTitle で設定した値が getTitle で取得できること");
        }

        @Test
        @DisplayName("setDescription / getDescription: 設定した値が取得できること")
        void setDescription_getDescription() {
            // Given
            TodoItem item = new TodoItem();
            String expected = "詳細説明テキスト";

            // When
            item.setDescription(expected);

            // Then
            assertEquals(expected, item.getDescription(),
                    "setDescription で設定した値が getDescription で取得できること");
        }

        @Test
        @DisplayName("setCompleted(true) / isCompleted: true が返ること")
        void setCompleted_true() {
            // Given
            TodoItem item = new TodoItem();

            // When
            item.setCompleted(true);

            // Then
            assertTrue(item.isCompleted(),
                    "setCompleted(true) 後に isCompleted が true を返すこと");
        }

        @Test
        @DisplayName("setCompleted(false) / isCompleted: false が返ること")
        void setCompleted_false() {
            // Given
            TodoItem item = new TodoItem();
            item.setCompleted(true);

            // When
            item.setCompleted(false);

            // Then
            assertFalse(item.isCompleted(),
                    "setCompleted(false) 後に isCompleted が false を返すこと");
        }

        @Test
        @DisplayName("setCreatedAt / getCreatedAt: 設定した値が取得できること")
        void setCreatedAt_getCreatedAt() {
            // Given
            TodoItem item = new TodoItem();
            LocalDateTime expected = LocalDateTime.of(2025, 6, 15, 10, 30);

            // When
            item.setCreatedAt(expected);

            // Then
            assertEquals(expected, item.getCreatedAt(),
                    "setCreatedAt で設定した値が getCreatedAt で取得できること");
        }
    }

    // =========================================================================
    // getFormattedCreatedAt
    // =========================================================================

    @Nested
    @DisplayName("getFormattedCreatedAt のテスト")
    class FormattedCreatedAtTest {

        @Test
        @DisplayName("正常系: createdAt が設定されている場合、yyyy/MM/dd HH:mm 形式で返ること")
        void formattedCreatedAt_returnsFormattedString() {
            // Given: 2025年6月15日 10時30分 の日時を持つ TodoItem
            TodoItem item = new TodoItem();
            item.setCreatedAt(LocalDateTime.of(2025, 6, 15, 10, 30));

            // When
            String formatted = item.getFormattedCreatedAt();

            // Then: フォーマット "yyyy/MM/dd HH:mm" で返ること
            assertEquals("2025/06/15 10:30", formatted,
                    "getFormattedCreatedAt は 'yyyy/MM/dd HH:mm' 形式の文字列を返すこと");
        }

        @Test
        @DisplayName("正常系: createdAt が null の場合、空文字を返ること")
        void formattedCreatedAt_nullCreatedAt_returnsEmpty() {
            // Given: createdAt を null に設定した TodoItem
            TodoItem item = new TodoItem();
            item.setCreatedAt(null);

            // When
            String formatted = item.getFormattedCreatedAt();

            // Then: 空文字が返ること
            assertEquals("", formatted,
                    "createdAt が null の場合、getFormattedCreatedAt は空文字を返すこと");
        }

        @Test
        @DisplayName("正常系: 月・日・時・分が 1 桁の場合でもゼロ埋めされること")
        void formattedCreatedAt_zeroPadding() {
            // Given: 1桁の月日時分 (2025年1月5日 9時5分)
            TodoItem item = new TodoItem();
            item.setCreatedAt(LocalDateTime.of(2025, 1, 5, 9, 5));

            // When
            String formatted = item.getFormattedCreatedAt();

            // Then: ゼロ埋めされた文字列が返ること
            assertEquals("2025/01/05 09:05", formatted,
                    "月・日・時・分が 1 桁の場合もゼロ埋めされること");
        }
    }
}
