---
name: sonarqube
description: SonarQube MCP を使用した静的解析の実施方法。解析前の準備・MCP ツールの使い方・問題の修正ガイドを含む。sonar-review エージェントが参照する
---

## 1. 作業開始前チェックリスト

以下をすべて確認してから作業を開始すること

- [ ] SonarQube サーバーが起動していること

  ```powershell
  # 起動確認（"status":"UP" が返れば OK）
  curl.exe --silent "http://localhost:9000/api/system/status"
  ```

  起動していない場合は以下で再起動する:

  ```powershell
  docker start sonarqube
  ```

- [ ] プロジェクトキーを確認する（`sonar-project.properties` の `sonar.projectKey` の値）
- [ ] `target/classes/` ディレクトリが存在すること（Maven ビルド済みであること）

  存在しない場合は以下をプロジェクトルートで実行する:

  ```powershell
  mvn clean package -DskipTests -q
  ```

---

## 2. 解析実施手順

### ステップ 1: Maven ビルド（.class ファイルの生成）

プロジェクトルートで以下を実行する:

```powershell
mvn clean package -DskipTests -q
```

`BUILD SUCCESS` が表示されたら次へ進む

---

### ステップ 2: sonar-scanner-cli による解析送信

トークンは MCP 設定（`mcp.json`）の `SONAR_TOKEN` 環境変数、または SonarQube の `http://localhost:9000/account/security` で確認・発行する

```powershell
docker run --rm `
  -e SONAR_HOST_URL="http://host.docker.internal:9000" `
  -e SONAR_TOKEN="{SonarQube トークン}" `
  -v "{プロジェクトルートの絶対パス}:/usr/src" `
  sonarsource/sonar-scanner-cli `
  "-Dsonar.projectKey={sonar.projectKey の値}" `
  "-Dsonar.sources=src/main/java" `
  "-Dsonar.tests=src/test/java" `
  "-Dsonar.java.binaries=target/classes" `
  "-Dsonar.exclusions=src/main/webapp/**,{既存 JSF Bean パッケージのパス}/**"
```

プロジェクトルートに `sonar-project.properties` が存在する場合、`-Dsonar.projectKey` 以降のオプションは省略できる。`sonar.exclusions` で既存 JSF コードを除外しているため、新規実装した API コードのみが解析対象になる。

末尾に `EXECUTION SUCCESS` が表示されれば解析完了

---

### ステップ 3: MCP ツールによる問題の確認

解析完了後、`mcp_sonarqube_search_sonar_issues_in_projects` で問題を照会する。プロジェクトキーは `sonar-project.properties` の `sonar.projectKey` の値を使用する。

```
# プロジェクト全体の問題を確認
projects: ["{プロジェクトキー}"]
issueStatuses: ["OPEN"]

# 特定ファイルのみに絞り込む場合は files を追加
files: ["{確認したいファイルのパス}"]

# 重大度で絞り込む場合は severities を指定
severities: ["HIGH", "BLOCKER"]

# セキュリティ問題のみを確認する場合
impactSoftwareQualities: ["SECURITY"]
```

---

## 3. MCP ツール一覧と使い方

### `mcp_sonarqube_search_sonar_issues_in_projects`

プロジェクト内の問題（バグ・脆弱性・コードスメル）を検索する  
主なパラメータ:

| パラメータ | 型 | 説明 |
|---|---|---|
| `projects` | `string[]` | 対象プロジェクトキー |
| `files` | `string[]` | 特定ファイルへの絞り込み（オプション） |
| `severities` | `string[]` | `INFO` / `LOW` / `MEDIUM` / `HIGH` / `BLOCKER` |
| `impactSoftwareQualities` | `string[]` | `MAINTAINABILITY` / `RELIABILITY` / `SECURITY` |
| `issueStatuses` | `string[]` | `OPEN` / `CONFIRMED` / `FALSE_POSITIVE` / `ACCEPTED` / `FIXED` |

---

### `mcp_sonarqube_show_rule`

問題に対応するルールの詳細（説明・修正方法）を取得する。問題に含まれる `ruleKey` を指定する

```
key: "java:S1135"
```

---

### `mcp_sonarqube_analyze_code_snippet`

ファイルのコードスニペットを解析し、潜在的な問題を検出する  
**注意**: ファイル全体のコンテンツを渡す必要がある

```
projectKey: "{プロジェクトキー}"
fileContent: [ファイルの全内容]
language: ["java"]
```

---

### `mcp_sonarqube_search_files_by_coverage`

カバレッジが低いファイルを検索する（カバレッジ改善の対象特定に使用）

```
projectKey: "{プロジェクトキー}"
maxCoverage: 80  # 80% 未満のファイルを取得
```

---

### `mcp_sonarqube_get_file_coverage_details`

特定ファイルのカバレッジ詳細（行・ブランチ単位）を取得する

```
projectKey: "{プロジェクトキー}"
filePath: "{確認したいファイルのパス}"
```

---

## 4. 問題修正ガイド

### 問題の優先順位

| 重大度 | 対応方針 |
|---|---|
| `BLOCKER` | 必ず修正する。修正せずに作業を完了してはならない |
| `HIGH` | 必ず修正する |
| `MEDIUM` | 原則修正する。修正が困難な場合はユーザーに状況を報告する |
| `LOW` / `INFO` | 修正するが優先度は低い。他の修正が完了してから対応する |

---

### よくある問題と修正パターン

#### `java:S1172` — 使用されていないメソッドパラメータ

不要なパラメータを削除する

```java
// NG: パラメータ e が使われていない
public void handleError(Exception e) {
    log.error("error occurred");
}

// OK: 使用しないパラメータを削除する
public void handleError() {
    log.error("error occurred");
}
```

#### `java:S1068` — 使用されていないフィールド

プライベートフィールドが実際に使用されていない場合はフィールドを削除する

#### `java:S112` — 汎用例外のスロー（RuntimeException / Exception）

```java
// NG
throw new RuntimeException("not found");

// OK: 意味のある例外クラスを使用する
throw new NotFoundException("Todo not found: id=" + id);
```

#### `java:S2259` — Null ポインタデリファレンスの可能性

```java
// NG
String value = map.get("key").trim();

// OK: null チェックを追加
String raw = map.get("key");
String value = raw != null ? raw.trim() : "";
```

#### `java:S3776` — 認知的複雑度が高い（Cognitive Complexity）

メソッドを分割して複雑度を下げる。`api-implementation` スキルの「1 メソッド 30 行以下」のルールに従う

#### `java:S106` — System.out.println の使用

```java
// NG
System.out.println("debug: " + value);

// OK: 適切なロガーを使用する
private static final Logger log = LoggerFactory.getLogger(ClassName.class);
log.debug("debug: {}", value);
```

---

### 修正禁止事項

- 既存 JSF コード（`bean/`・`model/` パッケージ）を変更してはならない
- `@SuppressWarnings` で問題を隠蔽してはならない（正当な理由がある場合はユーザーに確認する）
- テストの削除・無効化による問題回避は禁止する
- `sonar.exclusions` にファイルを追加して問題を除外することは禁止する（設定変更はユーザーに確認する）

---

## 5. 解析結果の再確認

修正後は以下の手順で解析結果を再確認する

1. `mvn clean package -DskipTests -q` で再ビルドする
2. sonar-scanner-cli で再解析する（ステップ 2 の手順を再実行）
3. MCP ツールで OPEN 状態の問題が解消されていることを確認する

```
projects: ["{プロジェクトキー}"]
issueStatuses: ["OPEN"]
severities: ["HIGH", "BLOCKER"]
```

BLOCKER / HIGH がゼロであることを確認すること
