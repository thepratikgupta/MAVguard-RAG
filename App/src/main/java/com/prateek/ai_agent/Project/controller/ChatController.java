package com.prateek.ai_agent.Project.controller;

import com.prateek.ai_agent.Project.dto.ChatDto;
import com.prateek.ai_agent.Project.service.RAGChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final RAGChatService chatService;

    public ChatController(RAGChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatDto.Response> query3gpp(@RequestBody ChatDto.Request request) {
        if (request.query() == null || request.query().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ChatDto.Response response = chatService.askQuestion(request.query());
        return ResponseEntity.ok(response);
    }
}
