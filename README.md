# GoREST API Automation Framework

REST Assured + TestNG + Hamcrest framework for the public **GoREST** API
(https://gorest.co.in/public/v2).

Everything reusable lives in `src/main/java` (utilities, endpoint library, POJOs, config).
The test classes in `src/test/java` stay thin and read as plain
**given / when / then** with Hamcrest matchers.

---

## Tech stack

| Purpose            | Library                                |
|--------------------|----------------------------------------|
| HTTP client / DSL  | REST Assured 5.5.0                     |
| Assertions         | Hamcrest matchers (via REST Assured)   |
| Test runner        | TestNG 7.10.2                          |
| Test reporting     | Extent Reports 5.1.2                   |
| JSON / POJO        | Jackson 2.17.2                         |
| Schema validation  | rest-assured json-schema-validator     |
| Build              | Maven (Java 17)                        |

---

## Project structure

```
RestassuredPetStore
├── pom.xml
├── testng.xml                 # full regression suite
├── testng-smoke.xml           # smoke subset (group = smoke)
│
├── src/main/java/com/gorest
│   ├── config
│   │   └── ConfigManager.java         # reads config.properties (-D / env override)
│   ├── constants
│   │   ├── IEndpointLibrary.java      # every endpoint path, one place
│   │   ├── StatusCode.java            # 200 / 201 / 204 / 401 / 404 / 422
│   │   └── FrameworkConstants.java    # config keys, folders, domain values
│   ├── pojo
│   │   ├── User.java                  # request + response model
│   │   ├── Post.java
│   │   └── ApiError.java              # 422 field/message entry
│   └── utils
│       ├── RestAssuredUtility.java    # base URI, auth, request/response specs
│       ├── JsonUtility.java           # POJO <-> JSON, payload templates, GPath reads
│       ├── TestDataUtility.java       # unique users / posts, invalid ids
│       └── LogUtility.java            # one-line logging
│
├── src/main/resources
│   ├── config.properties              # base uri, base path, token, timeouts
│   └── payloads                       # create-user.json, update-user.json, ...
│
├── src/test/java/com/gorest
│   ├── base
│   │   ├── BaseTest.java              # suite setup + fixtures + cleanup
│   │   └── AuthenticatedTest.java     # skips write tests when no token is set
│   ├── data
│   │   └── UserDataProvider.java      # valid / invalid data sets
│   └── tests
│       ├── GetUserTests.java          # READ    (no token needed)
│       ├── CreateUserTests.java       # CREATE
│       ├── UpdateUserTests.java       # UPDATE  (PUT + PATCH)
│       ├── DeleteUserTests.java       # DELETE
│       ├── UserValidationTests.java   # 422 validation
│       ├── UnauthorizedAccessTests.java # 401 (no token needed)
│       └── UserPostTests.java         # nested resource /users/{id}/posts
│
└── src/test/resources/schemas         # JSON schemas used by matchesJsonSchemaInClasspath
```

---

## Getting a token

Read calls are public, every write call needs a personal access token.

1. Sign in at https://gorest.co.in/consumer/login
2. Copy the access token shown on the dashboard
3. Supply it in any of these ways (first one wins):

```bash
mvn test -Dtoken=<your-token>        # command line
set TOKEN=<your-token>               # environment variable (Windows)
export TOKEN=<your-token>            # environment variable (Linux/macOS)
```

or paste it into `src/main/resources/config.properties`:

```properties
token=<your-token>
```

Without a token the write tests are **skipped** with a clear message instead of
failing with a confusing 401 - the read and 401 tests still run.

---

## How to run

```bash
mvn clean test                          # full regression suite (testng.xml)
mvn clean test -Psmoke                  # smoke subset (testng-smoke.xml)
mvn clean test -Dtoken=<your-token>     # full suite including write scenarios
mvn clean test -Dtest=GetUserTests      # one class
```

Reports and logs:

| Output                | Location                                        |
|-----------------------|-------------------------------------------------|
| TestNG HTML report    | `target/surefire-reports/index.html`            |
| Emailable report      | `target/surefire-reports/emailable-report.html` |
| Extent report         | `target/extent-reports/extent-report.html`      |
| Request/response log  | `target/logs/api.log`                           |

---

## How a test looks

```java
@Test(groups = {"smoke", "regression"},
        description = "POST /users with a valid payload returns 201 and echoes the data back")
public void createUser_withValidPayload_shouldReturn201() {
    User user = TestDataUtility.randomUser();

    given()
            .spec(RestAssuredUtility.requestSpec())
            .body(user)
    .when()
            .post(IEndpointLibrary.USERS)
    .then()
            .spec(RestAssuredUtility.jsonResponseSpec(StatusCode.CREATED))
            .body("id", notNullValue())
            .body("name", equalTo(user.getName()))
            .body("email", equalTo(user.getEmail()))
            .body(matchesJsonSchemaInClasspath("schemas/user-schema.json"));
}
```

- `requestSpec()` - JSON headers + bearer token, built once in the utility
- `jsonResponseSpec(...)` - status code + JSON content type + response-time budget
- `body(...)` - Hamcrest matchers (`equalTo`, `hasItem`, `everyItem`, `greaterThan`, ...)

---

## Test coverage

| Area | Scenario | Expected |
|------|----------|----------|
| Read | GET /users returns a list matching the schema | 200 |
| Read | Pagination `page` / `per_page` + `x-pagination-*` headers | 200 |
| Read | Filter by `status` and by `gender` | 200 |
| Read | GET /users/{id} | 200 |
| Read | GET /users/{id} with an unknown id | 404 |
| Read | List deserialises into `User` POJOs | 200 |
| Create | POST /users with a valid payload | 201 |
| Create | POST from a JSON payload template | 201 |
| Create | Both gender / status combinations (data provider) | 201 |
| Create | Created user is retrievable by id | 200 |
| Update | PUT /users/{id} replaces the values | 200 |
| Update | PATCH /users/{id} changes a single field | 200 |
| Update | Change is persisted (fresh GET) | 200 |
| Update | PUT with an unknown id | 404 |
| Delete | DELETE /users/{id} | 204 |
| Delete | Deleted user is gone | 404 |
| Delete | Delete twice / unknown id | 404 |
| Auth | POST / PUT / DELETE without a token | 401 |
| Auth | POST with an invalid token | 401 |
| Validation | Blank name, blank email, malformed email, bad gender, blank status | 422 |
| Validation | Duplicate email | 422 |
| Validation | Missing email field, blank payload | 422 |
| Posts | POST /users/{id}/posts | 201 |
| Posts | GET /users/{id}/posts, GET /posts | 200 |

Groups available for filtering: `smoke`, `regression`, `negative`.

---

## Design notes

- **No hard-coded endpoints** - every path comes from `IEndpointLibrary`.
- **No hard-coded status codes** - `StatusCode` enum.
- **Specs, not repetition** - headers, auth, content type and the response-time
  budget live in `RestAssuredUtility`, so a test only states what is unique to it.
- **Self-cleaning** - `BaseTest` deletes every user a class created in `@AfterClass`,
  so reruns never collide with leftover data.
- **Unique data** - `TestDataUtility.uniqueEmail()` keeps GoREST from rejecting
  repeated runs with "email has already been taken".
- **Nothing secret in git** - the token can be passed with `-Dtoken=` or an env var.
