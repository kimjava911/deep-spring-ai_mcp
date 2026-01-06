package kr.java.springai_mcp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GithubApiService {

    private final RestClient client;

    public GithubApiService(@Value("${github.token}") String token) {
        this.client = RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + token)
                .build();
    }

    public String search(String query, int limit) {
        return client.get()
                .uri(uri -> uri.path("/search/repositories")
                        .queryParam("q", query)
                        .queryParam("per_page", limit)
                        .build())
                .retrieve()
                .body(String.class);
    }
}