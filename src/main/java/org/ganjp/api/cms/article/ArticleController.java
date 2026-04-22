package org.ganjp.api.cms.article;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

@Slf4j
@RestController
@RequestMapping("/open/articles")
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping
    public ApiResponse<PaginatedResponse<ArticleResponse>> getArticles(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Article.Language language = CmsUtil.parseLanguage(lang, Article.Language.class);
        if (lang != null && !lang.isBlank() && language == null)
            return ApiResponse.error(400, "Invalid lang", null);
        return ApiResponse.success(
                articleService.getArticles(title, language, tags, isActive, page, size, sort, direction),
                "Articles retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<ArticleDetailResponse> getArticleById(@PathVariable String id) {
        ArticleDetailResponse resp = articleService.getArticleById(id);
        if (resp == null) {
            return ApiResponse.error(404, "Article not found", null);
        }
        return ApiResponse.success(resp, "Article retrieved");
    }

    // Serve cover image (supports Range requests for large images)
    @GetMapping("/cover-images/{filename}")
    public ResponseEntity<?> viewCover(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        try {
            CmsUtil.validateFilename(filename);
            File file = articleService.getCoverImageFileByFilename(filename);
            long contentLength = file.length();
            String contentType = CmsUtil.determineContentType(filename);

            if (rangeHeader == null) {
                InputStreamResource full = new InputStreamResource(new FileInputStream(file));
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentLength(contentLength)
                        .body(full);
            }

            HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
            long start = httpRange.getRangeStart(contentLength);
            long end = httpRange.getRangeEnd(contentLength);
            long rangeLength = end - start + 1;

            InputStream rangeStream = new InputStream() {
                private final RandomAccessFile raf;
                private long remaining = rangeLength;
                {
                    this.raf = new RandomAccessFile(file, "r");
                    this.raf.seek(start);
                }

                @Override
                public int read() throws IOException {
                    if (remaining <= 0)
                        return -1;
                    int b = raf.read();
                    if (b != -1)
                        remaining--;
                    return b;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    if (remaining <= 0)
                        return -1;
                    int toRead = (int) Math.min(len, remaining);
                    int r = raf.read(b, off, toRead);
                    if (r > 0)
                        remaining -= r;
                    return r;
                }

                @Override
                public void close() throws IOException {
                    try {
                        raf.close();
                    } finally {
                        super.close();
                    }
                }
            };

            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                    .contentLength(rangeLength)
                    .body(new InputStreamResource(rangeStream));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading cover image: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
