package org.ganjp.api.cms.image;

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
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ImageService {
    private final ImageRepository imageRepository;
    private final ImageUploadProperties imageUploadProperties;

    @Value("${image.base-url:}")
    private String imageBaseUrl;

    public PaginatedResponse<ImageResponse> getImages(String name, Image.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Image> pageResult = imageRepository.searchImages(name, lang, tags, isActive, pageable);

        List<ImageResponse> publicList = pageResult.getContent().stream().map(this::mapToResponse).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public ImageResponse getImageById(String id) {
        return imageRepository.findByIdAndIsActiveTrue(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    public File getImageFile(String filename) throws IOException {
        imageRepository.findByFilenameAndIsActiveTrue(filename)
            .orElseThrow(() -> new IllegalArgumentException("Image not found or not active: " + filename));

        Path fullPath = Paths.get(imageUploadProperties.getDirectory()).resolve(filename);
        if (!Files.exists(fullPath)) {
            throw new IOException("Image file not found: " + filename);
        }
        File file = fullPath.toFile();
        if (!file.isFile() || !file.canRead()) {
            throw new IOException("Cannot read image file: " + filename);
        }
        return file;
    }

    private ImageResponse mapToResponse(Image r) {
        return ImageResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .originalUrl(r.getOriginalUrl())
                .altText(r.getAltText())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(CmsUtil.joinBaseAndPath(imageBaseUrl, r.getFilename()))
                .thumbnailUrl(CmsUtil.joinBaseAndPath(imageBaseUrl, r.getThumbnailFilename()))
                .build();
    }
}
