package org.ganjp.api.cms.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class CmsUtil {
    private CmsUtil() {}

    public static Pageable buildPageable(int page, int size, String sort, String direction) {
        Sort.Direction dir = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        return PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(dir, sort));
    }

    /**
     * Join a base URL and a path (filename). Returns null if path is empty.
     */
    public static String joinBaseAndPath(String base, String path) {
        if (path == null || path.isBlank()) return null;
        if (base != null && !base.isBlank()) {
            String prefix = base;
            String p = path;
            if (!prefix.endsWith("/") && !p.startsWith("/")) prefix = prefix + "/";
            else if (prefix.endsWith("/") && p.startsWith("/")) p = p.substring(1);
            return prefix + p;
        }
        if (path.startsWith("http") || path.startsWith("/")) return path;
        return null;
    }

    /**
     * Join base + segment + path. Segment should not be null (e.g. "cover-images").
     */
    public static String joinBasePathWithSegment(String base, String segment, String path) {
        if (path == null || path.isBlank()) return null;
        if (segment == null) segment = "";
        String seg = segment;
        if (!seg.endsWith("/")) seg = seg + "/";
        if (base != null && !base.isBlank()) {
            String prefix = base;
            if (!prefix.endsWith("/")) prefix = prefix + "/";
            if (prefix.endsWith("/") && seg.startsWith("/")) seg = seg.substring(1);
            String p = path.startsWith("/") ? path.substring(1) : path;
            return prefix + seg + p;
        }
        if (path.startsWith("http") || path.startsWith("/")) return path;
        return "/" + seg + (path.startsWith("/") ? path.substring(1) : path);
    }

    public static String determineContentType(String filename) {
        String lowerFilename = filename.toLowerCase();
        if (lowerFilename.endsWith(".png")) return "image/png";
        if (lowerFilename.endsWith(".jpg") || lowerFilename.endsWith(".jpeg")) return "image/jpeg";
        if (lowerFilename.endsWith(".gif")) return "image/gif";
        if (lowerFilename.endsWith(".svg")) return "image/svg+xml";
        if (lowerFilename.endsWith(".webp")) return "image/webp";
        if (lowerFilename.endsWith(".bmp")) return "image/bmp";
        if (lowerFilename.endsWith(".mp4")) return "video/mp4";
        if (lowerFilename.endsWith(".webm")) return "video/webm";
        if (lowerFilename.endsWith(".ogv") || lowerFilename.endsWith(".ogg")) return "video/ogg";
        if (lowerFilename.endsWith(".mov")) return "video/quicktime";
        if (lowerFilename.endsWith(".mkv")) return "video/x-matroska";
        if (lowerFilename.endsWith(".mp3")) return "audio/mpeg";
        if (lowerFilename.endsWith(".wav")) return "audio/wav";
        if (lowerFilename.endsWith(".flac")) return "audio/flac";
        if (lowerFilename.endsWith(".aac")) return "audio/aac";
        if (lowerFilename.endsWith(".m4a")) return "audio/mp4";
        if (lowerFilename.endsWith(".pdf")) return "application/pdf";
        return "application/octet-stream";
    }
}
