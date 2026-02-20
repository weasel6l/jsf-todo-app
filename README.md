# JSF Todo App

JSF (JavaServer Faces) 2.3 と **FlashContainer (Flash スコープ)** を活用した Todo アプリケーションです。  
PRG (Post-Redirect-Get) パターンによる状態管理と、Playwright による E2E テストを備えています。

## ブランチ構成

| ブランチ | 役割 |
|---|---|
| `main` | 本番リリース用ブランチ |
| `develop` | 開発統合ブランチ |
| `feature/jsf-todo-app` | 本機能の実装ブランチ |

## 技術スタック

### バックエンド
- **Java 11**
- **JSF 2.3** (Mojarra 実装)
- **CDI 2.0** (Weld 3.1.9)
- **Maven** ビルドツール
- **Tomcat 9** (推奨デプロイ先)

### フロントエンド
- **Facelets** (XHTML テンプレート)
- **CSS3** (カスタムスタイル)

### テスト
- **Playwright** (E2E テスト)
- **Node.js** テストサーバー (JSF/Tomcat の代替モック)

## プロジェクト構成

```
jsf-todo-app/
├── pom.xml                            # Maven ビルド設定 (JSF 2.3, CDI 2.0, Weld)
├── package.json                       # Node.js / Playwright 依存関係
├── playwright.config.ts               # Playwright E2E テスト設定
├── test-server.js                     # E2E テスト用モックサーバー (Node.js)
├── src/
│   └── main/
│       ├── java/com/example/todo/
│       │   ├── model/
│       │   │   └── TodoItem.java      # モデルクラス (id, title, description, completed, createdAt)
│       │   └── bean/
│       │       ├── TodoBean.java      # @SessionScoped: リスト管理 + FlashContainer への格納
│       │       └── TodoDetailBean.java # @ViewScoped: FlashContainer からのデータ取得・編集
│       └── webapp/
│           ├── WEB-INF/
│           │   ├── web.xml            # FacesServlet, セッション設定, Weld リスナー
│           │   ├── faces-config.xml
│           │   └── beans.xml         # CDI 有効化
│           ├── resources/css/
│           │   └── todo.css          # カスタムスタイル
│           ├── index.xhtml            # ルート (todos.xhtml へリダイレクト)
│           ├── todos.xhtml            # Todo 一覧ページ (追加・完了切替・削除)
│           └── detail.xhtml          # Todo 詳細・編集ページ
└── tests/
    └── e2e/
        └── todo-add.spec.ts          # Playwright E2E テスト (正常系: Todo 追加)
```

## FlashContainer を使った状態管理

### 概要

JSF の **Flash スコープ** (`javax.faces.context.Flash`) は、
**POST → Redirect → GET (PRG)** パターンを跨いでデータを保持できるスコープです。
リクエストスコープより少し長く、セッションスコープよりも短命です。

```
[todos.xhtml]
   ↓  「詳細・編集」ボタンをクリック
TodoBean#viewDetail(item)
   ↓  flash.put("selectedTodo", item)  ← Flash に格納
   ↓  return "detail?faces-redirect=true"  ← リダイレクト発生
[HTTP 302 Redirect]
   ↓
[detail.xhtml]
TodoDetailBean#init()
   ↓  flash.get("selectedTodo")  ← Flash から取得
   ↓  selectedTodo, editTitle, editDescription を初期化
```

### TodoBean.java での格納

```java
public String viewDetail(TodoItem item) {
    ExternalContext externalContext =
            FacesContext.getCurrentInstance().getExternalContext();
    Flash flash = externalContext.getFlash();

    flash.setKeepMessages(true);      // メッセージも保持
    flash.put("selectedTodo", item);  // 選択 Todo を格納

    return "detail?faces-redirect=true";
}
```

### TodoDetailBean.java での取得

