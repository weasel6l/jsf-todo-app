# JSF Todo App

JSF (JavaServer Faces) 2.3 と **FlashContainer (Flash スコープ)** を活用した Todo アプリケーションです。

## ブランチ構成

| ブランチ | 役割 |
|---|---|
| `main` | 本番リリース用ブランチ |
| `develop` | 開発統合ブランチ |
| `feature/jsf-todo-app` | 本機能の実装ブランチ |

## 技術スタック

- **JSF 2.3** (Mojarra 実装)
- **CDI 2.0** (Weld)
- **Maven** ビルドツール
- **Java 11**
- **Tomcat 9** (推奨デプロイ先)

## プロジェクト構成

```
src/
└── main/
    ├── java/com/example/todo/
    │   ├── model/
    │   │   └── TodoItem.java          # モデルクラス (Serializable)
    │   └── bean/
    │       ├── TodoBean.java          # SessionScoped: リスト管理 + FlashContainer への格納
    │       └── TodoDetailBean.java    # ViewScoped: FlashContainer からのデータ取得
    └── webapp/
        ├── WEB-INF/
        │   ├── web.xml
        │   ├── faces-config.xml
        │   └── beans.xml
        ├── resources/css/
        │   └── todo.css
        ├── index.xhtml                # リダイレクト
        ├── todos.xhtml                # Todo 一覧ページ
        └── detail.xhtml              # Todo 詳細・編集ページ
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
| Todo 追加 | タイトル・説明を入力して追加 |
| 完了切り替え | ボタン一押しで完了/未完了を切り替え |
| 詳細・編集 | FlashContainer でデータを渡し詳細ページで編集 |
| 削除 | 確認ダイアログ付き削除 |
| 統計表示 | 合計・完了・未完了のカウントをリアルタイム表示 |
