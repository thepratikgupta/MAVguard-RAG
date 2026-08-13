package com.prateek.ai_agent.Project.dto;

import java.util.List;

public class ChatDto {

    public record Request(String query) {}

    public record Response(String answer, boolean wasFoundInDocs, List<TelecomSearchResult> sourcesUsed){}
}
