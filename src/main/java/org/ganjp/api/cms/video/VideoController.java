package org.ganjp.api.cms.video;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoController {
    private final VideoService videoService;

    @GetMapping
    public ApiResponse<PaginatedResponse<VideoResponse>> getVideos(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Video.Language l = parseLanguage(lang, Video.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = videoService.getVideos(name, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Videos retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable String id) {
        VideoResponse r = videoService.getVideoById(id);
        if (r == null) return ApiResponse.error(404, "Video not found", null);
        return ApiResponse.success(r, "Video retrieved");
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
