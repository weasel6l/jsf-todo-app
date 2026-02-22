---
name: api-implementation
description: Helidon MP を用いた REST API の実装ルール。コーディング規約・Javadoc 規約・バリデーション規約を含む。JSFからのマイグレーション時のバックエンド API 実装に使用する。
---

## 1. コーディング規約

- import にはシングルクラスインポートを使用する
  - ワイルドカードインポート（*）は禁止する

- マイグレーション対象の実装は、
  **外部仕様としての振る舞いが基となる JSF 実装と同一であることを最優先とする**
  - 入出力
  - エラー条件
  - 画面・API 利用者から見た結果

- 上記の振る舞いを維持できる範囲で、
  クラスは単一責任の原則に則って分割する

- 1 つのメソッドの行数（Javadoc を含まない）は
  **30 行以下を目安**として実装する
  - 行数の超過は「設計見直しのサイン」として扱う
  - 可読性や責務の不自然な分割を招く場合は、この限りではない

- 全てのテキストファイルは改行（newline）で終わること
  - Java ファイル・設定ファイル・その他すべてのテキストファイルが対象
  - ファイルの最終行の末尾に改行がない場合、コミット対象から除外して修正すること
  - PowerShell で `[System.IO.File]::WriteAllText` を使う場合、**文字列末尾に必ず `\n` を付けること**

> **警告（PowerShell here-string の落とし穴）**: `@'...'@` 形式の here-string は
> `'@` の直前の改行を含まない。コンテンツの末尾に明示的に `\n` を追加しないと
> 末尾改行がつかない。
> 正しい書き方: 文字列末尾の `}` の後に空行または `\n` を追加する。
>
> **NG**: `$c = @'\n...}\n'@`  （`'@`直前に内容がない行 = 改行なし）
>
> **OK**: `$c = @'\n...}\n\n'@`  （最後の`}`の後に改行が1つある）

#### 末尾改行の検証（コミット前に必ず実行する）

新規作成・変更したすべての Java ファイルについて、以下のコマンドで末尾バイトを検証すること。
`LastByte=10`（LF）でない場合は修正してからコミットすること。

```powershell
# 単一ファイルの確認
$b = [System.IO.File]::ReadAllBytes("path\to\File.java")
"LastByte=" + $b[$b.Length-1] + " (10=OK/LF, other=NG)"
```

---

### ファイル規約

#### エンコーディング

- 新規作成するすべての Java ファイルは **UTF-8 BOM なし** で保存すること
  - BOM あり（UTF-8 with BOM）は Java コンパイラがエラーとして扱う場合があるため禁止する
  - PowerShell で `Set-Content` を使う場合は `-Encoding UTF8` を **使用しない**
  - 代わりに `[System.Text.UTF8Encoding]::new($false)` と `[System.IO.File]::WriteAllText` の組み合わせを使用する

#### Copyright ヘッダー

- すべての新規 Java ソースファイルの先頭には、以下の形式で Copyright ヘッダーを記載すること

```java
/*
 * Copyright (c) 2026 Your Company Name
 * All rights reserved.
 */
```

- ヘッダーは `package` 宣言より前に配置する
- 既存ファイルへの遡及的な追加は不要とする（新規作成ファイルのみ対象）

#### Copyright ヘッダーの検証（コミット前）

- 新規作成した Java ファイルは、先頭が Copyright ヘッダーで始まることを確認する
- 少なくとも以下を目視で確認する
  - 1 行目が `/*`
  - `Copyright (c) 2026 Your Company Name` を含む
  - `package` 宣言より前に記載されている

---

### Lombok 利用規約

- 本プロジェクトでは、ボイラープレートコード削減を目的として
  Lombok の利用を **推奨** する

- Lombok は以下の用途で使用してよい
  - コンストラクタ生成
  - getter / setter の生成
  - equals / hashCode / toString の生成
  - DTO・値オブジェクトの簡潔な定義

- Lombok の利用により
  **処理の流れや副作用が読み取りにくくなる場合は使用してはならない**

#### 使用を推奨するアノテーション例

- `@RequiredArgsConstructor`
- `@AllArgsConstructor`
- `@Getter`
- `@Setter`（DTO に限る）
- `@EqualsAndHashCode`
- `@ToString`

#### 使用を制限または禁止するアノテーション

