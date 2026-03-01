---
name: code-style-formatting
description: Spotless と Checkstyle を用いたコード整形・規約チェック規約。pom.xml プラグイン設定・新規ファイルのみを対象とした整形手順・Checkstyle 違反チェック手順・終了基準を含む。spotless-formatter エージェントおよび checkstyle-checker エージェントが参照する
---

## 1. このスキルの適用範囲

本スキルはマイグレーション作業中に **新規追加されたファイルのみ** を対象とする。

- **対象**: `git diff --name-only --diff-filter=A HEAD` で列挙される新規追加ファイル
- **対象外**: 既存の JSF コード（Backing Bean・Model・XHTML・`faces-config.xml`・`web.xml`）および今回変更されていない既存 Java ファイル

> **制約**: ロジックの変更は一切行わない。フォーマット・規約以外の問題は扱わない。

---

## 2. pom.xml へのプラグイン追加

Spotless・Checkstyle いずれも `pom.xml` の `<build><plugins>` に追加する。
**既に追加済みの場合はこのステップをスキップする。**

### 2-1. Spotless Maven Plugin

```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.43.0</version>
    <configuration>
        <java>
            <!-- 対象は src/main/java と src/test/java 配下の Java ファイル全体 -->
            <includes>
                <include>src/main/java/**/*.java</include>
                <include>src/test/java/**/*.java</include>
            </includes>
            <!-- Google Java Format (Java 11 対応) -->
            <googleJavaFormat>
                <version>1.15.0</version>
                <style>GOOGLE</style>
            </googleJavaFormat>
            <!-- import の並び順 -->
            <importOrder>
                <order>java,javax,jakarta,io,org,com,jp</order>
            </importOrder>
            <!-- 末尾の空白を削除 -->
            <trimTrailingWhitespace/>
            <!-- ファイル末尾に改行を強制 -->
            <endWithNewline/>
        </java>
    </configuration>
</plugin>
```

### 2-2. Checkstyle Maven Plugin

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.3.1</version>
    <dependencies>
        <dependency>
            <groupId>com.puppycrawl.tools</groupId>
            <artifactId>checkstyle</artifactId>
            <version>10.12.7</version>
        </dependency>
    </dependencies>
    <configuration>
        <configLocation>.github/checkstyle/checkstyle.xml</configLocation>
        <consoleOutput>true</consoleOutput>
        <failsOnError>false</failsOnError>
        <outputFile>target/checkstyle-result.xml</outputFile>
        <includeTestSourceDirectory>true</includeTestSourceDirectory>
    </configuration>
</plugin>
```

> `failsOnError=false` にしているのは、整形と分離して結果を段階的に確認するため。
> Checkstyle 違反が残っている場合は後続フェーズに進めない（Section 6 参照）。

---

## 3. Checkstyle ルールファイルの準備

`.github/checkstyle/checkstyle.xml` が存在しない場合は、以下の内容で作成する。
**既に存在する場合はこのステップをスキップする。**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="error"/>

    <!-- ファイル単位チェック -->
    <module name="NewlineAtEndOfFile"/>

    <module name="TreeWalker">
        <!-- インポート規約 -->
        <module name="AvoidStarImport"/>
        <module name="UnusedImports"/>
        <module name="RedundantImport"/>

        <!-- 命名規約 -->
        <module name="TypeName"/>
        <module name="MethodName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="ParameterName"/>
        <module name="ConstantName"/>

        <!-- コードスタイル -->
        <module name="EmptyBlock"/>
        <module name="EmptyCatchBlock">
            <property name="exceptionVariableName" value="expected|ignore"/>
        </module>
        <module name="NeedBraces"/>
        <module name="LeftCurly"/>
        <module name="RightCurly"/>

        <!-- 空白 -->
        <module name="WhitespaceAround">
            <property name="tokens" value="ASSIGN,BAND,BAND_ASSIGN,BOR,BOR_ASSIGN,BSR,BSR_ASSIGN,BXOR,BXOR_ASSIGN,DIV,DIV_ASSIGN,EQUAL,GE,GT,LAND,LE,LITERAL_CATCH,LITERAL_DO,LITERAL_ELSE,LITERAL_FINALLY,LITERAL_FOR,LITERAL_IF,LITERAL_RETURN,LITERAL_SWITCH,LITERAL_SYNCHRONIZED,LITERAL_TRY,LITERAL_WHILE,LOR,LT,MINUS,MINUS_ASSIGN,MOD,MOD_ASSIGN,NOT_EQUAL,PLUS,PLUS_ASSIGN,QUESTION,SL,SLIST,SL_ASSIGN,SR,SR_ASSIGN,STAR,STAR_ASSIGN,TYPE_EXTENSION_AND"/>
        </module>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>

        <!-- 行長 -->
        <module name="LineLength">
            <property name="max" value="120"/>
        </module>

        <!-- ブロックスコープ -->
        <module name="VisibilityModifier">
            <property name="packageAllowed" value="false"/>
            <property name="protectedAllowed" value="true"/>
        </module>

        <!-- その他 -->
        <module name="ModifierOrder"/>
        <module name="RedundantModifier"/>
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>
    </module>
</module>
```

