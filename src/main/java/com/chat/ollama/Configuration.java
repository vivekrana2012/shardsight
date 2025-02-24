package com.chat.ollama;

import lombok.Getter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Getter
@org.springframework.context.annotation.Configuration
public class Configuration {

    @Value("classpath:/static/fantasy_village_v2.txt")
    private String source;

    @Bean
    TokenTextSplitter tokenTextSplitter() {
        return new TokenTextSplitter(100, 10, 5, 10000, Boolean.TRUE);
    }
}
