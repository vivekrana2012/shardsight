package com.chat.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ModelPrompter {

    private final ChatModel chatModel;

    public ModelPrompter(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    String prompt(List<Message> messages) {
        org.springframework.ai.chat.prompt.Prompt prompt = new Prompt(messages);

        log.debug("Prompting the model now");

        ChatResponse response = chatModel.call(prompt);

        log.debug("Got response from the model");

        return response.getResults().stream()
                .map(Generation::getOutput)
                .map(AssistantMessage::getText)
                .collect(Collectors.joining());
    }
}
