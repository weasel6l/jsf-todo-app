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

---

## 2. Javadoc 規約

- クラスおよびメソッド、メンバには必ず Javadoc を記載する

- 使用可能なタグは以下のみとする
  - @param
  - @return
  - @throws

- プレーンテキストを基本とし、HTML タグの使用は禁止する

- 敬語は使用しない
- 文末に句点を付けない
- /** で始まる Javadoc コメントを使用すること（/* */ は使用しない）

- 実装内容の説明ではなく、
  「責務」「前提条件」「事後条件」を記載すること

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
