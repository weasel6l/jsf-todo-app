---
name: junit5-testing
description: JUnit 5 を用いた JSF コードへのテスト実装規約。デトロイト派・@Nested・@DisplayName・Given-When-Then コメント構文によるテスト実装規約・依存関係設定・実行コマンドを含む
---

## 1. pom.xml 依存関係

### 必須バージョン（互換性確認済み）

```xml
<!-- JUnit 5 -->
<dependency>
  <groupId>org.junit.jupiter</groupId>
  <artifactId>junit-jupiter</artifactId>
  <version>5.10.2</version>
  <scope>test</scope>
</dependency>

<!-- AssertJ（オブジェクトアサーション用・任意） -->
<dependency>
  <groupId>org.assertj</groupId>
  <artifactId>assertj-core</artifactId>
  <version>3.25.3</version>
  <scope>test</scope>
</dependency>

<!-- Mockito（境界のみ使用） -->
<dependency>
  <groupId>org.mockito</groupId>
  <artifactId>mockito-junit-jupiter</artifactId>
  <version>5.11.0</version>
  <scope>test</scope>
</dependency>
```

### Maven Surefire プラグイン設定（JUnit 5 実行に必須）

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-surefire-plugin</artifactId>
  <version>3.2.5</version>
  <configuration>
    <includes>
      <include>**/*Test.java</include>
    </includes>
  </configuration>
</plugin>
```

---

## 2. テスト実行コマンド

### 特定テストクラスの実行（推奨）

```powershell
# 単一クラス
mvn test "-Dtest=FooServiceTest"

# 複数クラス（PowerShell では引用符必須）
mvn test "-Dtest=FooServiceTest,BarServiceTest"
```

### 全テスト実行

```powershell
mvn test
```

### 結果確認コマンド

```powershell
# Red 確認（失敗を期待する場合）
mvn test "-Dtest={テストクラス名}" 2>&1 | Select-String -Pattern "FAIL|ERROR|BUILD FAILURE"

# Green 確認
mvn test "-Dtest={テストクラス名}" 2>&1 | Select-String -Pattern "Tests run.*Failures.*Errors|BUILD"
```

> **重要**: `Tests run: N, Failures: 0, Errors: 0` かつ `BUILD SUCCESS` を確認してから次へ進む

---

## 3. TDD サイクル

**必ず以下の順序を守ること。まとめて実装・まとめてテスト追加は禁止する。**

### フェーズ 1: Red（テストを書く）

1. テストクラスにテストメソッドを **1 件だけ** 追加する
2. 対応する実装クラスはスタブとして作成する
   - スタブ本体: `throw new UnsupportedOperationException("not implemented");`
   - コンパイルエラーの状態も Red とみなす
3. テストが **失敗（Red）** であることを確認する

### フェーズ 2: Green（最小限の実装）

1. テストが通る **最小限** のコードを実装する
2. `Tests run: N, Failures: 0, Errors: 0` と `BUILD SUCCESS` を確認する

### フェーズ 3: Refactor（リファクタリング）

1. 重複除去・責務整理を行う
2. Javadoc・コーディング規約に準拠させる
3. 再度テストを実行し Green が保たれていることを確認する
4. **Refactor は省略不可**

---

## 4. テストクラス設計規約

### テスト対象クラス

| 対象 | テストクラス名 | 備考 |
|---|---|---|
| Service クラス | `{ServiceName}Test` | 境界（Repository 等）は Mock |
| Validator クラス | `{ValidatorName}Test` | 独立実行可能なバリデーションロジック |
| Utility クラス | `{UtilityName}Test` | 純粋関数のみ対象 |

> **JSF Backing Bean / Model / XHTML はテスト対象外とする**（既存 JSF コードは変更しない）

### Mock 使用方針

- Mock は **境界（外部 I/O・データソース）のみ** に使用する
- Service 内部のロジックは実オブジェクトを使用してテストする（デトロイト派）
- `@ExtendWith(MockitoExtension.class)` を使用する

---

## 5. テスト記述規約

### 構造

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("FooService")
class FooServiceTest {

    @Mock
    private FooRepository fooRepository;

    @InjectMocks
    private FooService fooService;

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("存在する ID を指定した場合、対応するエンティティを返す")
        void returnsEntityWhenIdExists() {
            // Given
            var expected = new Foo(1L, "テスト");
            when(fooRepository.findById(1L)).thenReturn(Optional.of(expected));

            // When
            var actual = fooService.findById(1L);

            // Then
            assertThat(actual).isPresent();
            assertThat(actual.get().getName()).as("名前が一致すること").isEqualTo("テスト");
        }
    }
}
```

### 規約一覧

| 規約 | 内容 |
|---|---|
| テスト方針 | デトロイト派（実オブジェクト優先） |
| グループ化 | `@Nested` でテスト観点ごとに分類する |
| メソッド名 | 英語で記述する |
| 日本語説明 | `@DisplayName` に日本語で観点を記載する |
| Javadoc | 「前提条件・期待する事後条件」を記述する |
| Given-When-Then | コメントで構造を明示する |
| アサーションメッセージ | 必ず記載する（失敗時の理由が分かるように） |
| JUnit アサーション | `assertEquals(expected, actual, "メッセージ")` の形式（メッセージは最後） |
| AssertJ | `assertThat(actual).as("メッセージ").isEqualTo(expected)` の形式 |
| パラメータライズドテスト | 原則使用しない |

### @DisplayName 記述例

```java
// Good
@DisplayName("名前が空文字の場合、バリデーションエラーが発生する")

// Bad（英語・曖昧）
@DisplayName("testNameEmpty")
```

### アサーション記述例

```java
// JUnit 5
assertEquals("期待値", actual, "名前が一致すること");
assertThrows(IllegalArgumentException.class, () -> service.process(null), "null 入力で例外が発生すること");

// AssertJ（オブジェクト比較）
assertThat(actual)
    .as("取得したユーザーの名前が一致すること")
    .extracting(User::getName)
    .isEqualTo("田中太郎");
```

---

## 6. テスト分類と実装順序

各テスト対象に対して、以下の順に実装する:

### 1. 正常系（ハッピーパス）
- 最初に実装する
- 代表的なデータで処理が正常に通るケース
- データが複数件・0 件のケース

### 2. 境界値
- 正常系通過後に実装する
- 文字数制限の上限値（`@Size(max=N)` → N 文字ちょうど）
- 数値範囲の上下限値

### 3. 異常系・エラー系
- 最後に実装する
- バリデーションエラー（null・空文字・上限超過等）
- 存在しない ID 参照（NoSuchElementException 等）
- 業務例外

---

## 7. コミット前チェックリスト

- [ ] 全テストメソッドに `@DisplayName`（日本語）が付与されている
- [ ] 全テストメソッドに Javadoc（前提条件・事後条件）が記述されている
- [ ] 各 `@Nested` クラスに `@DisplayName` が付与されている
- [ ] Given-When-Then コメントが存在する
- [ ] 全アサーションにメッセージが記述されている
- [ ] `mvn test` ですべてのテストが Green であることを確認した
- [ ] テスト内に「テスト方針・デトロイト派」等の実装方針コメントが含まれていない
