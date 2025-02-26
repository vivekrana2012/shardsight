package com.chat.ollama;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuerySanitizer {

    private static final Set<String> STOP_WORDS = Arrays.stream(EnglishAnalyzer.getDefaultStopSet().toArray())
            .map(charArray -> new String((char[]) charArray))
            .collect(Collectors.toSet());

    String sanitize(String query) {
        return Arrays.stream(query.split("\\s+"))  // Split by spaces
                .map(String::toLowerCase)         // Convert to lowercase
                .filter(word -> !STOP_WORDS.contains(word))  // Remove stopwords
                .collect(Collectors.joining(" "));
    }
}
