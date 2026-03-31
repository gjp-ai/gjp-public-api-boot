package org.ganjp.api.cms.question;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ganjp.api.core.model.ApiResponse;
import org.ganjp.api.core.model.PaginatedResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequestMapping("/v1/questions")
@RequiredArgsConstructor
@Slf4j
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping
    public ApiResponse<PaginatedResponse<QuestionResponse>> getQuestions(
            @RequestParam(required = false) String question,
            @RequestParam(required = false) String lang,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "displayOrder") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        Question.Language l = parseLanguage(lang, Question.Language.class);
        if (lang != null && !lang.isBlank() && l == null) return ApiResponse.error(400, "Invalid lang", null);
        var resp = questionService.getQuestions(question, l, tags, isActive, page, size, sort, direction);
        return ApiResponse.success(resp, "Questions retrieved");
    }

    @GetMapping("/{id}")
    public ApiResponse<QuestionResponse> getQuestionById(@PathVariable String id) {
        QuestionResponse r = questionService.getQuestionById(id);
        if (r == null) return ApiResponse.error(404, "Question not found", null);
        return ApiResponse.success(r, "Question retrieved");
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
