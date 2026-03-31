package org.ganjp.api.cms.audio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Locale;

@RestController
@RequestMapping("/v1/audios")
@RequiredArgsConstructor
@Slf4j
public class AudioController {
    private final AudioService audioService;

    @GetMapping
    public ApiResponse<PaginatedResponse<AudioResponse>> getAudios(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Audio.Language l = parseLanguage(lang, Audio.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = audioService.getAudios(name, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Audios retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<AudioResponse> getAudioById(@PathVariable String id) {
        AudioResponse r = audioService.getAudioById(id);
        if (r == null) return ApiResponse.error(404, "Audio not found", null);
        return ApiResponse.success(r, "Audio retrieved");
    }

    @GetMapping("/view/{filename}")
    public ResponseEntity<?> viewAudio(@PathVariable String filename,
                                       @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            return serveStreamable(audioService.getAudioFile(filename), filename, rangeHeader);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading audio file: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/cover-images/{filename}")
    public ResponseEntity<Resource> viewAudioCoverImage(@PathVariable String filename) {
        try {
            File file = audioService.getAudioCoverFile(filename);
            return serveInline(file, filename);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IOException e) {
            log.error("Error reading audio cover image: {}", filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<Resource> serveInline(File file, String filename) {
        Resource resource = new FileSystemResource(file);
        String contentType = CmsUtil.determineContentType(filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }

    private ResponseEntity<?> serveStreamable(File file, String filename, String rangeHeader) throws IOException {
        long contentLength = file.length();
        String contentType = CmsUtil.determineContentType(filename);

        if (rangeHeader == null) {
            Resource resource = new FileSystemResource(file);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentLength(contentLength)
                    .body(resource);
        }

        HttpRange httpRange = HttpRange.parseRanges(rangeHeader).get(0);
        long start = httpRange.getRangeStart(contentLength);
        long end = httpRange.getRangeEnd(contentLength);
        long rangeLength = end - start + 1;

        InputStreamResource resource = new InputStreamResource(new RangeInputStream(file, start, rangeLength));

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + contentLength)
                .contentLength(rangeLength)
                .body(resource);
    }

    private static class RangeInputStream extends java.io.InputStream {
        private final RandomAccessFile raf;
        private long remaining;

        RangeInputStream(File file, long start, long length) throws IOException {
            this.raf = new RandomAccessFile(file, "r");
            this.raf.seek(start);
            this.remaining = length;
        }

        @Override
        public int read() throws IOException {
            if (remaining <= 0) return -1;
            int b = raf.read();
            if (b != -1) remaining--;
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (remaining <= 0) return -1;
            int toRead = (int) Math.min(len, remaining);
            int r = raf.read(b, off, toRead);
            if (r > 0) remaining -= r;
            return r;
        }

        @Override
        public void close() throws IOException {
            try { raf.close(); } finally { super.close(); }
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