- `@Data`
  - 暗黙に setter / equals / toString が生成され、責務が不明瞭になるため原則使用しない
- `@Builder`
  - DTO 以外での使用は禁止する（Service / Entity では使用不可）

---

### 依存性注入（DI）規約

- 依存性注入（DI）は **コンストラクタインジェクションのみを使用する**
  - フィールドインジェクションは禁止する
  - セッターインジェクションは禁止する

- DI 対象クラスでは、
  Lombok のコンストラクタ生成アノテーションを使用し、
  **クラス単位でコンストラクタインジェクションを明示する**

- Lombok を使用する場合の基本ルールは以下とする
  - 不変な依存関係は `final` フィールドとして定義する
  - `@RequiredArgsConstructor` を基本とする
  - 全フィールド注入が必要な場合のみ `@AllArgsConstructor` を使用してよい

- コンストラクタには `@Inject` を付与し、
  CDI による依存性注入を明示すること

- Lombok のコンストラクタ生成を使う場合は、以下のいずれかで `@Inject` を明示する
  - `onConstructor_ = @Inject` を利用する
  - 明示的にコンストラクタを実装して `@Inject` を付与する

#### CDI プロキシ要件と Lombok の組み合わせ（重要）

CDI はランタイムにプロキシクラスを生成するため、
**管理 Bean（`@RequestScoped` / `@ApplicationScoped` 等）には引数なしコンストラクタが必須**。

`@RequiredArgsConstructor` で `final` フィールドを持つクラスに引数なしコンストラクタを追加する場合、
**`@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)` を必ず使用すること**。
`force = true` がないと `final` フィールドが初期化されないとしてコンパイルエラーになる。

```java
// OK: CDI プロキシ要件を満たすパターン
@ApplicationScoped
@RequiredArgsConstructor(onConstructor_ = @Inject)
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class TodoListService {
    private final TodoRepository repository;
}
```

```java
// NG: force = true がないと final フィールドの初期化エラーでコンパイル失敗
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // コンパイルエラー
```

---

## 2. Javadoc 規約

- クラス、メソッド、メンバには必ず Javadoc を記載する
  - テストクラスやテストメソッドも例外ではない

- 使用可能な Javadoc タグは以下のみに限定する  
  - `@param`  
  - `@return`  
  - `@throws`

- 記述はプレーンテキストを基本とし、HTML タグの使用は禁止する

- 文体は常体とし、敬語は使用しない
- **Javadoc の文末に句点（。）を付けてはならない**
  - 「した。」「保持する。」「返す。」のような書き方は禁止する
  - コミット前に以下のコマンドで0件であることを必ず確認する

    ```powershell
    # Javadoc 行末の句点を検出する (0 件 = OK)
    # src/main と src/test の両方を対象にする
    Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-String -Pattern "\*\s.*\u3002\s*$" | Where-Object { $_.Line -match '^\s*\*' }
    Get-ChildItem -Path "src\test\java" -Filter "*.java" -Recurse | Select-String -Pattern "\*\s.*\u3002\s*$" | Where-Object { $_.Line -match '^\s*\*' }
    ```

- 実装内容の説明ではなく、
  「責務」「前提条件」「事後条件」を記載すること
  - テスト方針・実装方式（「デトロイト派」「実オブジェクトを使用」等）も記載しない
  - このような情報はスキル文書・設計文書で管理すること

- Javadoc コメントは `/**` で開始すること  
  - `/* ... */` 形式のコメントは使用しない

- Javadoc コメントは必ず複数行で記載すること  
  - 1 行のみの Javadoc は禁止する
  - **この規則はフィールド・メンバ変数にも例外なく適用する**
    - `/** 説明 */` のようなフィールドの 1 行 Javadoc も違反である

### 記載例

```java
/**
 * OK: 複数行で記載する
 * 説明は簡潔かつ明確に書く
 */
```

```java
/** NG: 1行で記載しない */
```

```java
/**
 * OK: フィールドも複数行で記載する
 */
private final TodoRepository repository;
```

```java
/** NG: フィールドの1行Javadocも禁止 */
private final TodoRepository repository;
```

### コミット前の自己チェック

