import { test, expect } from '@playwright/test';

/**
 * JSF Todo App - 正常系 E2E テスト
 *
 * 対象機能: Todo 新規追加 (FlashContainer によるリダイレクト後メッセージ表示)
 *
 * テストシナリオ:
 *   1. Todo 一覧ページを開く
 *   2. 初期件数を記録する
 *   3. タイトルと説明を入力して「追加する」をクリック
 *   4. POST → Redirect → GET (PRG パターン) 後のページを検証
 *      a. FlashContainer 経由のフラッシュメッセージが表示されること
 *      b. 追加した Todo がリストに表示されること
 *      c. 統計バッジの「合計」が 1 増加していること
 *      d. 新しい Todo のタイトル・説明文が正しく表示されること
 *      e. 新しい Todo の状態が「未完了」であること
 */
test.describe('Todo 新規追加 - 正常系', () => {

  /**
   * 正常系テスト: タイトルと説明を入力して Todo を追加できる
   *
   * JSF の FlashContainer (Flash スコープ) を用いた PRG パターンを検証する。
   * TodoBean#addTodo() 実行後のリダイレクト先でフラッシュメッセージが
   * 正しく引き継がれていることを確認する。
   */
  test('タイトルと説明を入力して Todo を追加でき、フラッシュメッセージが表示される', async ({ page }) => {

    // ── Step 1: Todo 一覧ページを開く ──────────────────────────────
    await page.goto('/todos');

    // ページタイトルを確認
    await expect(page).toHaveTitle('JSF Todo App - 一覧');

    // ── Step 2: 追加前の統計バッジを取得する ───────────────────────
    const totalBadge     = page.locator('.stat-badge.total');
    const pendingBadge   = page.locator('.stat-badge.pending');
    const completedBadge = page.locator('.stat-badge.completed');

    // 統計バッジが表示されていることを確認
    await expect(totalBadge).toBeVisible();
    await expect(pendingBadge).toBeVisible();
    await expect(completedBadge).toBeVisible();

    // 追加前の合計件数テキストを取得 (例: "合計: 3")
    const totalTextBefore = await totalBadge.innerText();
    const totalBefore = parseInt(totalTextBefore.replace(/[^0-9]/g, ''), 10);

    // 追加前の未完了件数を取得
    const pendingTextBefore = await pendingBadge.innerText();
    const pendingBefore = parseInt(pendingTextBefore.replace(/[^0-9]/g, ''), 10);

    // ── Step 3: Todo 追加フォームに入力する ────────────────────────
    const testTitle       = `E2E テスト用 Todo ${Date.now()}`;
    const testDescription = 'Playwright MCP を使って実装した正常系テストの説明文';

    // タイトルを入力
    const titleInput = page.getByRole('textbox', { name: 'タイトル *' });
    await expect(titleInput).toBeVisible();
    await titleInput.fill(testTitle);
    await expect(titleInput).toHaveValue(testTitle);

    // 説明を入力
    const descInput = page.getByRole('textbox', { name: '説明（任意）' });
    await expect(descInput).toBeVisible();
    await descInput.fill(testDescription);
    await expect(descInput).toHaveValue(testDescription);

    // ── Step 4: 「追加する」ボタンをクリックする ────────────────────
    const addButton = page.getByRole('button', { name: '追加する' });
    await expect(addButton).toBeVisible();
    await addButton.click();

    // ── Step 5: リダイレクト後のページを検証する ────────────────────

    // 5-a. PRG パターンによりリダイレクトされた URL になっていること
    //      (flash クエリパラメータが付与されるのは FlashContainer 実装の証拠)
    await expect(page).toHaveURL(/\/todos/);

    // 5-b. FlashContainer 経由のフラッシュメッセージが表示されること
    //      JSF Flash スコープはリダイレクトを跨いでメッセージを保持する
    const flashMessage = page.locator('.messages');
    await expect(flashMessage).toBeVisible();
    await expect(flashMessage).toContainText('Todoを追加しました');

    // 5-c. 追加した Todo がリストに表示されること
    const todoList = page.locator('.todo-item');
    const todoTitles = todoList.locator('.todo-title');
    // 追加したタイトルが一覧に存在する
    await expect(todoTitles.filter({ hasText: testTitle })).toBeVisible();

    // 5-d. 説明文が正しく表示されること
    const addedItem = todoList.filter({ hasText: testTitle });
    await expect(addedItem).toBeVisible();
    await expect(addedItem.locator('.todo-desc')).toContainText(testDescription);

    // 5-e. 新しい Todo の状態が「未完了」であること
    //      完了トグルボタンの aria-label で確認
    const toggleButton = addedItem.getByRole('button', { name: '未完了' });
    await expect(toggleButton).toBeVisible();

    // 5-f. 「詳細・編集」リンクが表示されること
    await expect(addedItem.getByRole('link', { name: '詳細・編集' })).toBeVisible();

    // 5-g. 「削除」ボタンが表示されること
    await expect(addedItem.getByRole('button', { name: '削除' })).toBeVisible();

    // 5-h. 統計バッジの「合計」が 1 増加していること
    const totalTextAfter = await totalBadge.innerText();
    const totalAfter = parseInt(totalTextAfter.replace(/[^0-9]/g, ''), 10);
    expect(totalAfter).toBe(totalBefore + 1);

    // 5-i. 統計バッジの「未完了」が 1 増加していること (新規追加は必ず未完了)
    const pendingTextAfter = await pendingBadge.innerText();
    const pendingAfter = parseInt(pendingTextAfter.replace(/[^0-9]/g, ''), 10);
    expect(pendingAfter).toBe(pendingBefore + 1);

    // 5-j. 「完了」件数は変わっていないこと
    const completedTextBefore = await completedBadge.innerText();
    // リダイレクト後にも同じ値であることを確認
    await expect(completedBadge).toHaveText(completedTextBefore);
  });
});
