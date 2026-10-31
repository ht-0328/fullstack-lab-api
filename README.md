# fullstack-lab-api

タスクの業務 API（Kotlin + Spring Boot）。[fullstack-lab](https://github.com/ht-0328/fullstack-lab) の一部で、BFF からのみ呼ばれる内部サービスです。全体の構成と動かし方は fullstack-lab の README を参照してください。

## 契約が正

[openapi/api.yaml](openapi/api.yaml) がこのサービスの契約です。次を生成し、生成コードは git で管理します（手で直さず、仕様を直して生成し直します）。

| 生成物 | 生成元 | 使う側 |
|---|---|---|
| コントローラーのインターフェースと DTO（`src/generated`） | `kotlin-spring` | このリポジトリ。[TaskController](src/main/kotlin/com/example/fullstacklab/api/task/TaskController.kt) が `TasksApi` を実装する |
| クライアントライブラリ `com.example.fullstacklab:api-client`（[client/](client/)、生成物は `client/src/generated`） | `kotlin`（`jvm-spring-restclient` + Jackson 3） | BFF。Maven リポジトリ経由でバージョン指定で依存する |

仕様を変えたら `./gradlew generate` で生成し直し、生成物も同じコミットに含めます（ホストに JDK が無ければ fullstack-lab の `./scripts/generate.sh`）。`docker build --target test .` はテストの前に生成し直して差分がないことを確かめるので（`check-generated` ステージ）、生成し忘れや手で直した生成コードは CI で落ちます。

契約を変えるときは、`openapi/api.yaml` の `info.version` と [gradle.properties](gradle.properties) の `version` を同時に上げます（ずれていると publish に失敗します）。同じバージョンは上書きしません。呼び出し側は、自分の都合の良いタイミングで新しいバージョンへ追従します。

## テスト

| 種類 | 実行 | 確認すること |
|---|---|---|
| 単体テスト | `docker build --target test .` | `CalendarClientTest`：外部サービス呼び出しの契約（API キー、変換、エラー） |
| API テスト（Karate） | `docker compose -f compose.test.yaml build && docker compose -f compose.test.yaml run --rm karate` | [karate/features/tasks.feature](karate/features/tasks.feature)：契約どおりの振る舞い（登録・取得・所有者での絞り込み・完了（冪等）・外部サービスの応答による補足情報・入力エラー・404） |

Karate は [compose.test.yaml](compose.test.yaml) で、この API と依存先（DB、WireMock）だけを立てて実行します。他のリポジトリは要りません。実行環境は [karate/Dockerfile](karate/Dockerfile)（JRE + Karate の実行ファイル）で、ホストに JDK は不要です。終わったら `docker compose -f compose.test.yaml down -v` で片付けます。

fullstack-lab の全体構成の中で同じテストを流すには、fullstack-lab の `scripts/api-test.sh` を使います。

## コマンド

```bash
docker build --target test .                      # 生成 + 単体テスト（JDK 不要）
./gradlew :client:publish                         # クライアントを publish（下記の環境変数で宛先を指定）
```

| 環境変数 | 意味 | 既定 |
|---|---|---|
| `MAVEN_REPO_PUBLISH_URL` | publish 先 | `http://localhost:8081/releases`（ローカルの Reposilite） |
| `MAVEN_REPO_USER` / `MAVEN_REPO_PASSWORD` | 資格情報 | なし |

CI（[.github/workflows/ci.yml](.github/workflows/ci.yml)）は、PR で単体テストと Karate を実行し、main に取り込まれたときにクライアントを GitHub Packages へ publish します。

## その他

- [wiremock/](wiremock/)：外部カレンダーサービスのスタブ。このサービスの外部依存なので、ここで管理します
- [src/main/resources/db/migration/](src/main/resources/db/migration/)：Flyway のマイグレーション
