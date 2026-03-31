package org.ganjp.api.cms.article.image;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.cms.article.ArticleProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleImageService {
    private final ArticleProperties articleProperties;

    public java.io.File getImageFile(String filename) {
        Path uploadPath = Paths.get(articleProperties.getContentImage().getUpload().getDirectory());
        return uploadPath.resolve(filename).toFile();
    }
}
