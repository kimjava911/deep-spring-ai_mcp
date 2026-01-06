package kr.java.springai_mcp.controller;

import kr.java.springai_mcp.service.GithubMcpSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * GitHub 검색 화면을 제공하는 Controller
 */
@Controller
@RequiredArgsConstructor
public class GithubSearchViewController {

    private final GithubMcpSearchService searchService;

    @GetMapping
    public String index() {
        return "redirect:/github";
    }

    /**
     * 검색 화면
     */
    @GetMapping("/github")
    public String searchPage() {
        return "search";
    }

    /**
     * 기본 검색 (MCP 바로 호출)
     */
    @GetMapping("/github/search/view")
    public String search(
            @RequestParam String q,
            Model model
    ) {
        String result = searchService.searchRepository(q);
        model.addAttribute("query", q);
        model.addAttribute("result", result);
        model.addAttribute("mode", "direct");
        return "search";
    }

    /**
     * Action 기반 검색 (callForAction → MCP → summarize)
     */
    @GetMapping("/github/search/action/view")
    public String searchWithAction(
            @RequestParam String q,
            Model model
    ) {
        String result = searchService.searchRepositoryWithAction(q);
        model.addAttribute("query", q);
        model.addAttribute("result", result);
        model.addAttribute("mode", "action");
        return "search";
    }
}
