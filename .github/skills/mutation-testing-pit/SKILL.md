---
name: mutation-testing-pit
description: PIT（Pitest）を用いたミューテーションテスト実施規約。pom.xml 設定・実行コマンド・結果確認・改善手順・終了基準の数値定義を含む
---

## 1. pom.xml 設定

### PIT Maven プラグイン

```xml
<plugin>
  <groupId>org.pitest</groupId>
  <artifactId>pitest-maven</artifactId>
  <version>1.15.3</version>
  <dependencies>
    <!-- JUnit 5 サポート -->
    <dependency>
      <groupId>org.pitest</groupId>
      <artifactId>pitest-junit5-plugin</artifactId>
      <version>1.2.1</version>
    </dependency>
  </dependencies>
  <configuration>
    <!-- テスト対象クラスのパターン（既存 JSF コードを除外） -->
    <targetClasses>
      <param>com.example.*.service.*</param>
      <param>com.example.*.validator.*</param>
      <param>com.example.*.util.*</param>
    </targetClasses>
    <!-- テストクラスのパターン -->
    <targetTests>
      <param>com.example.*.*Test</param>
    </targetTests>
    <!-- ミューテーター（適用する変異の種類） -->
    <mutators>
      <mutator>DEFAULTS</mutator>
    </mutators>
    <!-- HTML レポート出力先 -->
    <outputFormats>
      <outputFormat>HTML</outputFormat>
      <outputFormat>XML</outputFormat>
    </outputFormats>
    <!-- タイムアウト設定（ミリ秒） -->
    <timeoutConstant>1000</timeoutConstant>
    <!-- 最大生存ミュータント数のしきい値 -->
    <mutationThreshold>80</mutationThreshold>
    <!-- カバレッジしきい値 -->
    <coverageThreshold>90</coverageThreshold>
  </configuration>
</plugin>
```

> **バージョン互換性**: `pitest-junit5-plugin 1.2.1` は JUnit 5.10.x に対応。`pitest-maven 1.15.x` は Java 17 以上を推奨

---

## 2. 実行コマンド

### ミューテーションテスト実行

```powershell
# 全ミューテーションテストを実行
mvn test-compile org.pitest:pitest-maven:mutationCoverage

# 特定クラスのみ対象（高速化）
mvn test-compile org.pitest:pitest-maven:mutationCoverage "-Dpit.targetClasses=com.example.service.FooService"
```

### レポート確認

```powershell
# HTML レポートをブラウザで確認
Start-Process "target/pit-reports/{タイムスタンプ}/index.html"

# 最新レポートをワイルドカードで検索
Get-ChildItem "target/pit-reports" | Sort-Object LastWriteTime -Descending | Select-Object -First 1 | ForEach-Object { Start-Process "$($_.FullName)/index.html" }
```

---

## 3. ミューテーションスコアの目標と終了基準

### 数値目標

| 指標 | 目標値 | 説明 |
|---|---|---|
| **Mutation Score** | **≥ 80%** | Killed ÷ Total（Survived + Killed）× 100 |
| **Line Coverage** | ≥ 90% | PIT が計測したライン カバレッジ |

> **Mutation Score の計算式**: `Killed / (Survived + Killed) × 100`
> - `NO_COVERAGE`（テストが存在しない）のミュータントは分母から除外する
> - `TIMED_OUT` は Killed としてカウントする

### ループ終了条件

以下の**いずれか**を満たした場合、ミューテーションテストのループを終了してよい:

1. **Mutation Score が 80% 以上** である
2. **生存ミュータント（Survived）が 0 件** である（理想状態）
3. 残存している生存ミュータントが「等価ミュータント（Equivalent Mutant）」であることを確認できた

### 等価ミュータントと判断する基準

以下のすべてを満たす場合に限り、そのミュータントを等価ミュータントと判断してよい:

| 条件 | 説明 |
|---|---|
| ミュータントを Kill するテストが**論理的に存在しない** | 例: `i++` → `i--` の変異で、その変数が戻り値に影響しない場合 |
| 仕様上意味のある差異が生じない | プログラムの外部から観測可能な振る舞いが変わらない |
| テストレビューエージェントが等価ミュータントとして承認した | フェーズ 8 での承認が必要 |

---

## 4. 結果の読み方

### ミューテーションの状態一覧

| 状態 | 意味 | 対応 |
|---|---|---|
| **KILLED** | テストがミュータントを検出できた | 良好。対応不要 |
| **SURVIVED** | テストがミュータントを検出できなかった | テスト追加が必要 |
| **NO_COVERAGE** | テストが存在しない | テストを追加する（JaCoCo カバレッジ改善と共通） |
| **TIMED_OUT** | 実行がタイムアウトした | Killed として扱う |
| **NON_VIABLE** | コンパイル不能な変異 | 無視してよい |

---

## 5. Survived ミュータントの改善手順

### ステップ 1: Survived ミュータントの確認

HTML レポート（`target/pit-reports/*/index.html`）を開き、以下を確認する:
- どのクラス・メソッドのミュータントが生存しているか
- 変異の内容（どのコードをどう変えたか）

### ステップ 2: 原因分析

```
例）変異: return true → return false
原因: 戻り値が true/false どちらでも同じテスト結果になっている
対応: 戻り値 false の場合の動作を検証するテストを追加する
```

### ステップ 3: テストケース追加

`junit5-testing` スキルに従い、当該ミュータントを Kill できるテストケースを追加する。

### ステップ 4: 再実行と確認

```powershell
mvn test-compile org.pitest:pitest-maven:mutationCoverage
```

Mutation Score が目標値（≥ 80%）を満たしていることを確認する。

---

## 6. よくある Survived ミュータントのパターン

| ミュータント変異 | 原因 | 対処 |
|---|---|---|
| 条件反転 (`if (a > b)` → `if (a >= b)`) | 境界値テストが不足 | 境界値ちょうどのテストケースを追加 |
| 戻り値変更 (`return true` → `return false`) | 正常系のみテストしている | 条件が false になるケースのテストを追加 |
| 算術演算子変更 (`+` → `-`) | 計算結果の数値テストが不足 | 具体値で検証するアサーションを追加 |
| null 返却 (`return obj` → `return null`) | null 安全チェックが不足 | null の場合の挙動を検証するテストを追加 |

---

## 7. コミット前チェックリスト

- [ ] `mvn test-compile org.pitest:pitest-maven:mutationCoverage` が完了した
- [ ] Mutation Score が 80% 以上である（または等価ミュータントとして承認を受けている）
- [ ] HTML レポートで生存ミュータントを確認し、対応方針を記録した
- [ ] JSF 既存コードが `targetClasses` から除外されている
