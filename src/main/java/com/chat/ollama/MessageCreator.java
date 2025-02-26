package com.chat.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageCreator {

    List<Message> create(Request request, List<Document> retrievedDocuments) {

        SystemPromptTemplate systemPromptTemplate
                = new SystemPromptTemplate("You are a knowledgeable assistant. " +
                "Use the provided context to answer the question accurately but don't mention the context in your reply. " +
                "Respond in plain text only. Do not use Markdown or HTML formatting: {context}");

        Message systemMessage = systemPromptTemplate.createMessage(
                Map.of("context",
                        retrievedDocuments.stream().map(Document::getText).collect(Collectors.joining("\n"))));

        log.debug("System message created");

        Message userMessage = new UserMessage(request.getQuery());

        log.debug("User message created");

        List<Message> messages = new ArrayList<>();

        messages.add(systemMessage);

        log.debug("System message added");

        if (request.getOlderPrompts() != null) {
            messages.addAll(request.getOlderPrompts().stream()
                    .map(prompt -> new AssistantMessage(prompt.getResponse())).toList());
        }

        log.debug("Assistant messages added");

        messages.add(userMessage);

        log.debug("User message added");

        return messages;
    }
}
