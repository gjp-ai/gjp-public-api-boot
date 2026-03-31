package org.ganjp.api.cms.audio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Transactional(readOnly = true)
public class AudioService {
    private final AudioRepository audioRepository;
    private final AudioUploadProperties uploadProperties;

    @Value("${audio.base-url:}")
    private String audioBaseUrl;

    public PaginatedResponse<AudioResponse> getAudios(String name, Audio.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Audio> pageResult = audioRepository.searchAudios(name, lang, tags, isActive, pageable);

        List<AudioResponse> publicList = pageResult.getContent().stream().map(r ->
            AudioResponse.builder()
                .id(r.getId())
                .title(r.getName())
                .description(r.getDescription())
                .subtitle(r.getSubtitle())
                .artist(r.getArtist())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(CmsUtil.joinBaseAndPath(audioBaseUrl, r.getFilename()))
                .coverImageUrl(CmsUtil.joinBasePathWithSegment(audioBaseUrl, "cover-images", r.getCoverImageFilename()))
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public AudioResponse getAudioById(String id) {
        Audio r = audioRepository.findById(id).orElse(null);
        if (r == null) return null;
        return AudioResponse.builder()
            .id(r.getId())
            .title(r.getName())
            .description(r.getDescription())
            .subtitle(r.getSubtitle())
            .artist(r.getArtist())
            .tags(r.getTags())
            .lang(r.getLang())
            .displayOrder(r.getDisplayOrder())
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
            .url(CmsUtil.joinBaseAndPath(audioBaseUrl, r.getFilename()))
            .coverImageUrl(CmsUtil.joinBasePathWithSegment(audioBaseUrl, "cover-images", r.getCoverImageFilename()))
            .build();
    }

    public File getAudioFile(String filename) throws IOException {
        if (!audioRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Audio not found: " + filename);
        }
        Path audioPath = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(audioPath)) {
            throw new IllegalArgumentException("Audio file not found: " + filename);
        }
        return audioPath.toFile();
    }

    public File getAudioCoverFile(String filename) throws IOException {
        if (!audioRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("Audio cover image not found: " + filename);
        }
        Path coverPath = Path.of(uploadProperties.getDirectory(), "cover-images", filename);
        if (!Files.exists(coverPath)) {
            throw new IllegalArgumentException("Cover image file not found: " + filename);
        }
        return coverPath.toFile();
    }
}
