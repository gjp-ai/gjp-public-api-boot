package org.ganjp.api.cms.video;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/open/videos")
@RequiredArgsConstructor
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
        Video.Language language = CmsUtil.parseLanguage(lang, Video.Language.class);
        if (lang != null && !lang.isBlank() && language == null) {
            return ApiResponse.error(400, "Invalid lang", null);
        }
        return ApiResponse.success(
                videoService.getVideos(name, language, tags, isActive, page, size, sort, direction),
                "Videos retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<VideoResponse> getVideoById(@PathVariable String id) {
        VideoResponse resp = videoService.getVideoById(id);
        if (resp == null)
            return ApiResponse.error(404, "Video not found", null);
        return ApiResponse.success(resp, "Video retrieved");
    }

    @GetMapping("/cover-images/{filename}")
    public ResponseEntity<?> viewCoverImage(@PathVariable String filename) throws IOException {
        File file = videoService.getCoverImageFileByFilename(filename);
        String contentType = CmsUtil.determineContentType(filename);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .contentLength(file.length())
                .body(resource);
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<?> viewVideo(@PathVariable String filename, @RequestHeader(value = "Range", required = false) String rangeHeader) throws IOException {
        File file = videoService.getVideoFileByFilename(filename);
        long contentLength = file.length();
        String contentType = CmsUtil.determineContentType(filename);

        if (rangeHeader == null) {
            InputStreamResource full = new InputStreamResource(new FileInputStream(file));
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentLength(contentLength)
                    .body(full);
        }

        HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
        long start = httpRange.getRangeStart(contentLength);
        long end = httpRange.getRangeEnd(contentLength);
        long rangeLength = end - start + 1;

        // Stream the requested range without loading entire range into memory
        java.io.InputStream rangeStream = new java.io.InputStream() {
            private final java.io.RandomAccessFile raf;
            private long remaining = rangeLength;
            {
                this.raf = new java.io.RandomAccessFile(file, "r");
                this.raf.seek(start);
            }
            @Override
            public int read() throws java.io.IOException {
                if (remaining <= 0) return -1;
                int b = raf.read();
                if (b != -1) remaining--;
                return b;
            }
            @Override
            public int read(byte[] b, int off, int len) throws java.io.IOException {
                if (remaining <= 0) return -1;
                int toRead = (int) Math.min(len, remaining);
                int r = raf.read(b, off, toRead);
                if (r > 0) remaining -= r;
                return r;
            }
            @Override
            public void close() throws java.io.IOException {
                raf.close();
            }
        };

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                .contentLength(rangeLength)
                .body(new InputStreamResource(rangeStream));
    }
}
