package org.ganjp.api.cms.question;

import lombok.RequiredArgsConstructor;
import org.ganjp.api.cms.util.CmsUtil;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {
    private final QuestionRepository questionRepository;

    public PaginatedResponse<QuestionResponse> getQuestions(String question, Question.Language lang, String tags, Boolean isActive, int page, int size, String sort, String direction) {
        Pageable pageable = CmsUtil.buildPageable(page, size, sort, direction);
        Page<Question> pageResult = questionRepository.search(question, lang, tags, isActive, pageable);
        List<QuestionResponse> list = pageResult.getContent().stream().map(this::mapToResponse).toList();
        return PaginatedResponse.of(list, pageResult.getNumber(), pageResult.getSize(), pageResult.getTotalElements());
    }

    public List<QuestionResponse> getAllQuestions(String question, Question.Language lang, String tags,
            Boolean isActive, String updatedAfter) {
        return questionRepository.findAllQuestions(question, lang, tags, isActive, CmsUtil.parseLocalDateTime(updatedAfter)).stream()
                .map(this::mapToResponse)
                .toList();
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
                .channel(r.getChannel())
                .displayOrder(r.getDisplayOrder())
                .updatedAt(r.getUpdatedAt() != null ? r.getUpdatedAt().toString() : null)
                .build();
    }
}
