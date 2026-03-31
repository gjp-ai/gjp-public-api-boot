package org.ganjp.api.cms.question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ganjp.api.cms.question.Question;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResponse {
    private String id;
    private String question;
    private String answer;
    private String tags;
    private Question.Language lang;
    private Integer displayOrder;
    private String updatedAt;
}
