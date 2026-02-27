---
name: mutation-testing-java
description: JUnit5・Jacoco・jqwik・PIT を組み合わせたミューテーションテスト実施ルール。JSF バックエンドの既存コードに対してテストを実装し、カバレッジ 100% → ミューテーションスコア 100% を達成させる手順を含む
---

## 1. 全体フロー

```
JUnit5 テスト実装
     ↓
Jacoco でカバレッジ 100% を確認
     ↓
PIT でミューテーションテストを実行
     ↓
Survived mutant が存在する場合
     ↓ ← ここをループ
jqwik でプロパティベーステストを追加
     ↓
PIT を再実行 → Survived = 0 なら完了
```

---

## 2. 作業開始前チェックリスト

以下をすべて確認してから作業を開始すること

- [ ] Java 11 以上が使用されていること
- [ ] `pom.xml` に以下の依存関係が追加されていること

  ```xml
  <!-- JUnit 5 -->
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.2</version>
    <scope>test</scope>
  </dependency>

  <!-- jqwik (プロパティベーステスト) -->
  <dependency>
    <groupId>net.jqwik</groupId>
    <artifactId>jqwik</artifactId>
    <version>1.8.4</version>
    <scope>test</scope>
  </dependency>
  ```

- [ ] `pom.xml` に JaCoCo Maven Plugin が設定されていること

  ```xml
  <plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
      <execution>
        <id>prepare-agent</id>
        <goals><goal>prepare-agent</goal></goals>
      </execution>
      <execution>
        <id>report</id>
        <phase>test</phase>
        <goals><goal>report</goal></goals>
      </execution>
    </executions>
  </plugin>
  ```

- [ ] `pom.xml` に PIT Maven Plugin が設定されていること

  ```xml
  <plugin>
    <groupId>org.pitest</groupId>
    <artifactId>pitest-maven</artifactId>
    <version>1.16.1</version>
    <dependencies>
      <!-- JUnit 5 サポート -->
      <dependency>
        <groupId>org.pitest</groupId>
        <artifactId>pitest-junit5-plugin</artifactId>
        <version>1.2.1</version>
      </dependency>
    </dependencies>
    <configuration>
      <!-- テスト対象クラスのパッケージを指定する -->
      <targetClasses>
        <param>com.example.*</param>
      </targetClasses>
      <targetTests>
        <param>com.example.*Test</param>
      </targetTests>
      <mutators>
        <mutator>STRONGER</mutator>
      </mutators>
      <outputFormats>
        <outputFormat>HTML</outputFormat>
        <outputFormat>XML</outputFormat>
      </outputFormats>
      <timestampedReports>false</timestampedReports>
    </configuration>
  </plugin>
  ```

- [ ] `maven-surefire-plugin` が JUnit 5 エンジンを認識できる設定になっていること

  ```xml
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
  </plugin>
  ```

---

## 3. JUnit5 テスト規約（ミューテーションテスト対応版）

`tdd-java` スキルの規約に加え、ミューテーションテストの耐性を高めるために以下を追加で厳守する

### 3-1. アサーションの厳密化

- **境界値は必ず両方向でテストする**
  - 例: `max=100` のバリデーションなら、`100`（有効）と `101`（無効）の両方をテストする
  - 境界値の片方しかテストしない場合、境界値ミュータント（± 1 演算子の変化）を生き残らせる原因になる
- **複数フィールドを持つオブジェクトは、全フィールドをアサートする**
  - `assertEquals(expected.getId(), actual.getId(), ...)` のように特定フィールドのみをアサートしないこと
  - AssertJ の `usingRecursiveComparison()` を使用するか、全フィールドを個別にアサートする
- **ブール値を返すメソッドは、`true` になるケースと `false` になるケースを両方テストする**
- **条件分岐（if/else・三項演算子）は、全パスを通るテストを書く**

### 3-2. 同値クラスの選定を厳密にする

- 同値クラスの「代表値」として中間値だけでなく、以下を選ぶこと:
  - 最小値・最大値・ゼロ・null・空文字列・空リスト
  - 負数・正数・境界値そのもの

### 3-3. 戻り値の検証を省略しない

- `void` メソッドの場合、副作用（モックの呼び出し・状態変化）を必ずアサートする
- `verify()` だけでなく、`ArgumentCaptor` や状態確認で実際の値を検証する

---

## 4. JaCoCo カバレッジ確認手順

### ステップ 1: テスト実行とレポート生成

```powershell
mvn clean test
```

`BUILD SUCCESS` を確認する。`target/site/jacoco/` にレポートが生成される