- コミット前に以下のパターンで 1 行 Javadoc が残っていないことを確認すること
  - 検索パターン（正規表現）: `/\*\* .+ \*/`
  - 新規作成・変更したファイルに対して上記パターンで検索し、**0 件**であることを確認してからコミットする

- Javadoc に HTML タグが含まれていないことを確認すること

  ```powershell
  # Javadoc 内の HTML タグを検出する (0 件 = OK)
  # src/main と src/test の両方を対象にする
  Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | Select-String -Pattern '<[a-z][a-z0-9]*'
  Get-ChildItem -Path "src\test\java" -Filter "*.java" -Recurse | Select-String -Pattern '<[a-z][a-z0-9]*'
  ```

---

## 3. バリデーション規約

- リクエストパラメータおよびDTOには、Jakarta Bean Validation（Helidon MPがサポートする仕様）の標準アノテーションを使用する
  - 例：@NotNull, @Size, @Pattern, @Email など

- 標準アノテーションのみでは適切なバリデーションが実現できない場合、
  カスタム制約アノテーション（@Constraint）を定義する

- カスタム制約を実装する場合は、以下を必須とする
  - 制約アノテーションの定義
  - ConstraintValidator の実装クラス
  - 明確なエラーメッセージ定義

- 単なる if 文による Service 層での入力チェックは禁止する
  - 入力値の妥当性検証は原則 Bean Validation で行う

- カスタム ConstraintValidator を実装した場合、
  そのバリデーションロジックに対する **1対1の単体テストクラスを必ず作成する**

- API エンドポイントでは @Valid を必ず使用し、
  Bean Validation を有効化すること

---

## 4. OpenAPI アノテーション規約

> MicroProfile OpenAPI アノテーション（`org.eclipse.microprofile.openapi.annotations`）を使用すること
> Swagger アノテーション（`io.swagger.core.v3`）は使用しない

- REST API の **すべてのエンドポイントに OpenAPI アノテーションを必ず付与する**
  - アノテーションなしでの Resource クラスのコミットは禁止する
  - 「あとで追加する」は認めない。実装と同時に付与すること

- DTO には **`@Schema` アノテーションを必ず付与する**
  - アノテーションなしでの DTO クラスのコミットは禁止する
  - クラスレベル・フィールドレベルの両方に付与すること

### アプリケーション全体の OpenAPI 定義

- JAX-RS `Application` クラスに `@OpenAPIDefinition` を付与し、API 全体のメタ情報を定義する
- プロジェクトにつき **1 箇所のみ** 定義する

| アノテーション | パッケージ | 付与対象 |
|---|---|---|
| `@OpenAPIDefinition` | `org.eclipse.microprofile.openapi.annotations` | `Application` クラス：`info`（タイトル・バージョン・説明）を指定する |
| `@Info` | `org.eclipse.microprofile.openapi.annotations.info` | `@OpenAPIDefinition` の `info` 属性：`title`・`version`・`description` |

```java
@OpenAPIDefinition(
    info = @Info(
        title = "Todo API",
        version = "1.0.0",
        description = "JSF Todo アプリのマイグレーション API"
    )
)
@ApplicationScoped
@ApplicationPath("/api")
public class TodoApplication extends Application { }
```

### Resource クラスの必須アノテーション一覧

| アノテーション | パッケージ | 付与対象 |
|---|---|---|
| `@Tag` | `org.eclipse.microprofile.openapi.annotations.tags` | Resource クラス：API グループ名（`name`）と説明（`description`） |
| `@Operation` | `org.eclipse.microprofile.openapi.annotations` | 各エンドポイントメソッド：`summary`（1行説明）と `description`（詳細） |
| `@APIResponse` / `@APIResponses` | `org.eclipse.microprofile.openapi.annotations.responses` | 各エンドポイントメソッド：HTTP ステータスコード・説明・レスポンススキーマ |
| `@Content` | `org.eclipse.microprofile.openapi.annotations.media` | `@APIResponse` / `@RequestBody` 内：`mediaType` と `schema` の指定 |
| `@Parameter` | `org.eclipse.microprofile.openapi.annotations.parameters` | `@PathParam` / `@QueryParam` 引数：パラメータの説明 |
| `@RequestBody` | `org.eclipse.microprofile.openapi.annotations.parameters` | `@POST` / `@PUT` のリクエスト引数：説明・必須指定・リクエストスキーマ |