---

## 4. 新規ファイルのリスト取得

以下のコマンドで今回新規追加されたファイルを取得する:

```powershell
# 新規追加された Java ファイルの一覧
git diff --name-only --diff-filter=A HEAD

# ステージされていない新規ファイル（untracked）も含めて確認する場合
git status --short | Select-String "^\?\?"
```

> 取得したファイルパスをメモしておく（以降の手順で使用する）

---

## 5. Spotless によるコード整形

### ステップ 1: 整形前の状態を記録

```powershell
git diff --name-only HEAD
git status --short
```

修正されているファイルのリストを記録する（整形後に変化を比較するため）。

### ステップ 2: Spotless apply の実行

```powershell
mvn spotless:apply -q
```

> Spotless はフォーマットが必要なファイルを自動的に整形する。

### ステップ 3: 整形後の差分を確認

```powershell
git diff --name-only
```

**確認ポイント**:
- 差分が出たファイルが **新規追加ファイルのみ** であることを確認する
- 既存ファイル（Backing Bean・既存 API コード）が含まれている場合は、それらの変更を `git checkout` で元に戻す

```powershell
# 既存ファイルへの変更を元に戻す例
git checkout -- src/main/java/jp/co/example/todo/bean/SomeBean.java
```

### ステップ 4: 整形結果の検証

```powershell
mvn spotless:check -q
```

エラーなしで完了すれば整形成功。エラーが出た場合はステップ 2 に戻る。

---

## 6. Checkstyle による規約違反チェック

### ステップ 1: Checkstyle の実行

```powershell
mvn checkstyle:check -q 2>&1
```

または XML レポートを生成してから確認する:

```powershell
mvn checkstyle:checkstyle -q
```

### ステップ 2: 新規ファイルの違反のみを抽出

Checkstyle は全ファイルを対象とするため、新規ファイルに関する違反のみを抽出する:

```powershell
# 新規追加ファイルのリスト（Section 4 で取得したもの）を使い、レポートから該当行を抽出
[xml]$report = Get-Content "target/checkstyle-result.xml"
# 新規ファイルのパス名でフィルタリングして違反箇所を確認する
```

または標準出力から手動でフィルタリングする:

```powershell
mvn checkstyle:check 2>&1 | Select-String "src/main/java/jp/co/example/ec/api"
mvn checkstyle:check 2>&1 | Select-String "src/test/java/jp/co/example/ec/api"
```

### ステップ 3: 違反の修正

違反があった場合は **該当箇所のコードを手動で修正する**。
以下のルールに従うこと:

| 違反種別 | 対応方法 |
|---|---|
| `AvoidStarImport` | ワイルドカードインポートをシングルクラスインポートに変換 |
| `UnusedImports` | 未使用のインポートを削除 |
| `NeedBraces` | `if`・`for`・`while` に `{}` を追加 |
| `LineLength` | 120 文字を超える行を改行で分割 |
| `EmptyBlock` | 空のブロックにコメントを追加するか削除 |
| `VisibilityModifier` | フィールドを `private` に変更し getter/setter を追加 |
| `ModifierOrder` | 修飾子を `public static final` の順に修正 |

> **禁止**: ロジックを変更すること・テストを削除すること・既存ファイルを変更すること

### ステップ 4: 修正後の再確認

```powershell
mvn checkstyle:check 2>&1 | Select-String "src/main/java/jp/co/example/ec/api"
mvn checkstyle:check 2>&1 | Select-String "src/test/java/jp/co/example/ec/api"
```

新規ファイルに関する違反がゼロになるまで繰り返す。

---

## 7. テスト再実行による回帰確認

整形・修正後に必ずテストが Green であることを確認する:

```powershell
mvn test -q
```

`BUILD SUCCESS` かつ `Failures: 0, Errors: 0` を確認する。

---

## 8. 終了基準

以下の全項目を満たすこと。1 つでも未達の場合は後続フェーズに進めない。

- [ ] `mvn spotless:check` がエラーなしで完了すること
- [ ] 新規追加ファイルに関する Checkstyle 違反がゼロであること
- [ ] `mvn test` が `BUILD SUCCESS`・`Failures: 0, Errors: 0` で完了すること
- [ ] 既存ファイルへのロジック変更がないこと（`git diff` で確認）

---

## 9. コミット対象への注意

- `spotless-maven-plugin` を `pom.xml` に追加した場合は **コミット対象に含む**
  - ただしプロパティや設定値が環境依存でないことを確認すること
- `.github/checkstyle/checkstyle.xml` は **コミット対象に含む**
- Spotless によって整形された **新規ファイルの変更はコミット対象に含む**
- 既存ファイルへの Spotless によるフォーマット変更は **コミットしない**（`git checkout -- <file>` で元に戻す）