### ステップ 2: カバレッジの確認（HTML レポート）

```powershell
# レポートファイルの存在確認
Test-Path "target/site/jacoco/index.html"
```

HTML レポートを確認する場合:
```powershell
Start-Process "target/site/jacoco/index.html"
```

### ステップ 3: XML レポートによる数値確認

```powershell
# 行カバレッジとブランチカバレッジを確認
Select-Xml -Path "target/site/jacoco/jacoco.xml" `
  -XPath "//counter[@type='LINE']" |
  ForEach-Object { $_.Node }

Select-Xml -Path "target/site/jacoco/jacoco.xml" `
  -XPath "//counter[@type='BRANCH']" |
  ForEach-Object { $_.Node }
```

`missed="0"` が確認できれば 100% 達成

### Jacoco だけでは不十分なケース

- 行カバレッジ 100% でも、アサーションが甘ければミュータントは生き残る
- カバレッジ 100% は **ミューテーションテストの前提条件** であり、目標ではない
- カバレッジ 100% 達成後に必ず PIT でミューテーションテストを実行すること

---

## 5. jqwik プロパティベーステスト規約

### 5-1. 使用場面

以下のケースで jqwik の `@Property` テストを使用する:

- ミュータントを生き残らせた **条件分岐・演算子** のカバー
- バリデーションロジックの全域テスト（「任意の有効値で成功する」「任意の無効値で失敗する」）
- 境界値の網羅が難しいメソッド（文字列長・数値範囲）

### 5-2. 基本構文

```java
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

class ExamplePropertyTest {

    /**
     * 任意の正数を入力してもバリデーションが通ること
     */
    @Property(tries = 100)
    void anyPositiveAmountIsValid(
            @ForAll @Positive int amount) {
        // Given
        var request = new CreateRequest(amount);

        // When
        var violations = validator.validate(request);

        // Then
        assertThat(violations)
            .as("正数はバリデーションを通過すること")
            .isEmpty();
    }

    /**
     * 任意の負数はバリデーションエラーになること
     */
    @Property(tries = 100)
    void anyNegativeAmountIsInvalid(
            @ForAll @Negative int amount) {
        // Given
        var request = new CreateRequest(amount);

        // When
        var violations = validator.validate(request);

        // Then
        assertThat(violations)
            .as("負数はバリデーションエラーになること")
            .isNotEmpty();
    }
}
```

### 5-3. 主要なアノテーション

| アノテーション | 説明 |
|---|---|
| `@Property` | プロパティテストとしてマークする。`tries` で試行回数を指定（デフォルト 1000） |
| `@ForAll` | 引数をランダム生成する |
| `@Positive` | 正数のみを生成する |
| `@Negative` | 負数のみを生成する |
| `@IntRange(min=1, max=100)` | 指定範囲の整数を生成する |
| `@StringLength(min=1, max=100)` | 指定文字数の文字列を生成する |
| `@NotEmpty` | 空でない値を生成する |
| `@AlphaChars` | アルファベットのみを生成する |
| `@NumericChars` | 数字のみを生成する |

### 5-4. カスタム Arbitrary の定義

複雑なドメインオブジェクトをランダム生成する場合:

```java
@Provide
Arbitrary<CreateRequest> validRequests() {
    return Combinators.combine(
        Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(100),
        Arbitraries.integers().between(1, Integer.MAX_VALUE)
    ).as(CreateRequest::new);
}

@Property
void validRequestAlwaysSucceeds(@ForAll("validRequests") CreateRequest req) {
    // ...
}
```

### 5-5. jqwik と JUnit5 の共存設定

`src/test/resources/junit-platform.properties` に以下を追加する:

```properties
junit.jupiter.extensions.autodetection.enabled=true
```

---

## 6. PIT ミューテーションテスト実行手順

### ステップ 1: ミューテーションテストの実行

```powershell
mvn org.pitest:pitest-maven:mutationCoverage
```

実行時間は対象クラス数・テスト数に応じて数分〜数十分かかる

特定クラスのみを対象にする場合:
```powershell
mvn org.pitest:pitest-maven:mutationCoverage `
  "-DtargetClasses=com.example.todo.resource.TodoResource" `
  "-DtargetTests=com.example.todo.resource.TodoResourceTest"
```

### ステップ 2: レポートの確認

レポートは `target/pit-reports/` に生成される:

```powershell
# レポートディレクトリの確認
Get-ChildItem "target/pit-reports/" -Recurse | Where-Object { $_.Name -eq "index.html" }
```

HTML レポートを開く:
```powershell
Start-Process "target/pit-reports/index.html"
```

