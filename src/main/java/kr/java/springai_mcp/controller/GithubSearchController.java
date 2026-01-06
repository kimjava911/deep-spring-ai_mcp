package kr.java.springai_mcp.controller;

import kr.java.springai_mcp.service.GithubMcpSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class GithubSearchController {

    private final GithubMcpSearchService searchService;

    @GetMapping("/github/search")
    public String search(@RequestParam String q) {
        // http://localhost:8080/github/search?q=react
        return searchService.searchRepository(q);
    }

    /**
     * callForAction() 플로우 사용
     */
    @GetMapping("/github/search/action")
    public String searchWithAction(@RequestParam String q) {
        return searchService.searchRepositoryWithAction(q);
    }
}
