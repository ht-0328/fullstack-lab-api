Feature: タスク API（契約 openapi/api.yaml に沿った振る舞い）

  Background:
    * url baseUrl
    * def userHeaders = { 'X-User-Id': 'karate-user', 'X-User-Name': 'Karate' }
    * def longTitle = 'a'.repeat(101)

  Scenario: 登録したタスクを一覧と詳細で取得できる
    Given path 'api/tasks'
    And headers userHeaders
    And request { title: 'karate: 一覧と詳細', description: '説明', dueDate: '2026-12-01' }
    When method post
    Then status 201
    And match header Location contains '/api/tasks/'
    And match response == { id: '#number', title: 'karate: 一覧と詳細', description: '説明', dueDate: '2026-12-01', createdBy: 'karate-user', createdByName: 'Karate', createdAt: '#string', completedAt: null }
    * def id = response.id

    Given path 'api/tasks', id
    When method get
    Then status 200
    And match response == { id: '#(id)', title: 'karate: 一覧と詳細', description: '説明', dueDate: '2026-12-01', createdBy: 'karate-user', createdByName: 'Karate', createdAt: '#string', completedAt: null }

    Given path 'api/tasks'
    And param ownerId = 'karate-user'
    When method get
    Then status 200
    And match response[*].id contains id
    And match each response contains { createdBy: 'karate-user' }

  Scenario: 所有者で絞り込んだ一覧には他の利用者のタスクが含まれない
    Given path 'api/tasks'
    And headers { 'X-User-Id': 'karate-other', 'X-User-Name': 'Other' }
    And request { title: 'karate: 他人のタスク' }
    When method post
    Then status 201
    * def otherId = response.id

    Given path 'api/tasks'
    And param ownerId = 'karate-user'
    When method get
    Then status 200
    And match response[*].id !contains otherId

  Scenario: タスクを完了にできる。再実行しても 200 で、完了日時は最初の値のまま（冪等）
    Given path 'api/tasks'
    And headers userHeaders
    And request { title: 'karate: 完了' }
    When method post
    Then status 201
    And match response.completedAt == null
    * def id = response.id

    Given path 'api/tasks', id, 'completion'
    When method post
    Then status 200
    And match response contains { id: '#(id)', completedAt: '#string' }
    * def completedAt = response.completedAt

    Given path 'api/tasks', id, 'completion'
    When method post
    Then status 200
    And match response.completedAt == completedAt

    Given path 'api/tasks', id
    When method get
    Then status 200
    And match response.completedAt == completedAt

  Scenario: 存在しないタスクの完了は 404
    Given path 'api/tasks', 999999999, 'completion'
    When method post
    Then status 404
    And match response contains { status: 404 }

  Scenario Outline: 期限日の補足情報は外部カレンダーサービスの応答で決まる（<case>）
    Given path 'api/tasks'
    And headers userHeaders
    And request { title: 'karate: <case>', dueDate: '<dueDate>' }
    When method post
    Then status 201

    Given path 'api/tasks', response.id, 'due-date-info'
    When method get
    Then status <status>
    And match response contains <expected>

    Examples:
      | case     | dueDate    | status | expected                                                              |
      | 祝日     | 2026-11-03 | 200    | { date: '2026-11-03', businessDay: false, holidayName: '文化の日' }   |
      | 営業日   | 2026-12-01 | 200    | { date: '2026-12-01', businessDay: true, holidayName: null }         |
      | 外部障害 | 2099-12-31 | 502    | { status: 502, title: 'Bad Gateway' }                                 |

  Scenario: 期限日のないタスクの補足情報は 404
    Given path 'api/tasks'
    And headers userHeaders
    And request { title: 'karate: 期限なし' }
    When method post
    Then status 201

    Given path 'api/tasks', response.id, 'due-date-info'
    When method get
    Then status 404
    And match response contains { status: 404 }

  Scenario Outline: 入力エラーは 400 の ProblemDetail（<case>）
    Given path 'api/tasks'
    And headers userHeaders
    And request <body>
    When method post
    Then status 400
    And match response contains { status: 400, title: 'Bad Request' }

    Examples:
      | case               | body                      |
      | タイトルが空       | { title: '' }             |
      | タイトルが長すぎる | { title: '#(longTitle)' } |
      | タイトルがない     | { description: 'x' }      |

  Scenario: 利用者ヘッダーが無い登録は 400（BFF が必ず付ける前提の契約）
    Given path 'api/tasks'
    And request { title: 'karate: ヘッダーなし' }
    When method post
    Then status 400

  Scenario: 存在しない ID は 404
    Given path 'api/tasks', 999999999
    When method get
    Then status 404
    And match response contains { status: 404 }
