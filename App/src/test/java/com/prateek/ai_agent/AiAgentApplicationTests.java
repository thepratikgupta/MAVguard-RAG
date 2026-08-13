package com.prateek.ai_agent;

import com.prateek.ai_agent.Project.dto.ChatDto;
import com.prateek.ai_agent.Project.service.RAGChatService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RAGChatServiceE2ETest {

    @Autowired
    private RAGChatService ragChatService;

    @Test
    @DisplayName("Valid 3GPP Query -> Should Return True and Cite Source")
    void testValid3GPPQuery_ShouldReturnTrueAndCiteSource() {

        String validQuery = "Can the 5G-RG connect to the 5GC via both accesses?";
        ChatDto.Response response = ragChatService.askQuestion(validQuery);

        Assertions.assertTrue(response.wasFoundInDocs(),
                "The system should flag 'wasFoundInDocs' as true for a valid telecom query.");
        Assertions.assertFalse(response.sourcesUsed().isEmpty(),
                "Lucene should have retrieved at least one source.");
        Assertions.assertTrue(response.answer().contains("Source: TS 23.501"),
                "The LLM response must contain a proper citation to prove it didn't hallucinate.");

        System.out.println("Passed Valid Query Test: " + response.answer());
    }

    @Test
    @DisplayName("Out-of-Domain Query -> Should Trigger Guardrail and Abort")
    void testOutOfDomainQuery_ShouldTriggerGuardrail() {

        String trickQuery = "What is the capital of France and how do I bake a cake?";
        ChatDto.Response response = ragChatService.askQuestion(trickQuery);

        Assertions.assertFalse(response.wasFoundInDocs(),
                "The guardrail should flag 'wasFoundInDocs' as false for out-of-domain queries.");
        Assertions.assertEquals("Information not found in the provided 3GPP documentation.", response.answer(),
                "The system must return the exact strict fallback message to prevent LLM hallucinations.");

        System.out.println("Passed Trick Query Test (Guardrail Triggered Successfully).");
    }
}