package org.ganjp.api.cms.video;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoService {
    private final VideoRepository videoRepository;
    private final VideoUploadProperties uploadProperties;

    @Value("${video.base-url:}")
    private String videoBaseUrl;

    @Value("${video.cover-image.base-url:}")
    private String videoCoverImageBaseUrl;

    public PaginatedResponse<VideoResponse> getVideos(String name, Video.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Video> pageResult = videoRepository.searchVideos(name, lang, tags, isActive, pageable);
        List<VideoResponse> list = pageResult.getContent().stream().map(this::mapToResponse).toList();
        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public List<VideoResponse> getAllVideos(String name, Video.Language lang, String tags,
            Boolean isActive, String updatedAfter) {
        return videoRepository.findAllVideos(name, lang, tags, isActive, CmsUtil.parseLocalDateTime(updatedAfter)).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public VideoResponse getVideoById(String id) {
        return videoRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public File getVideoFileByFilename(String filename) throws IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path videoPath = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(videoPath)) {
            throw new IllegalArgumentException("Video file not found: " + filename);
        }
        return videoPath.toFile();
    }

    public File getCoverImageFileByFilename(String filename) throws IOException {
        if (filename == null) throw new IllegalArgumentException("filename is null");
        Path coverPath = Path.of(uploadProperties.getDirectory(), "cover-images", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }

    private VideoResponse mapToResponse(Video r) {
        return VideoResponse.builder()
                .id(r.getId())
                .title(r.getName())
                .description(r.getDescription())
                .tags(r.getTags())
                .lang(r.getLang())
                .channel(r.getChannel())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(CmsUtil.joinBaseAndPath(videoBaseUrl, r.getFilename()))
                .coverImageUrl(CmsUtil.joinBaseAndPath(videoCoverImageBaseUrl, r.getCoverImageFilename()))
                .build();
    }
}
