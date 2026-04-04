# Quick Reference

One-page cheat sheet. Read this before writing any code for this project.

---

## Project Identity

- **Role:** Public read-only API — no authentication, no mutations
- **Port:** 8084 | **Context path:** `/api/` | **Base URL:** `http://localhost:8084/api`
- **Java 21** | **Spring Boot 3.4.5** | **MySQL 8**
- **Package root:** `org.ganjp.api`
- **Shares database** with `gjp-admin-api-boot` — same tables, same data

---

## Entities & Tables

| Entity | Table | Notable Fields |
|---|---|---|
| `Article` | `cms_article` | title, summary, content (longtext), coverImageFilename, lang, isActive |
| `ArticleImage` | `cms_article_image` | articleId, filename, width, height, lang |
| `Video` | `cms_video` | name, filename, coverImageFilename, description, tags, lang |
| `Image` | `cms_image` | name, filename, thumbnailFilename, width, height, altText, lang |
| `Audio` | `cms_audio` | name, filename, coverImageFilename, subtitle (text), artist, lang |
| `File` | `cms_file` | name, filename, extension, mimeType, lang |
| `Logo` | `cms_logo` | name, filename, extension, lang |
| `Question` | `cms_question` | question (255), answer (2000), lang |
| `Website` | `cms_website` | name, url, logoUrl, description, lang |
| `AppSetting` | `master_app_settings` | name, value (500), lang, isPublic |

All entities have: `id` (CHAR 36 UUID), `lang` (EN/ZH), `displayOrder`, `isActive`, `updatedAt`

---

## All Endpoints

All endpoints are **public** — no `Authorization` header required.

```
GET  /                          Welcome message

GET  /v1/articles               ?title&lang&tags&isActive&page&size&sort&direction
GET  /v1/articles/{id}          Full article with content

GET  /v1/videos                 ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/videos/{id}

GET  /v1/images                 ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/images/{id}

GET  /v1/audios                 ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/audios/{id}

GET  /v1/files                  ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/files/{id}

GET  /v1/logos                  ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/logos/{id}

GET  /v1/questions              ?question&lang&tags&isActive&page&size&sort&direction
GET  /v1/questions/{id}

GET  /v1/websites               ?name&lang&tags&isActive&page&size&sort&direction
GET  /v1/websites/{id}

GET  /v1/app-settings           Public settings only (isPublic=true, no filters)
```

**Pagination defaults:** `page=0`, `size=20`, `sort=displayOrder`, `direction=asc`

---

## Media File URLs

Media files are stored by the admin API. Public API constructs URLs using configured base URL:

| Content | URL Pattern |
|---|---|
| Article cover image | `{cms.base-url}/v1/articles/cover-images/{filename}` |
| Article body image | `{cms.base-url}/v1/articles/content-images/{filename}` |
| Video file | `{cms.base-url}/v1/videos/{filename}` |
| Image (full) | `{cms.base-url}/v1/images/view/{filename}` |
| Image thumbnail | `{cms.base-url}/v1/images/view/{thumbnailFilename}` |
| Audio file | `{cms.base-url}/v1/audios/{filename}` |
| Audio cover | `{cms.base-url}/v1/audios/{coverImageFilename}` |
| File download | `{cms.base-url}/v1/files/view/{filename}` |
| Logo | `{cms.base-url}/v1/logos/view/{filename}` |

`cms.base-url` is configured per environment (`application-dev.yml`, `application-prod.yml`).

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

### ApiResponse structure (differs from admin API)
```json
{
  "status": { "code": 200, "message": "...", "errors": null },
  "data": { ... },
  "meta": { "serverDateTime": "2025-02-15 10:30:45" }
}
```

### PaginatedResponse structure
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 150,
  "totalPages": 8
}
```

### Controller — keep it thin
```java
// No logic in controllers — delegate entirely to service
@GetMapping
public ResponseEntity<ApiResponse<PaginatedResponse<ArticleResponse>>> getArticles(...) {
    return ResponseEntity.ok(ApiResponse.success(articleService.getArticles(...), "Articles retrieved"));
}
```

### Service — read-only transactions
```java
@Transactional(readOnly = true)
public PaginatedResponse<ArticleResponse> getArticles(...) { ... }

private ArticleResponse mapToResponse(Article article) { ... }  // always private
```

### Pagination (use CmsUtil helper)
```java
Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
```

### Language parsing (use CmsUtil helper)
```java
Article.Language language = CmsUtil.parseLanguage(lang, Article.Language.class); // null-safe
```

### No @PreAuthorize needed — all endpoints are public

---

## Key Constraints

| Rule | Value |
|---|---|
| All endpoints | Public — no authentication |
| No mutations | GET only — all write ops done by admin API |
| `AppSetting` | Only `isPublic = true` records returned |
| Default sort | `displayOrder ASC` |
| UUID primary keys | `CHAR(36)`, never `AUTO_INCREMENT` |
| No `@Data` on entities | Use `@Getter @Setter @Builder` etc. separately |
| Test coverage | Must exceed 90% (JaCoCo enforced) |
| Lombok | Use aggressively — `@RequiredArgsConstructor`, `@Slf4j`, `@Builder` |

---

## Package Structure

```
org.ganjp.api/
├── GjpApiPublicApplication.java
├── RootController.java          — GET /
├── core/
│   ├── config/CorsConfig.java
│   ├── exception/GlobalExceptionHandler.java
│   └── model/
│       ├── ApiResponse.java
│       └── PaginatedResponse.java
├── cms/
│   ├── util/CmsUtil.java        — parseLanguage, buildPageable, validateFilename, determineContentType
│   ├── article/                 — Article, ArticleImage sub-package
│   ├── audio/
│   ├── file/
│   ├── image/
│   ├── logo/
│   ├── question/
│   ├── video/
│   └── website/
└── master/setting/              — AppSetting
```

---

## Test Patterns

```java
// Service test
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @Mock ArticleRepository articleRepository;
    @InjectMocks ArticleService articleService;

    @Test
    void should_returnArticles_when_validRequest() { ... }  // naming: should_do_when_condition
}

// Controller test
@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean ArticleService articleService;
    @Autowired MockMvc mockMvc;
}
```

---

*Last Updated: 2026-04-04*
