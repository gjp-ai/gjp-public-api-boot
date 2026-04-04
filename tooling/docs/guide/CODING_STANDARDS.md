# Coding Standards & Code Generation Guide

> Coding standards and conventions for the GJP Open API Spring Boot project. 
> This document acts as the **Blueprint for Code Generators** and human developers alike. All new modules (Entity, Service, Controller, DTOs) **MUST** strictly adhere to these patterns.

## Table of Contents
1. [Core Principles](#1-core-principles)
2. [Project Naming Conventions](#2-project-naming-conventions)
3. [Templates & Strict Examples](#3-templates--strict-examples)
   1. [Entity Template](#31-entity-template)
   2. [DTOs Template](#32-dtos-template)
   3. [Repository Template](#33-repository-template)
   4. [Service Template](#34-service-template)
   5. [Controller Template](#35-controller-template)
4. [General Rules](#4-general-rules)

---

## 1. Core Principles
- **Read-Only First:** The public API is predominantly for fetching data (`GET`). Mutations (`POST`, `PUT`, `DELETE`) are generally handled by the `gjp-admin-api-boot` project unless strictly required (like submitting a Question).
- **Lombok Over Boilerplate:** Aggressively use Lombok (`@RequiredArgsConstructor`, `@Getter`, `@Setter`, `@Builder`, `@Slf4j`) to keep files small and readable.
- **Fail Fast, Return Early:** Use inline guard clauses for null checks.

---

## 2. Project Naming Conventions

| Element | Convention | Example |
|---------|-----------|---------|
| Entity class | Singular noun | `Article`, `Video` |
| DTO classes | `{Entity}Response`, `{Entity}DetailResponse` | `ArticleResponse` |
| Service class | `{Entity}Service` | `ArticleService` |
| Controller class | `{Entity}Controller` | `ArticleController` |
| Repository | `{Entity}Repository` | `ArticleRepository` |
| API URL path | `/v1/{entity_plural}` | `/v1/articles` |

---

## 3. Templates & Strict Examples

When generating code for a new module (e.g., `Product`), strictly adhere to the following file structures.

### 3.1. Entity Template

**Rules:**
1. Never use `@Data` on an Entity (causes lazy-load proxy issues with `hashCode`).
2. ID is always `String` UUID (`length = 36`).
3. Must extend `BaseEntity`.

```java
package org.ganjp.api.cms.product;

import jakarta.persistence.*;
import lombok.*;
import org.ganjp.api.core.model.BaseEntity;

@Entity
@Table(name = "cms_products")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product extends BaseEntity {
    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "lang")
    private Language lang;

    public enum Language { EN, ZH }
}
```

### 3.2. DTOs Template

**Rules:**
1. Use `@Data` for DTOs.
2. Provide list view (`Response`) and full view (`DetailResponse`) if the entity is large.

```java
package org.ganjp.api.cms.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private String id;
    private String name;
    private Product.Language lang;
}
```

### 3.3. Repository Template

**Rules:**
1. Paginated queries must return `Page<T>` and accept `Pageable`.
2. Follow Spring Data JPA naming conventions for static queries.

```java
package org.ganjp.api.cms.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:lang IS NULL OR p.lang = :lang)")
    Page<Product> searchProducts(@Param("name") String name, 
                                 @Param("lang") Product.Language lang, 
                                 Pageable pageable);
}
```

### 3.4. Service Template

**Rules:**
1. Autowire dependencies via constructor using `@RequiredArgsConstructor`.
2. Read operations MUST be annotated with `@Transactional(readOnly = true)`.
3. Private mapping methods (`mapToResponse`) MUST exist so `.stream().map(this::mapToResponse)` can be used cleanly instead of inline lambda builders.
4. Optional `.orElse(null)` MUST be utilized for `findById` logic.

```java
package org.ganjp.api.cms.product;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.core.model.PaginatedResponse;
import org.ganjp.api.cms.util.CmsUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {
    private final ProductRepository productRepository;

    public PaginatedResponse<ProductResponse> getProducts(String name, Product.Language lang, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Product> pageResult = productRepository.searchProducts(name, lang, pageable);
        
        List<ProductResponse> list = pageResult.getContent().stream()
                .map(this::mapToResponse)
                .toList();

        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public ProductResponse getProductById(String id) {
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private ProductResponse mapToResponse(Product p) {
        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .lang(p.getLang())
                .build();
    }
}
```

### 3.5. Controller Template

**Rules:**
1. Endpoints map to `/v1/...`
2. No complex logic or transformations allowed in Controllers.
3. Every response MUST trigger `ApiResponse.success()` or `ApiResponse.error()`.
4. Keep the return inline `return ApiResponse.success(service.getSomething(), "Message");`
5. Fast null failures: `if (resp == null) return ApiResponse.error(404, "Not found", null);`

```java
package org.ganjp.api.cms.product;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.ganjp.api.cms.util.CmsUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ApiResponse<PaginatedResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
            
        Product.Language language = CmsUtil.parseLanguage(lang, Product.Language.class);
        if (lang != null && !lang.isBlank() && language == null) {
            return ApiResponse.error(400, "Invalid lang", null);
        }

        return ApiResponse.success(
                productService.getProducts(name, language, page, size, sort, direction), 
                "Products retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable String id) {
        ProductResponse resp = productService.getProductById(id);
        if (resp == null) return ApiResponse.error(404, "Product not found", null);
        return ApiResponse.success(resp, "Product retrieved");
    }
}
```

---

## 4. General Rules

1. **Imports Order:** Avoid wildcard imports (`.*`). Place custom domains (`org.ganjp.*`) after external domain libraries but before `org.springframework.*` and `java.*`.
2. **`@Slf4j` Usage:** Only add `@Slf4j` to Controllers/Services if logging lines actually exist within the class. Unused import warnings should be minimized.
3. **Enum Parsing:** NEVER hardcode or duplicate Enum string-parsing blocks in controllers. Extract validation to `CmsUtil.parseLanguage()`.
4. **Media Types:** When endpoints return Streams/Files, inject headers tightly and leverage `CmsUtil.determineContentType(filename)` natively.

---

## 5. Testing Rules (Strict Mandate)

**Unit testing is absolutely required for all components. Code coverage MUST exceed 90%.** Code generators must generate tests alongside implementation.

### 5.1. Core Testing Requirements
- **Nomenclature:** Test method names MUST follow `should_doSomething_when_condition()`.
- **Structure:** Every test MUST use the Given-When-Then (Arrange-Act-Assert) pattern.
- **Tools:** Use `JUnit 5` (`@Test`), `Mockito` (`@Mock`, `@InjectMocks`), and `AssertJ` (`assertThat`).

### 5.2. Service Layer Testing
- **Mandate:** Test all business logic, null handling, and optional unwraps. 
- **Pattern:** Use `@ExtendWith(MockitoExtension.class)`. Mock the `JpaRepository` and inject into the `Service`. Do NOT load the Spring Application Context.

```java
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock private ProductRepository productRepository;
    @InjectMocks private ProductService productService;

    @Test
    void should_returnProduct_when_found() {
        // Given
        when(productRepository.findById("123")).thenReturn(Optional.of(new Product()));
        // When
        ProductResponse resp = productService.getProductById("123");
        // Then
        assertThat(resp).isNotNull();
    }
}
```

### 5.3. Controller Layer Testing
- **Mandate:** Test HTTP semantics, JSON mapping (`PaginatedResponse`/`ApiResponse`), and status codes.
- **Pattern:** Use `@WebMvcTest(ProductController.class)` and `@MockBean` for the Service.

```java
@WebMvcTest(ProductController.class)
class ProductControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private ProductService productService;

    @Test
    void should_return200_when_gettingProducts() throws Exception {
        // Given
        when(productService.getProducts(any(), any(), anyInt(), anyInt(), any(), any()))
            .thenReturn(PaginatedResponse.of(List.of(), 0, 20, 0));
        
        // When & Then
        mockMvc.perform(get("/v1/products"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.status.code").value(200));
    }
}
```
