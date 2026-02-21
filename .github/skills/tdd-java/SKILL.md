---
name: tdd-java
description: JUnit 5 を用いた TDD 実装ルール。デトロイト派・@Nested・@DisplayName・Given-When-Then コメント構文によるテスト実装規約を含む
---

## 1. TDD 実装ルール

- 必ず TDD で実装を行う
- **実装コードを 1 行でも書く前に、必ずテストを先に作成・実行すること**
  - テストが Red（失敗）であることをターミナルで確認してから実装を開始する
  - Red 確認前に Resource / Service / Repository の実装コードを書くことを禁止する

### TDD サイクル（必ず以下の順序を守ること）

#### フェーズ 1: Red（テストを書く）

1. テストクラスにテストメソッドを 1 件だけ追加する
2. 対応する実装クラスはまだ作成しないか、コンパイルが通る最小のスタブのみ作成する
3. 以下のコマンドでテストが **失敗（Red）** であることを必ず確認する

```powershell
mvn test 2>&1 | Select-String -Pattern "FAIL|ERROR|BUILD FAILURE"
```

#### フェーズ 2: Green（最小限の実装をする）

1. テストが通る最小限のコードを実装する
2. 以下のコマンドで **全テストが Green** であることを必ず確認する

```powershell
mvn test 2>&1 | Select-String -Pattern "Tests run.*Failures.*Errors|BUILD"
```

3. `Tests run: N, Failures: 0, Errors: 0` かつ `BUILD SUCCESS` を確認してから次へ進む

#### フェーズ 3: Refactor（リファクタリング）

1. 重複除去・責務整理・Javadoc 補完・OpenAPI アノテーション付与を行う
2. 再度 `mvn test` を実行し、Green が保たれていることを確認する
3. Refactor は省略してはならない

---

- 実装開始時は、まず**典型的な正常系テストを 1 件作成する**
  - 入力値と期待結果が最も分かりやすいケースを選択する

- 正常系テストが通過した後、境界値・異常系のテストを段階的に追加する

---

## 2. 単体テスト規約

- 単体テストは JUnit 5 を使用して実装する
  - アサーションは原則 JUnit 5（org.junit.jupiter.api.Assertions）を使用する
- オブジェクトのアサーションに限り AssertJ を使用してもよい
  - assertThat を用いた fluent API を利用する
  - equals に依存した比較は禁止する
- テスト方針はデトロイト派とする
- Resource クラスには対応する ResourceTest を実装する
- **テストクラスは Resource クラスとバリデーションクラスのみ**とする
  - Service クラスの ServiceTest は作成しない
    - Service クラスのテストは ResourceTest 内で結合して行う
  - **Repository クラスの RepositoryTest も作成しない**
    - Repository のテストも ResourceTest 内の結合テストで網羅する
  - 上記以外（Exception・DTO・その他ユーティリティ）のテストクラスも作成しない
  - 使用するテストクラスのリスト（許可）: `{画面名}ResourceTest`, `{制約名}ValidatorTest`
- Mock は 境界（外部 I/O）のみ に使用する
- @Nested を用いてテスト観点ごとにグループ化する
- テストメソッドには @DisplayName を付与し、日本語で観点を記載する
- テストメソッド名は英語で記述する
- Given-When-Then 構文を用いてテストを構成する
- アサーション失敗時に意図が分かるよう、必要に応じて message を記載する
- パラメータライズドテストは原則使用しない
  - ただしバリデーションや境界値検証では使用を許可する
