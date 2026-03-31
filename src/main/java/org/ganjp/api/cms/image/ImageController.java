package org.ganjp.api.cms.image;

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
@RequestMapping("/v1/images")
@RequiredArgsConstructor
@Slf4j
public class ImageController {
    private final ImageService imageService;

    @GetMapping
    public ApiResponse<PaginatedResponse<ImageResponse>> getImages(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Image.Language l = parseLanguage(lang, Image.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = imageService.getImages(name, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Images retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<ImageResponse> getImageById(@PathVariable String id) {
        ImageResponse r = imageService.getImageById(id);
        if (r == null) return ApiResponse.error(404, "Image not found", null);
        return ApiResponse.success(r, "Image retrieved");
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<Resource> viewImage(@PathVariable String filename) {
        try {
            File file = imageService.getImageFile(filename);
            Resource resource = new FileSystemResource(file);
            String contentType = CmsUtil.determineContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading image file: {}", filename, e);
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
