````skill
---
name: playwright-cli
description: Playwright を用いた E2E テスト実装ルール。テストサーバー連携・カバレッジ計測（c8 + V8 Native Coverage）・agent:false による接続プール問題の回避を含む。e2e-test-developer および e2e-coverage エージェントが参照する
---

## 1. テスト実装ルール

### 基本方針

- テストは画面単位でスペックファイルに分割する（1 画面 = 1 `*.spec.ts`）
- テストファイルは `tests/e2e/` 配下に配置する
- 各テストは独立して実行できるように `beforeEach` でサーバー状態をリセットする
- テスト同士は状態を共有しない（`localStorage`・サーバー状態・グローバル変数を前提にしない）
- `workers: 1` を必ず設定し、並列実行による状態競合を防ぐ

### ファイル命名規約

| 種別 | 規約 | 例 |
|---|---|---|
| スペックファイル | `{画面名}.spec.ts` | `todo-list.spec.ts` |
| テストグループ | `test.describe('画面名', ...)` | `'Todo 一覧・操作'` |
| テスト名 | 日本語で「何をしたら何になる」形式 | `'未完了 Todo をトグルすると完了状態になる'` |

### `beforeEach` パターン（必須）

テストごとにサーバー状態を初期化することで独立性を保証する。
状態種別に応じて以下の 2 パターンを使い分ける:

```typescript
// パターン1: 初期 3 件の状態にリセット（多くのテストはこれを使う）
test.beforeEach(async ({ request }) => {
  await request.post('/reset');
});

// パターン2: 全件削除（空状態のテスト用）
test.beforeEach(async ({ request }) => {
  await request.post('/clearAll');
});
```

---

## 2. テストサーバー連携

### 必須エンドポイント（テストサーバー側に実装すること）

| エンドポイント | メソッド | 役割 |
|---|---|---|
| `/reset` | POST | Todo を初期状態（3 件）にリセットする |
| `/clearAll` | POST | 全 Todo を削除する |
| `/stop` | POST | サーバーを正常終了させ、V8 カバレッジを確定させる |

### `/stop` エンドポイントの実装パターン（重要）

`NODE_V8_COVERAGE` が設定されている場合、`process.exit(0)` 時に自動的にカバレッジが書き出される。
同期呼び出しで先にレスポンスを返し、その後 `setTimeout` で終了することで接続リセット問題を防ぐ:

```javascript
if (req.method === 'POST' && path === '/stop') {
  // NODE_V8_COVERAGE が設定されている場合、process.exit で自動的にカバレッジが書き出される
  res.writeHead(200, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ ok: true }));
  setTimeout(() => process.exit(0), 500);
  return;
}
```

---

## 3. カバレッジ計測ルール（c8 + V8 Native Coverage）

### 構成概要

```
coverage-runner.cjs           ← オーケストレーター
playwright.noserver.config.ts ← Playwright 設定（webServer なし）
.c8rc                         ← c8 設定ファイル
coverage/
  tmp/                        ← V8 カバレッジ JSON の出力先
  index.html                  ← HTML レポート
```

### `.c8rc` 設定

```json
{
  "include": ["test-server.js"],
  "reporter": ["text", "html", "text-summary"],
  "temp-dir": "./coverage/tmp",
  "all": true
}
```

### `coverage-runner.cjs` の設計パターン

カバレッジ計測の正しい順序:

1. `coverage/tmp` を空にしてから起動する（古いデータを混入させない）
2. `NODE_V8_COVERAGE=<絶対パス>` を環境変数に設定してサーバーを `spawn` する
3. サーバー起動を HTTP ポーリングで待機する
4. Playwright テストを `execSync` で実行する（`playwright.noserver.config.ts` を使用）
5. `POST /stop` を `agent: false` で送信し、サーバーを正常終了させる
6. `server.on('exit')` でプロセス終了を待機する（最大 10 秒）
7. `coverage/tmp` にファイルが生成されたことを確認してから `c8 report` を実行する

### `package.json` スクリプト定義

```json
{
  "scripts": {
    "test:coverage": "node coverage-runner.cjs",
    "coverage:report": "c8 report"
  }
}
```

