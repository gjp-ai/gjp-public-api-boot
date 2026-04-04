# GJP Open API — AI Instructions

This file is the universal source of truth for AI assistants working in this repository.
Tool-specific files (CLAUDE.md, .cursorrules, .github/copilot-instructions.md, etc.) are
kept in sync with this file via `tooling/scripts/util/sync-ai-instructions.sh`.

---

## Project Overview

GJP Open API — a Spring Boot 3.x **read-only, unauthenticated** REST API serving public
content (articles, videos, images, audio, files, logos, questions, websites, app settings).

- **Port:** 8081 | **Context path:** `/api/` | **Base URL:** `http://localhost:8081/api`
- **Java 21** | **Spring Boot 3.4.5** | **MySQL 8**
- **No authentication** — all endpoints are public
- **No mutations** — GET only; all writes handled by `gjp-admin-api-boot`
- **Shares database** with `gjp-admin-api-boot` (same tables)

---

## Read These Before Starting Any Task

| File | What it contains |
|---|---|
| `tooling/docs/context/quick-reference.md` | All entities, tables, endpoints, and mandatory patterns |
| `tooling/docs/context/decisions.md` | Why key architectural decisions were made — do not re-propose items listed here |
| `tooling/docs/context/todo.md` | In-progress work, known gaps, and items decided against |
| `tooling/docs/guide/CODING_STANDARDS.md` | Coding rules — follow before writing any code |

---

## Mandatory Code Patterns

### Every response
```java
ResponseEntity<ApiResponse<T>>                      // single object
ResponseEntity<ApiResponse<PaginatedResponse<T>>>   // paginated list
```

### Success response
```java
return ResponseEntity.ok(ApiResponse.success(data, "Message"));
```

### No @PreAuthorize — all endpoints are public

### Service — read-only transactions
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public PaginatedResponse<ArticleResponse> getArticles(...) { ... }

    private ArticleResponse mapToResponse(Article article) { ... }  // always private
}
```

### Pagination — use CmsUtil
```java
Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
```

### Language parsing — use CmsUtil
```java
Article.Language language = CmsUtil.parseLanguage(lang, Article.Language.class); // null-safe
```

### Entity base — no @Data
```java
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity @Table(name = "cms_article")
public class Article {  // NOT extends BaseEntity in this project
    @Id @Column(columnDefinition = "char(36)") private String id;
    ...
}
```

---

## Package Structure

```
org.ganjp.api/
├── core/
│   ├── config/CorsConfig.java
│   ├── exception/GlobalExceptionHandler.java
│   └── model/ ApiResponse, PaginatedResponse
├── cms/
│   ├── util/CmsUtil.java   ← use for parseLanguage, buildPageable, validateFilename
│   ├── article/            ← Article, ArticleImage sub-package
│   ├── audio/
│   ├── file/
│   ├── image/
│   ├── logo/
│   ├── question/
│   ├── video/
│   └── website/
└── master/setting/         ← AppSetting
```

---

## Key Constraints

| Rule | Value |
|---|---|
| No mutations | GET only — never add POST/PUT/PATCH/DELETE |
| No auth | No `@PreAuthorize`, no SecurityConfig, no JWT |
| `AppSetting` | Only `isPublic = true` records returned |
| Default sort | `displayOrder ASC` |
| No `@Data` on entities | Use `@Getter @Setter @Builder` separately |
| Test coverage | Must exceed 90% (JaCoCo enforced — build fails below threshold) |
| Test naming | `should_doSomething_when_condition()` |
| UUID PKs | `CHAR(36)`, never `AUTO_INCREMENT` |

---

## Build & Test Commands

```bash
./mvnw compile
./mvnw test
./tooling/script/run.sh                    # run with dev profile
./tooling/script/run.sh --prod             # run with prod profile
./tooling/script/coverage.sh               # generate JaCoCo coverage report
./mvnw package -DskipTests
```

---

## Key Directories

| Path | Contents |
|---|---|
| `src/main/java/org/ganjp/api/` | Main source code |
| `src/main/resources/` | `application*.yml` config files |
| `tooling/docs/context/` | AI context files (quick-reference, decisions, todo) |
| `tooling/docs/guide/` | Coding standards |
| `tooling/script/` | Run and coverage scripts |
| `tooling/postman/` | Postman collection |

---

## Git Conventions

- Commit messages: imperative mood, describe the "why"
- Do not commit `.env`, credentials, or secrets
