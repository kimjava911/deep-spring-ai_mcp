package kr.java.springai_mcp.config;

import kr.java.springai_mcp.tool.GithubSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
//    ChatClient chatClient(OpenAiChatModel chatModel, GithubSearchTool tool) {
    ChatClient chatClient(GoogleGenAiChatModel chatModel, GithubSearchTool tool) {
        return ChatClient.builder(chatModel)
                .defaultTools(tool)
                .build();
    }
}

