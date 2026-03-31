package org.ganjp.api.cms.file;

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
public class FileService {
    private final FileRepository fileRepository;
    private final FileUploadProperties uploadProperties;

    @Value("${file.base-url:}")
    private String fileBaseUrl;

    public PaginatedResponse<FileResponse> getFiles(String name, org.ganjp.api.cms.file.File.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<org.ganjp.api.cms.file.File> pageResult = fileRepository.searchFiles(name, lang, tags, isActive, pageable);

        List<FileResponse> publicList = pageResult.getContent().stream().map(r ->
            FileResponse.builder()
                .id(r.getId())
                .name(r.getName())
                .description(null)
                .originalUrl(r.getOriginalUrl())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .url(CmsUtil.joinBaseAndPath(fileBaseUrl, r.getFilename()))
                .build()
        ).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public FileResponse getFileById(String id) {
        org.ganjp.api.cms.file.File r = fileRepository.findById(id).orElse(null);
        if (r == null) return null;
        return FileResponse.builder()
            .id(r.getId())
            .name(r.getName())
            .description(null)
            .originalUrl(r.getOriginalUrl())
            .tags(r.getTags())
            .lang(r.getLang())
            .displayOrder(r.getDisplayOrder())
            .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
            .url(CmsUtil.joinBaseAndPath(fileBaseUrl, r.getFilename()))
            .build();
    }

    public File getFileResource(String filename) throws IOException {
        if (!fileRepository.existsByFilename(filename)) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        Path p = Path.of(uploadProperties.getDirectory(), filename);
        if (!Files.exists(p)) throw new IllegalArgumentException("File not found: " + filename);
        return p.toFile();
    }
}