> **注意**: MicroProfile OpenAPI では `@APIResponse` / `@APIResponses`（大文字 API）を使用する
> Swagger の `@ApiResponse` / `@ApiResponses`（小文字 pi）とは異なるため注意すること

#### `@APIResponse` での `@Content` 使用ルール

- **2xx 系レスポンス**には `content` 属性で `@Content` を付与し、レスポンススキーマを明示すること
- **4xx / 5xx 系レスポンス**（エラーレスポンス）では `content` は省略してよい

#### `@RequestBody` での `@Content` 使用ルール

- `@RequestBody` には `content` 属性で `@Content` を付与し、リクエストスキーマを明示すること
- `required = true` を明示すること

#### 記述例（Resource クラス）

```java
@Tag(name = "Todo一覧", description = "Todo 一覧画面の API")
@Path("/api/todo/list")
@RequestScoped
public class TodoListResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Todo 一覧取得",
        description = "登録されているすべての Todo を一覧で返却する"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "200",
            description = "取得成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoListResponse.class)
            )
        )
    })
    public TodoListResponse getList() { ... }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(
        summary = "Todo 追加",
        description = "新しい Todo を追加する"
    )
    @APIResponses({
        @APIResponse(
            responseCode = "201",
            description = "追加成功",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TodoAddResponse.class)
            )
        ),
        @APIResponse(
            responseCode = "400",
            description = "バリデーションエラー"
        )
    })
    public Response addTodo(
        @RequestBody(
            description = "Todo 追加リクエスト",
            required = true,
            content = @Content(
                schema = @Schema(implementation = TodoAddRequest.class)
            )
        )
        @Valid TodoAddRequest request
    ) { ... }
}
```

### DTO クラスの必須アノテーション一覧

| アノテーション | パッケージ | 付与対象 |
|---|---|---|
| `@Schema(description = "...")` | `org.eclipse.microprofile.openapi.annotations.media` | DTO クラス：DTO 全体の役割説明 |
| `@Schema(description = "...", example = "...")` | 同上 | 各フィールド：フィールドの意味と値の例 |
| `@Schema(..., required = true)` | 同上 | 必須フィールド：入力必須であることを明示する |
| `@Schema(..., maxLength = N)` | 同上 | 文字列フィールド：バリデーション上限に合わせた最大長 |

#### 記述例（リクエスト DTO）

```java
@Schema(description = "Todo 追加リクエスト")
public class TodoAddRequest {

    @Schema(description = "Todo のタイトル",
            required = true,
            maxLength = 100,
            example = "牛乳を買う")
    @NotBlank
    private String title;

    @Schema(description = "Todo の説明",
            maxLength = 500,
            example = "スーパーで購入する")
    private String description;
}
```

#### 記述例（レスポンス DTO）

```java
@Schema(description = "Todo アイテムのレスポンス")
public class TodoItemResponse {

    @Schema(description = "Todo の一意識別子", example = "1")
    private Long id;

    @Schema(description = "完了状態フラグ。true の場合は完了済み", example = "false")
    private boolean completed;
}
```

### コミット前の自己チェック

#### Resource クラス

- [ ] Resource クラスに `@Tag` が付与されているか
- [ ] すべてのエンドポイントメソッドに `@Operation(summary = ...)` が付与されているか
- [ ] すべてのエンドポイントメソッドに `@APIResponses` が付与されているか（`@ApiResponses` ではなく `@APIResponses`）
- [ ] 2xx 系 `@APIResponse` に `content = @Content(schema = @Schema(implementation = ...))` が付与されているか
- [ ] `@PathParam` / `@QueryParam` 引数に `@Parameter` が付与されているか
- [ ] `@POST` / `@PUT` のリクエスト引数に `@RequestBody` が付与されているか
- [ ] `@RequestBody` に `content = @Content(schema = @Schema(implementation = ...))` が付与されているか

#### DTO クラス

- [ ] DTO クラスに `@Schema(description = "...")` が付与されているか
- [ ] すべてのフィールドに `@Schema(description = "...", example = "...")` が付与されているか
- [ ] 必須フィールドに `required = true` が付与されているか（`requiredMode = Schema.RequiredMode.REQUIRED` ではなく）
- [ ] 文字列フィールドに `maxLength` がバリデーション制約と一致して付与されているか
