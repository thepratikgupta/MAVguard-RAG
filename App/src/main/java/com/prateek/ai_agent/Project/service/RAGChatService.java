package com.prateek.ai_agent.Project.service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import com.prateek.ai_agent.Project.dto.ChatDto;
import com.prateek.ai_agent.Project.dto.TelecomSearchResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RAGChatService {

    private final Lucene3gppService luceneService;
    private final OpenAIClient openAIClient;

    private static final float RELEVANCE_THRESHOLD = 1.5f;

    public RAGChatService(Lucene3gppService luceneService,
                          @Value("${openai.api.key}") String apiKey,
                          @Value("${openai.base.url}") String baseUrl) {

        this.luceneService = luceneService;
        this.openAIClient = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .build();
    }

    public ChatDto.Response askQuestion(String userQuery) {

        List<TelecomSearchResult> searchResults = luceneService.search(userQuery, 10);

        if (searchResults.isEmpty() || searchResults.get(0).score() < RELEVANCE_THRESHOLD) {
            return new ChatDto.Response(
                    "Information not found in the provided 3GPP documentation.",
                    false,
                    List.of()
            );
        }

        StringBuilder contextBuilder = new StringBuilder();
        for (TelecomSearchResult result : searchResults) {
            contextBuilder.append(String.format("--- [Source Document: %s | Clause: %s] ---\n%s\n\n",
                    result.docId(), result.sectionNumber(), result.content()));
        }

        String systemPrompt = """
            You are a highly strict 3GPP Telecom Engineering Assistant.
            You must answer the user's question using ONLY the provided Lucene context.
            
            RULES:
            1. DO NOT use outside knowledge. If the answer is not in the context, say EXACTLY: 'Information not found in the provided 3GPP documentation.'
            2. You MUST cite your sources at the end of every claim using this exact format: [Source: {Document ID}, Clause {Section Number}].
            3. Be concise, technical, and professional.
            """;

        String userPrompt = "Context:\n" + contextBuilder.toString() + "\n\nUser Query: " + userQuery;

        ChatCompletionSystemMessageParam paramsOne = ChatCompletionSystemMessageParam.builder()
                .content(systemPrompt)
                .build();
        ChatCompletionUserMessageParam paramsTwo = ChatCompletionUserMessageParam.builder()
                .content(userPrompt)
                .build();

        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model("nvidia/nemotron-3-ultra-550b-a55b:free")
                .temperature(0.0)
                .addMessage(ChatCompletionMessageParam.ofSystem(paramsOne))
                .addMessage(ChatCompletionMessageParam.ofUser(paramsTwo))
                .build();

        String llmAnswer = openAIClient.chat().completions().create(params)
                .choices().get(0).message().content().orElse("Error generating response.");

        boolean actuallyFound = !llmAnswer.contains("Information not found");

        return new ChatDto.Response(llmAnswer, actuallyFound, searchResults);
    }
}