XML レポートで数値を確認する:

```powershell
# XML レポートのパース（mutations.xml）
[xml]$xml = Get-Content "target/pit-reports/mutations.xml"
$survived = $xml.mutations.mutation | Where-Object { $_.status -eq "SURVIVED" }
Write-Host "Survived mutants: $($survived.Count)"
```

### ステップ 3: Survived Mutant の解析

生き残ったミュータントに対して以下を確認する:

| 確認項目 | 確認方法 |
|---|---|
| どのクラス・行のミュータントか | `mutatedClass`・`lineNumber` |
| どの変換が行われたか | `mutator` タグ（例: `CONDITIONALS_BOUNDARY`） |
| 既存テストで通過したか否か | `killingTest` が空ならば生き残り |

### ステップ 4: 主要なミュータントの種類と対策

| ミュータント | 内容 | 対策 |
|---|---|---|
| `CONDITIONALS_BOUNDARY` | `<` を `<=` に変換など | 境界値の両端をテストする |
| `NEGATE_CONDITIONALS` | `==` を `!=` に変換など | true/false 両方のパスをアサートする |
| `MATH` | `+` を `-` に変換など | 演算結果の具体値をアサートする |
| `INCREMENTS` | `i++` を `i--` に変換など | 増加・減少後の値を確認する |
| `VOID_METHOD_CALLS` | void メソッド呼び出しを削除 | verify や状態変化を必ずアサートする |
| `RETURN_VALS` | 戻り値を変更する | 返却値の全フィールドをアサートする |
| `NULL_RETURNS` | null を返す | null チェックのテストを追加する |
| `EMPTY_RETURNS` | 空コレクション・空文字を返す | 0 件と 1 件以上のケースをテストする |

---

## 7. ミューテーションスコア 100% 達成の手順

### 7-1. Survived Mutant が残った場合の対処フロー

1. Survived Mutant の `mutatedClass`・`lineNumber`・`mutator` を確認する
2. 該当するソースコードの行を確認し、どのテストケースが不足しているかを推論する
3. 不足しているテストケースを JUnit5 または jqwik で追加する
4. `mvn org.pitest:pitest-maven:mutationCoverage` を再実行して確認する
5. Survived = 0 になるまで繰り返す

### 7-2. jqwik を使うべきタイミング

- 境界値ミュータント（`CONDITIONALS_BOUNDARY`）を複数の境界で生き残らせている場合
- バリデーションロジックで「任意の有効値は通過」「任意の無効値は失敗」の証明が必要な場合
- ループ・コレクション処理で要素数のバリエーションを網羅したい場合

### 7-3. 等価ミュータント（Equivalent Mutant）

以下のケースは **等価ミュータント** であり、理論上テストで殺すことができない:

- 条件の書き換えが実行時の動作に影響しないケース（到達不能コード）
- フレームワークの内部実装に依存する部分

等価ミュータントが確認された場合は **ユーザーに報告し、対応方針を確認する**

### 7-4. 完了条件

```
Survived Mutants: 0
Mutation Coverage: 100%
```

---

## 8. PIT 対象クラスの除外設定

JSF の Backing Bean・Model・DTO など、テスト対象外のクラスは `pom.xml` の `excludedClasses` で除外する:

```xml
<configuration>
  <targetClasses>
    <param>com.example.todo.resource.*</param>
    <param>com.example.todo.service.*</param>
    <param>com.example.todo.repository.*</param>
  </targetClasses>
  <excludedClasses>
    <param>com.example.todo.bean.*</param>
    <param>com.example.todo.model.*</param>
    <param>com.example.todo.dto.*</param>
  </excludedClasses>
  <targetTests>
    <param>com.example.todo.resource.*Test</param>
  </targetTests>
</configuration>
```

---

## 9. トラブルシューティング

### PIT が「No mutations found」を返す場合

- `targetClasses` のパッケージ名が正しいことを確認する
- `target/classes/` が存在することを確認する（`mvn compile -q` を実行する）
- `pitest-junit5-plugin` が依存関係に含まれているかを確認する

### PIT 実行が非常に遅い場合

- 特定クラスのみを対象に絞る（`-DtargetClasses=...`）
- `threads` を設定して並列実行する:

  ```xml
  <threads>4</threads>
  ```

### jqwik の `@Property` が JUnit 5 として認識されない場合

- `src/test/resources/junit-platform.properties` に `junit.jupiter.extensions.autodetection.enabled=true` が設定されているか確認する
- `junit-platform-launcher` が classpathに含まれているか確認する
