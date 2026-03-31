package org.ganjp.api.cms.question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class QuestionService {
    private final QuestionRepository questionRepository;

    public PaginatedResponse<QuestionResponse> getQuestions(String question, Question.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Question> pageResult = questionRepository.search(question, lang, tags, isActive, pageable);

        List<QuestionResponse> publicList = pageResult.getContent().stream().map(this::mapToResponse).toList();

        return PaginatedResponse.of(publicList, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public QuestionResponse getQuestionById(String id) {
        return questionRepository.findById(id)
                .map(this::mapToResponse)
                .orElse(null);
    }

    private QuestionResponse mapToResponse(Question r) {
        return QuestionResponse.builder()
                .id(r.getId())
                .question(r.getQuestion())
                .answer(r.getAnswer())
                .tags(r.getTags())
                .lang(r.getLang())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }
}
