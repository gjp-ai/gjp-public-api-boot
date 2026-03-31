package org.ganjp.api.cms.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

@RestController
@RequestMapping("/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    private final FileService fileService;

    @GetMapping
    public ApiResponse<PaginatedResponse<FileResponse>> getFiles(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        org.ganjp.api.cms.file.File.Language l = parseLanguage(lang, org.ganjp.api.cms.file.File.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = fileService.getFiles(name, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Files retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<FileResponse> getFileById(@PathVariable String id) {
        FileResponse r = fileService.getFileById(id);
        if (r == null) return ApiResponse.error(404, "File not found", null);
        return ApiResponse.success(r, "File retrieved");
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewFile(@PathVariable String filename) {
        try {
            File file = fileService.getFileResource(filename);
            Resource resource = new FileSystemResource(file);
            String contentType = CmsUtil.determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentLength(file.length())
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private <E extends Enum<E>> E parseLanguage(String lang, Class<E> enumClass) {
        if (lang == null || lang.isBlank()) return null;
        try {
            return Enum.valueOf(enumClass, lang.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
