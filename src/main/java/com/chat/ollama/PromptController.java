package com.chat.ollama;

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api/v1/prompt")
@RestController
public class PromptController {

    private final TokenTextSplitter tokenTextSplitter;
    private final VectorStore vectorStore;
    private final ChatModel chatModel;
    private final Configuration configuration;

    public PromptController(TokenTextSplitter tokenTextSplitter, VectorStore vectorStore,
                            ChatModel chatModel, Configuration configuration) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
        this.chatModel = chatModel;
        this.configuration = configuration;
    }

    @PostMapping
    String prompt(@RequestBody Request request) {

        List<Document> searchDocuments = vectorStore.similaritySearch(request.getQuery());

        SystemPromptTemplate systemPromptTemplate
                = new SystemPromptTemplate("You are a knowledgeable assistant. " +
                "Use the provided context to answer the question accurately but don't mention the context in your reply. " +
                "Respond in plain text only. Do not use Markdown or HTML formatting: {context}");

        Message systemMessage = systemPromptTemplate.createMessage(
                Map.of("context",
                        searchDocuments.stream().map(Document::getText).collect(Collectors.joining("\n"))));

        Message userMessage = new UserMessage(request.getQuery());

        List<Message> messages = new ArrayList<>();

        messages.add(systemMessage);

        if (request.getOlderPrompts() != null) {
            messages.addAll(request.getOlderPrompts().stream()
                    .map(prompt -> new AssistantMessage(prompt.getResponse())).toList());
        }

        messages.add(userMessage);

        Prompt prompt = new Prompt(messages);

        ChatResponse response = chatModel.call(prompt);

        return response.getResults().stream()
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .collect(Collectors.joining());
    }

    @PostMapping("/init")
    String init() {
        TextReader reader = new TextReader(configuration.getSource());

        List<Document> documents = reader.get();

        List<Document> splitDocuments = tokenTextSplitter.apply(documents);

        System.out.println("Total Splits - " + splitDocuments.size());

        vectorStore.add(splitDocuments);

        System.out.println("Splits added to vector DB.");

        return "Yay it's DONE!";
    }
}
