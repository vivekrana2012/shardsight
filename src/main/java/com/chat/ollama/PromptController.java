package com.chat.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequestMapping("/api/v1/prompt")
@RestController
public class PromptController {

    private final TokenTextSplitter tokenTextSplitter;
    private final VectorStore vectorStore;
    private final Configuration configuration;
    private final DocumentRetriever documentRetriever;
    private final MessageCreator messageCreator;
    private final ModelPrompter modelPrompter;

    public PromptController(TokenTextSplitter tokenTextSplitter, VectorStore vectorStore,
                            Configuration configuration, DocumentRetriever documentRetriever,
                            MessageCreator messageCreator, ModelPrompter modelPrompter) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStore = vectorStore;
        this.configuration = configuration;
        this.documentRetriever = documentRetriever;
        this.messageCreator = messageCreator;
        this.modelPrompter = modelPrompter;
    }

    @PostMapping
    String prompt(@RequestBody Request request) {

        log.info(STR."Got Request :: \{request.getQuery()}");

        // we need to search within our vector store to find query relevant information
        List<Document> retrievedDocuments = documentRetriever.retrieve(request.getQuery());

        // based on the user query, retrievedDocuments from vector store and
        // the historic conversation we need to create messages for the model
        List<Message> messages = messageCreator.create(request, retrievedDocuments);

        // now we prompt the model and get the response
        return modelPrompter.prompt(messages);
    }

    @PostMapping("/init")
    String init() {
        TextReader reader = new TextReader(configuration.getSource());

        List<Document> documents = reader.get();

        List<Document> splitDocuments = tokenTextSplitter.apply(documents);

        System.out.println(STR."Total Splits - \{splitDocuments.size()}");

        vectorStore.add(splitDocuments);

        System.out.println("Splits added to vector DB.");

        return "Yay it's DONE!";
    }
}