```java
@PostConstruct
public void init() {
    ExternalContext externalContext =
            FacesContext.getCurrentInstance().getExternalContext();
    Flash flash = externalContext.getFlash();

    // FlashContainer から TodoItem を取得
    selectedTodo = (TodoItem) flash.get("selectedTodo");

    if (selectedTodo != null) {
        editTitle       = selectedTodo.getTitle();
        editDescription = selectedTodo.getDescription();
    }
}
```

## 起動方法

### Maven + Tomcat プラグインで実行

```bash
mvn tomcat7:run
```

ブラウザで `http://localhost:8080/jsf-todo-app/index.xhtml` にアクセスしてください。

### WAR ファイルをビルドして Tomcat にデプロイ

```bash
mvn clean package
# target/jsf-todo-app-1.0-SNAPSHOT.war を Tomcat の webapps/ にコピー
```

## 機能一覧

| 機能 | 説明 |
|---|---|
| Todo 追加 | タイトル・説明を入力して追加。追加後は PRG パターンでリダイレクトし、Flash 経由の成功メッセージを表示 |
| 完了切り替え | ボタン一押しで完了/未完了を切り替え |
| 詳細・編集 | FlashContainer でデータを渡し詳細ページで編集 |
| 削除 | 確認ダイアログ付き削除 |
| 統計表示 | 合計・完了・未完了のカウントをリアルタイム表示 |

## 単体テスト (JUnit 5)

### テスト構成

| テストクラス | 対象クラス | テスト件数 |
|---|---|---|
| `TodoItemTest` | `TodoItem` | 17 件 |
| `TodoBeanTest` | `TodoBean` | 35 件 |
| `TodoDetailBeanTest` | `TodoDetailBean` | 24 件 |

### 実行方法

```bash
# すべての単体テストを実行する
mvn test

# テスト結果は target/surefire-reports/ に出力される
```

### テスト設計方針

- **Gherkin 記法** (`// Given` / `// When` / `// Then`) でテストシナリオを明示
- **`@DisplayName`** でテスト観点を日本語で記述
- **`@Nested`** クラスで機能ごとにブロック分け
- アサーションにはすべて **失敗時メッセージ** を付与
- `FacesContext.getCurrentInstance()` などの static メソッドは **Mockito `mockStatic`** でモック

---

## E2E テスト (Playwright)

### 概要

[Playwright](https://playwright.dev/) を使った E2E テストを `tests/e2e/` に配置しています。  
実際の JSF/Tomcat 環境を使わず、`test-server.js`（Node.js 製モックサーバー）を自動起動してテストを実行します。

### テストサーバー (`test-server.js`)

- ポート **8080** で起動する軽量 HTTP サーバー
- インメモリで Todo データを管理（初期データ 3 件）
- JSF がレンダリングした後の HTML を模擬し、Playwright からアクセス可能
- `playwright.config.ts` の `webServer` 設定により、`npm test` 実行時に自動起動・停止

### テストシナリオ

| ファイル | 内容 |
|---|---|
| `tests/e2e/todo-add.spec.ts` | Todo 新規追加の正常系テスト (PRG パターン + Flash メッセージ検証) |

**`todo-add.spec.ts` のテストステップ:**

1. Todo 一覧ページ (`/todos`) を開く
2. 追加前の統計バッジ（合計・未完了・完了件数）を記録
3. タイトルと説明を入力して「追加する」ボタンをクリック
4. POST → Redirect → GET 後のページを検証
   - Flash スコープ経由のフラッシュメッセージが表示されること
   - 追加した Todo がリストに表示されること
   - 統計バッジの「合計」が 1 増加していること
   - 新しい Todo の状態が「未完了」であること

### テスト実行方法

```bash
# 依存パッケージのインストール (初回のみ)
npm install
npx playwright install chromium

# テストをヘッドレスで実行
npm test

# テストを UI モードで実行 (デバッグ時)
npm run test:ui

# テストレポートを表示
npm run test:report
```

> `npm test` を実行すると `test-server.js` が自動起動し、テスト完了後に自動停止します。
