---
name: jqwik-property-testing
description: jqwik を用いたプロパティベーステスト実装規約。pom.xml 設定・プロパティ設計手法・アービトラリー設定・終了基準を含む
---

## 1. pom.xml 設定

```xml
<!-- jqwik（プロパティベーステスト） -->
<dependency>
  <groupId>net.jqwik</groupId>
  <artifactId>jqwik</artifactId>
  <version>1.8.4</version>
  <scope>test</scope>
</dependency>
```

> **バージョン互換性**: `jqwik 1.8.x` は JUnit Platform 1.10.x（JUnit 5.10.x に同梱）に対応

### Maven Surefire 設定（jqwik と JUnit 5 を共存させる）

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.5</version>
  <configuration>
    <includes>
      <include>**/*Test.java</include>
      <include>**/*Properties.java</include>
    </includes>
  </configuration>
</plugin>
```

---

## 2. プロパティベーステストとは

ユニットテストが「具体的な入力値に対して期待する出力値を検証する」のに対し、プロパティベーステストは「**任意の入力値に対して成立するべき性質（プロパティ）を検証する**」手法。

### jqwik がランダムに生成・試みるもの

- 任意の文字列（空文字・null・特殊文字・非常に長い文字列を含む）
- 任意の数値（最小値・最大値・负数・0 を含む）
- 任意のリスト（空リスト・要素数の多いリストを含む）

---

## 3. テストクラス設計規約

### ファイル命名

| 対象クラス | プロパティテストクラス名 |
|---|---|
| `FooService` | `FooServiceProperties` |
| `FooValidator` | `FooValidatorProperties` |

### 基本構造

```java
import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

@Label("FooService プロパティテスト")
class FooServiceProperties {

    @Property
    @Label("名前が有効な文字列の場合、結果の名前は入力と一致する")
    void nameRoundTrip(@ForAll @StringLength(min = 1, max = 100) String name) {
        // Given
        var input = new CreateFooRequest(name);

        // When
        var actual = fooService.create(input);

        // Then
        assertThat(actual.getName()).as("名前が入力値と一致すること").isEqualTo(name);
    }
}
```

---

## 4. プロパティ設計手法

### 設計すべきプロパティの分類

| 分類 | 説明 | 例 |
|---|---|---|
| **往復変換（Round-trip）** | 変換→逆変換で元に戻る | encode→decode, serialize→deserialize |
| **冪等性（Idempotent）** | 同じ操作を繰り返しても結果が変わらない | 同じ ID で 2 回検索しても同じ結果 |
| **不変条件（Invariant）** | 操作後も変わらない性質 | 追加後のリストサイズ > 追加前のサイズ |
| **対称性（Symmetry）** | 順序を変えても同じ結果になる | `a + b == b + a` |
| **事前条件 → 事後条件** | 入力が条件を満たす場合、出力が保証される | 正の数を入力→正の数が返る |

### 設計すべき優先度

1. **必ず設計する**: バリデーション系メソッド（`@NotBlank`・`@Size` 等の制約検証）
2. **設計を検討する**: ビジネスロジック（算術・文字列変換・ソート等）
3. **設計不要**: 単純な CRUD（データの保存・取得のみ）

---

## 5. アービトラリー（Arbitrary）の設定

### 組み込みアービトラリー

```java
// 文字列
@ForAll @StringLength(min = 1, max = 100) String name

// 空文字を含む文字列
@ForAll String anyString   // null は含まない

// 数値
@ForAll @IntRange(min = 1, max = 1000) int positiveInt

// ASCII 文字列のみ
@ForAll @Chars({'a'-'z', 'A'-'Z', '0'-'9'}) @StringLength(max = 50) String alphanumeric

// null を含む文字列
@ForAll @WithNull(probability = 0.1) String nullableString
```

### カスタムアービトラリー

```java
@Provide
Arbitrary<CreateFooRequest> validRequests() {
    Arbitrary<String> names = Arbitraries.strings()
        .withCharRange('a', 'z')
        .ofMinLength(1)
        .ofMaxLength(100);

    return names.map(name -> new CreateFooRequest(name));
}

@Property
void createdFooHasValidName(@ForAll("validRequests") CreateFooRequest request) {
    var result = fooService.create(request);
    assertThat(result.getName()).isNotBlank();
}
```

---

## 6. プロパティテストの終了基準

### 数値目標

| 指標 | 目標 | 説明 |
|---|---|---|
| **全 `@Property` が PASSED** | 必須 | デフォルトで各プロパティに 1000 ケース試行する |
| 試行回数 | デフォルト 1000 回 | `@Property(tries = N)` で変更可能 |
| 失敗ケースのシュリンク | 最小反例を確認 | jqwik が自動でシュリンクする |

### ループ終了条件

以下の**いずれか**を満たした場合、プロパティテストのループを終了してよい:

1. **全 `@Property` が PASSED** である
2. 「実施しても無意味なケース」として以下の条件を**すべて満たす**:
   - テスト対象メソッドが単純なデータ移送のみ（変換・計算ロジックなし）
   - JUnit5 テストで既にすべての境界値・等価クラスを網羅済み
   - テストレビューエージェント（フェーズ 8）から「プロパティテスト不要」の承認を得ている

---

## 7. 失敗時の対応手順

### ステップ 1: 最小反例の確認

jqwik は失敗時に「Shrinking」を実行し、最小の失敗入力値を出力する。

```
Falsified with sample {
  name = ""   ← 最小反例（この入力で失敗した）
}
```

### ステップ 2: 原因分析

| 原因 | 対処 |
|---|---|
| プロダクションコードのバグ | プロダクションコードを修正する |
| テストのプロパティが間違っている | プロパティ定義を見直す |
| アービトラリーの範囲が広すぎる | `@ForAll` の制約を実際のビジネス要件に合わせる |

### ステップ 3: 修正後の再実行

```powershell
mvn test "-Dtest=FooServiceProperties"
```

---

## 8. jqwik と JUnit 5 の共存

- `@Property` アノテーションを使用するメソッドは **`@Test` と共存できない**（同じメソッドに両方付与しない）
- jqwik のプロパティテストクラスと JUnit 5 のテストクラスは**別クラスに分離する**
- `@Label` は `@DisplayName` の jqwik 版である

---

## 9. コミット前チェックリスト

- [ ] `mvn test "-Dtest={プロパティテストクラス名}"` で全 `@Property` が PASSED である
- [ ] 各 `@Property` に `@Label`（日本語の観点説明）が付与されている
- [ ] 失敗が発生した場合、プロダクションコードを修正して再 PASSED を確認した
- [ ] 「実施しても無意味」と判断したケースはテストレビューエージェントの承認を得ている
