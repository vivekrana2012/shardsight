package com.chat.ollama;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class DocumentRetriever {

    private final VectorStore vectorStore;
    private final QuerySanitizer querySanitizer;

    public DocumentRetriever(VectorStore vectorStore, QuerySanitizer querySanitizer) {
        this.vectorStore = vectorStore;
        this.querySanitizer = querySanitizer;
    }

    List<Document> retrieve(String query) {
        String cleanQuery = querySanitizer.sanitize(query);

        log.debug(STR."Sanitized the query :: \{cleanQuery}");

        List<Document> documents = vectorStore.similaritySearch(cleanQuery);

        log.debug(STR."Retrieved document count :: \{documents.size()}");

        return documents;
    }
}
