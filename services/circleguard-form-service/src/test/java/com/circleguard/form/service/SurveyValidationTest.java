package com.circleguard.form.service;

import com.circleguard.form.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SurveyValidationTest {

    private final SymptomMapper mapper = new SymptomMapper();

    @Test
    void shouldDetectSymptomsWhenAnyQuestionAnsweredYes() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(
                        Question.builder().id(q1).text("Do you have a fever?").type(QuestionType.YES_NO).build(),
                        Question.builder().id(q2).text("Do you have a cough?").type(QuestionType.YES_NO).build()
                )).build();
        HealthSurvey survey = HealthSurvey.builder()
                .responses(Map.of(q1.toString(), (Object) "NO", q2.toString(), (Object) "YES"))
                .build();

        assertTrue(mapper.hasSymptoms(survey, questionnaire));
    }

    @Test
    void shouldNotDetectSymptomsWhenAllAnsweredNo() {
        UUID q1 = UUID.randomUUID();
        UUID q2 = UUID.randomUUID();
        Questionnaire questionnaire = Questionnaire.builder()
                .questions(List.of(
                        Question.builder().id(q1).text("Do you have a fever?").type(QuestionType.YES_NO).build(),
                        Question.builder().id(q2).text("Do you have a cough?").type(QuestionType.YES_NO).build()
                )).build();
        HealthSurvey survey = HealthSurvey.builder()
                .responses(Map.of(q1.toString(), (Object) "NO", q2.toString(), (Object) "NO"))
                .build();

        assertFalse(mapper.hasSymptoms(survey, questionnaire));
    }

    @Test
    void shouldHandleEmptyQuestionnaireWithoutThrowing() {
        Questionnaire questionnaire = Questionnaire.builder().questions(List.of()).build();
        HealthSurvey survey = HealthSurvey.builder().responses(Map.of()).build();

        assertDoesNotThrow(() -> mapper.hasSymptoms(survey, questionnaire));
    }
}