---

## 4. `agent: false` による keep-alive 問題回避（重要）

### 問題の背景

Node.js のデフォルト HTTP エージェントは keep-alive 接続プールを持つ。
Playwright テスト実行後、内部の HTTP 接続がプールに残ったまま失効する。
この状態で `POST /stop` を送信すると **失効した接続が再利用されて `ECONNRESET`** となり、
`/stop` ハンドラが実行されずサーバーが終了しない。

### 症状

- `coverage/tmp` が空のまま
- `server.on('exit')` が発火しない（10 秒タイムアウトになる）
- サーバーのデバッグログが `/stop` ハンドラ到達前に途絶える

### 解決策

`coverage-runner.cjs` 内の **すべての HTTP リクエスト** に `agent: false` を指定する:

```javascript
// ✅ 正しい: agent: false で新しい TCP 接続を使う
http.get({ hostname: 'localhost', port: PORT, path: '/todos', agent: false }, ...)
http.request({ hostname: 'localhost', port: PORT, path: '/stop', method: 'POST', agent: false }, ...)

// ❌ 誤り: デフォルトエージェント（接続プール再利用）
http.get('http://localhost:8080/todos', ...)
http.request({ hostname: 'localhost', port: 8080, path: '/stop', method: 'POST' }, ...)
```

### 診断方法

`/stop` ハンドラが到達されているか確認するには、サーバー側で `process.stdout.write` によるログを追加する（`console.log` ではなく同期書き込みを使う）:

```javascript
if (req.method === 'POST' && path === '/stop') {
  process.stdout.write('[/stop] HANDLER REACHED\n');
  // ...
}
```

---

## 5. `playwright.noserver.config.ts` の要件

Playwright が webServer を管理すると、テスト終了後にサーバーが終了させられてしまう。
カバレッジ計測時は `coverage-runner.cjs` がサーバーのライフサイクルを管理するため、
**`webServer` セクションを持たない** 専用設定ファイルを作成すること:

```typescript
// playwright.noserver.config.ts
// カバレッジ計測専用: webServer セクションを意図的に省略している
import { defineConfig, devices } from '@playwright/test';
export default defineConfig({
  testDir: './tests/e2e',
  workers: 1,
  use: {
    baseURL: 'http://localhost:8080',
    trace: 'on-first-retry',
  },
  projects: [
    { name: 'chromium', use: { ...devices['Desktop Chrome'] } },
  ],
});
```

---

## 6. ブランチカバレッジに関する注意

### `if (条件)` の両辺をカバーする

サーバーコードに `if (condition) { ... }` がある場合:
- `condition` が `true` になるテストケースと `false` になるテストケースの両方が必要

### 特にカバレッジが漏れやすい条件

- `null` チェック（`if (obj === null)` の null パス）
- クエリパラメータが存在しない場合（`if (query.param)` の falsy パス）
- エラーページへの直接アクセス（通常フローとは別ルート）

### ブランチカバレッジを補うテストパターン

```typescript
// 存在しないキーでアクセスしてもエラーにならないことを確認する
test('存在しない flash キーで /todos にアクセスしてもエラーにならない', async ({ page }) => {
  await page.goto('/todos?flash=nonexistent-key-xxxxx');
  await expect(page).toHaveURL('/todos?flash=nonexistent-key-xxxxx');
  // エラーにならないことを確認（ページが正常表示される）
  await expect(page.locator('h1')).toBeVisible();
});
```

---

## 7. コミット前チェックリスト

- [ ] `npm test`（通常の Playwright 実行）で全テストが通過する
- [ ] `npm run test:coverage` で `coverage/tmp` にファイルが生成される
- [ ] `c8 report` で `Statements`, `Branches`, `Functions`, `Lines` の全項目が目標カバレッジ以上
- [ ] `coverage/` ディレクトリが `.gitignore` に登録されている
- [ ] テストヘルパーエンドポイント（`/reset`, `/clearAll`, `/stop`）が `test-server.js` に実装されている
- [ ] `playwright.noserver.config.ts` が存在する
- [ ] `coverage-runner.cjs` の HTTP リクエストに `agent: false` が設定されている
````
