---
name: jacoco-coverage
description: JaCoCo を用いたコードカバレッジ計測・改善規約。pom.xml 設定・レポート生成コマンド・カバレッジ確認方法・改善手順を含む
---

## 1. pom.xml 設定

### JaCoCo Maven プラグイン

```xml
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.11</version>
  <executions>
    <!-- prepare-agent: JVM エージェントの設定 -->
    <execution>
      <id>prepare-agent</id>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <!-- report: テスト実行後にレポートを生成 -->
    <execution>
      <id>report</id>
      <phase>verify</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <!-- check: カバレッジ基準を下回った場合にビルド失敗 -->
    <execution>
      <id>check</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>BUNDLE</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>1.00</minimum>
              </limit>
              <limit>
                <counter>BRANCH</counter>
                <value>COVEREDRATIO</value>
                <minimum>1.00</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### 除外設定（JSF Backing Bean・Model 等を除外）

```xml
<configuration>
  <excludes>
    <!-- JSF 既存コード（テスト対象外） -->
    <exclude>**/bean/**</exclude>
    <exclude>**/model/**</exclude>
    <!-- DTO・Exception クラス（純粋データ保持クラス） -->
    <exclude>**/dto/**</exclude>
    <exclude>**/exception/**</exclude>
    <!-- エントリーポイント -->
    <exclude>**/Main.class</exclude>
  </excludes>
</configuration>
```

> **注意**: 除外設定はプロジェクト構成に応じて調整すること。除外した箇所は SonarQube の `sonar.exclusions` にも同様に設定すること

---

## 2. カバレッジ計測コマンド

### レポート生成

```powershell
# テスト実行 + JaCoCo レポート生成
mvn clean verify

# テストクラスを絞り込む場合
mvn clean verify "-Dtest=FooServiceTest,BarServiceTest"
```

### レポート確認

```powershell
# HTML レポートの確認（ブラウザで開く）
Start-Process "target/site/jacoco/index.html"

# XML レポートの確認（SonarQube 連携用）
# target/site/jacoco/jacoco.xml
```

---

## 3. カバレッジ目標

| カバレッジ種別 | 目標値 | 備考 |
|---|---|---|
| 命令カバレッジ (Instruction) | **100%** | 到達不能コードを除く |
| 分岐カバレッジ (Branch) | **100%** | 到達不能分岐を除く |
| 行カバレッジ (Line) | **100%** | 到達不能行を除く |
| メソッドカバレッジ (Method) | **100%** | 到達不能メソッドを除く |

### 100% 未達が許容されるケース

以下の条件を**すべて満たす**場合のみ、100% 未満でループを終了してよい:

1. 到達不能コードまたは防御的プログラミングコードが原因である
2. 当該コードが `@ExcludeFromJacocoGeneratedReport` または JaCoCo 除外設定で適切に除外されている
3. テスト全体レビュー（フェーズ 8）エージェントに報告し、承認を得ている

---

## 4. カバレッジ改善手順

### ステップ 1: 未カバー箇所の特定

```powershell
# HTML レポートを確認して赤色（未カバー）の行を特定する
Start-Process "target/site/jacoco/index.html"
```

MCP ツールを使用する場合:

```
mcp_sonarqube_get_file_coverage_details を呼び出す
- key: "{プロジェクトキー}:{ファイルパス}"
```

### ステップ 2: 未カバー箇所の分類

| 分類 | 対応方法 |
|---|---|
| テストが存在しない正常系・異常系 | テストケースを追加する |
| 条件分岐（if/else）の片側未カバー | 各分岐を通るテストケースを追加する |
| 例外ハンドリングが未カバー | 例外が発生するケースのテストを追加する |
| 到達不能コード | 除外設定を追加し、理由をコメントで記述する |

### ステップ 3: テストケース追加

`junit5-testing` スキルに従い、未カバー箇所に対応するテストケースを追加する。

### ステップ 4: 再計測と確認

```powershell
mvn clean verify
```

`BUILD SUCCESS` かつカバレッジ目標を達成していることを確認する。

---

## 5. よくある未カバー原因と対処

### private メソッド

- public メソッドを通じてテストする（private メソッドを直接テストしない）

### static initializer / コンストラクタ

- テストで対象クラスをインスタンス化するテストケースを手動で追加する

### Lombok 生成コード

`lombok.config` に以下を追加してカバレッジ計測対象から除外する:

```properties
lombok.addLombokGeneratedAnnotation = true
```

> Lombok 使用時の注意: `lombok.config` はコミット対象外とする（SonarQube 解析専用設定）

### null チェック・防御コード

- 到達不能な null チェックは除外設定を使用する
- ただし、除外する前にビジネスロジックとして必要かどうかを再検討すること

---

## 6. コミット前チェックリスト

- [ ] `mvn clean verify` が `BUILD SUCCESS` で完了した
- [ ] `target/site/jacoco/index.html` の全カバレッジが 100% である（または除外理由が明記されている）
- [ ] JSF 既存コードが JaCoCo 除外設定に含まれている
- [ ] `target/site/jacoco/jacoco.xml` が存在する（SonarQube 連携用）
